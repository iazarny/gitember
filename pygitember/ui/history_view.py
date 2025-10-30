from __future__ import annotations

import datetime as dt

from typing import List, Dict

from PyQt6.QtCore import QSize, QRect, Qt
from PyQt6.QtGui import QPainter, QColor, QPen
from PyQt6.QtWidgets import (
    QWidget,
    QVBoxLayout,
    QListWidget,
    QListWidgetItem,
    QStyledItemDelegate,
    QSplitter,
    QTextEdit,
    QTreeWidget,
    QTreeWidgetItem,
    QMenu,
)

from pygitember.git import GitRepository
from pygitember.ui.history_plot import build_plot, PlotCommit


def _format_commit_row(sha: str, summary: str, author: str, when_epoch: int, refs: list[str]) -> str:
    when = dt.datetime.fromtimestamp(when_epoch)
    refs_part = f" [{', '.join(refs)}]" if refs else ""
    return f"{sha} — {summary}{refs_part} — {author} — {when:%Y-%m-%d %H:%M}"


class HistoryView(QWidget):
    def __init__(self, repo: GitRepository) -> None:
        super().__init__()
        self._repo = repo
        self._diff_windows = []  # keep refs to avoid GC
        self._init_ui()
        self.refresh()

    def _init_ui(self) -> None:
        layout = QVBoxLayout(self)
        splitter = QSplitter(Qt.Orientation.Vertical)
        layout.addWidget(splitter)

        # Top: history graph/list
        top = QWidget()
        top_layout = QVBoxLayout(top)
        self.list = QListWidget()
        self.list.setItemDelegate(GraphDelegate(self.list))
        self.list.setUniformItemSizes(True)
        self.list.currentItemChanged.connect(self._on_commit_selected)
        top_layout.addWidget(self.list)
        splitter.addWidget(top)

        # Bottom: commit details
        bottom = QWidget()
        bottom_layout = QVBoxLayout(bottom)
        self.details = QTextEdit()
        self.details.setReadOnly(True)
        self.files = QTreeWidget()
        self.files.setColumnCount(2)
        self.files.setHeaderLabels(["Status", "Path"])
        self.files.header().setStretchLastSection(True)
        bottom_layout.addWidget(self.details)
        bottom_layout.addWidget(self.files)
        self.files.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.files.customContextMenuRequested.connect(self._on_files_menu)
        splitter.addWidget(bottom)

        splitter.setStretchFactor(0, 7)
        splitter.setStretchFactor(1, 3)

    def refresh(self) -> None:
        self.list.clear()
        commits_raw = [
            {
                "sha": c.sha,
                "id_full": c.id_full,
                "summary": c.summary,
                "author_name": c.author_name,
                "author_email": c.author_email,
                "author_time": c.author_time,
                "refs": c.refs,
                "parents": c.parents,
            }
            for c in self._repo.history(limit=500)
        ]
        plot_commits: List[PlotCommit] = build_plot(commits_raw)

        # Determine lane count snapshot as max lane index +1 seen so far
        max_lane = 0
        for pc in plot_commits:
            max_lane = max(max_lane, pc.lane.index if pc.lane else 0)
            text = _format_commit_row(pc.sha, pc.summary, pc.author_name, pc.author_time, pc.refs)
            item = QListWidgetItem(text)
            item.setData(257, pc.lane.index if pc.lane else 0)
            item.setData(258, max_lane + 1)
            item.setData(259, pc.parents)
            item.setData(260, pc.id_full)
            # Edge targets based on merging lanes and first parent continuation
            edge_targets: List[int] = []
            if pc.parents:
                # first parent continues in same lane
                edge_targets.append(pc.lane.index if pc.lane else 0)
            for ln in pc.mergingLanes:
                edge_targets.append(ln.index)
            item.setData(261, edge_targets)
            # Passing lanes for background vertical hints (future)
            self.list.addItem(item)

    def _on_commit_selected(self, current: QListWidgetItem, previous: QListWidgetItem | None) -> None:
        if current is None:
            self.details.clear()
            self.files.clear()
            return
        commit_id = current.data(260)
        if not isinstance(commit_id, str):
            return
        info = self._repo.commit_details(commit_id)
        self._fill_details(info)

    def _fill_details(self, info) -> None:
        self.details.setPlainText(
            f"Commit: {info.sha[:12]}\n"
            f"Author: {info.author_name} <{info.author_email}>\n"
            f"Date: {dt.datetime.fromtimestamp(info.author_time):%Y-%m-%d %H:%M:%S}\n"
            f"Parents: {', '.join(p[:12] for p in info.parents_full) or '-'}\n"
            f"Refs: {', '.join(info.refs) or '-'}\n\n"
            f"{info.summary}"
        )
        self.files.clear()
        for fc in info.files:
            label = self._status_label(fc.status)
            path = fc.new_path or fc.old_path or ""
            item = QTreeWidgetItem([label, path])
            self.files.addTopLevelItem(item)

    def _on_files_menu(self, pos):
        item = self.files.itemAt(pos)
        if item is None:
            return
        path = item.text(1)
        menu = QMenu(self)
        act_vs_parent = menu.addAction("Diff: This commit vs Parent")
        act_vs_head = menu.addAction("Diff: This commit vs HEAD")
        act_vs_working = menu.addAction("Diff: This commit vs Working Tree")
        action = menu.exec(self.files.mapToGlobal(pos))
        if action is None:
            return
        current = self.list.currentItem()
        if current is None:
            return
        commit_id = current.data(260)
        from pygitember.ui.diff_window import DiffWindow
        if action == act_vs_parent:
            left = self._repo.get_parent_bytes(commit_id, path)
            right = self._repo.get_commit_bytes(commit_id, path)
            win = DiffWindow(left, right, "Parent", "Commit")
        elif action == act_vs_head:
            left = (self._repo.get_head_text(path) or "").encode("utf-8")
            right = self._repo.get_commit_text(commit_id, path).encode("utf-8")
            win = DiffWindow(left, right, "HEAD", "Commit")
        else:
            left = self._repo.get_commit_text(commit_id, path).encode("utf-8")
            right = self._repo.get_working_tree_text(path).encode("utf-8")
            win = DiffWindow(left, right, "Commit", "Working Tree")
        win.show()
        self._diff_windows.append(win)

    @staticmethod
    def _status_label(status: str) -> str:
        return {
            "added": "🟢 added",
            "modified": "🟡 modified",
            "deleted": "🔴 deleted",
            "renamed": "🔁 renamed",
            "copied": "📄 copied",
        }.get(status, status)


class GraphDelegate(QStyledItemDelegate):
    GUTTER = 130
    ROW_HEIGHT = 28
    LANE_SPACING = 16

    def sizeHint(self, option, index):  # noqa: D401
        return QSize(option.rect.width(), self.ROW_HEIGHT)

    def paint(self, painter: QPainter, option, index) -> None:  # noqa: D401
        painter.save()
        rect = option.rect
        lane = index.data(257) or 0
        lane_count = index.data(258) or 1
        edge_targets = index.data(261) or []

        # Draw lanes background area
        gutter_rect = QRect(rect.left(), rect.top(), self.GUTTER, rect.height())
        painter.fillRect(gutter_rect, QColor(43, 45, 48))

        y_mid = rect.top() + rect.height() // 2

        # Draw node
        node_x = rect.left() + 16 + lane * self.LANE_SPACING
        color = self._lane_color(lane)
        painter.setBrush(color)
        painter.setPen(QPen(QColor(20, 20, 22), 1))
        painter.drawEllipse(node_x - 4, y_mid - 4, 8, 8)

        # Draw edges to parent lanes (toward bottom of the row)
        edge_pen = QPen(QColor(170, 170, 175), 2)
        painter.setPen(edge_pen)
        for target_lane in edge_targets[:6]:  # limit visual clutter
            tx = rect.left() + 16 + target_lane * self.LANE_SPACING
            # soft diagonal toward next row position
            painter.drawLine(node_x, y_mid + 1, tx, rect.bottom())

        # Draw text after gutter
        painter.setPen(QColor(230, 230, 230))
        text_rect = QRect(rect.left() + self.GUTTER + 8, rect.top(), rect.width() - self.GUTTER - 8, rect.height())
        painter.drawText(text_rect, Qt.AlignmentFlag.AlignVCenter | Qt.AlignmentFlag.AlignLeft, index.data())

        painter.restore()

    @staticmethod
    def _lane_color(i: int) -> QColor:
        palette = [
            QColor(97, 218, 251),   # cyan
            QColor(255, 159, 67),   # orange
            QColor(123, 237, 159),  # green
            QColor(162, 155, 254),  # purple
            QColor(255, 107, 129),  # red
            QColor(250, 211, 144),  # sand
        ]
        return palette[i % len(palette)]


