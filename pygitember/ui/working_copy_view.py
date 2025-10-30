from __future__ import annotations

from typing import List, Dict

from PyQt6.QtCore import Qt, QPoint
from PyQt6.QtWidgets import (
    QWidget,
    QVBoxLayout,
    QTabWidget,
    QListWidget,
    QListWidgetItem,
    QTreeWidget,
    QTreeWidgetItem,
    QMenu,
)

from pygitember.git import GitRepository


class WorkingCopyView(QWidget):
    def __init__(self, repo: GitRepository) -> None:
        super().__init__()
        self._repo = repo
        self._diff_windows = []  # keep refs to avoid GC
        self._init_ui()
        self.refresh()

    def _init_ui(self) -> None:
        layout = QVBoxLayout(self)
        self.tabs = QTabWidget()
        # All files with right-aligned status icon/text
        self.tree_all = QTreeWidget()
        self.tree_all.setColumnCount(2)
        self.tree_all.setHeaderLabels(["Status", "Path"])
        self.tree_all.header().setStretchLastSection(False)
        self.tree_all.header().setSectionResizeMode(0, self.tree_all.header().ResizeMode.ResizeToContents)
        self.tree_all.header().setSectionResizeMode(1, self.tree_all.header().ResizeMode.Stretch)
        self.list_modified = QListWidget()
        self.list_added = QListWidget()
        self.list_deleted = QListWidget()
        self.list_untracked = QListWidget()

        self.tabs.addTab(self.tree_all, "All")
        self.tabs.addTab(self.list_modified, "Modified")
        self.tabs.addTab(self.list_added, "Added")
        self.tabs.addTab(self.list_deleted, "Deleted")
        self.tabs.addTab(self.list_untracked, "Untracked")

        layout.addWidget(self.tabs)
        # Context menus
        for w in [self.tree_all, self.list_modified, self.list_added, self.list_deleted, self.list_untracked]:
            w.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
            w.customContextMenuRequested.connect(self._on_context_menu)

    def refresh(self) -> None:
        st = self._repo.status()
        self._fill_all({
            **{p: "modified" for p in st.modified},
            **{p: "added" for p in st.added},
            **{p: "deleted" for p in st.deleted},
            **{p: "untracked" for p in st.untracked},
            **{p: "conflicted" for p in st.conflicted},
        })
        self._fill(self.list_modified, st.modified)
        self._fill(self.list_added, st.added)
        self._fill(self.list_deleted, st.deleted)
        self._fill(self.list_untracked, st.untracked)

    def _fill(self, widget: QListWidget, items: List[str]) -> None:
        widget.clear()
        for p in sorted(items):
            widget.addItem(QListWidgetItem(p))

    def _fill_all(self, status_by_path: Dict[str, str]) -> None:
        self.tree_all.clear()
        # Deterministic order by path
        for path in sorted(status_by_path.keys()):
            status = status_by_path[path]
            item = QTreeWidgetItem([self._status_icon(status), path])
            # Right-align status column (now first)
            item.setTextAlignment(0, Qt.AlignmentFlag.AlignRight | Qt.AlignmentFlag.AlignVCenter)
            item.setToolTip(0, status.capitalize())
            self.tree_all.addTopLevelItem(item)

    def _status_icon(self, status: str) -> str:
        # Emoji placeholders for now; can be replaced with QIcon resources
        return {
            "added": "🟢",
            "modified": "🟡",
            "deleted": "🔴",
            "untracked": "🆕",
            "conflicted": "⚠️",
        }.get(status, "")

    # ----- context menu -----
    def _on_context_menu(self, pos: QPoint) -> None:
        sender = self.sender()
        if sender is self.tree_all:
            item = sender.itemAt(pos)
            path = item.text(1) if item else None
        elif isinstance(sender, QListWidget):
            item = sender.itemAt(pos)
            path = item.text() if item else None
        else:
            return
        if not path:
            return
        menu = QMenu(self)
        act_wt_vs_head = menu.addAction("Diff: Working Tree vs HEAD")
        act_wt_vs_index = menu.addAction("Diff: Working Tree vs Index")
        act_index_vs_head = menu.addAction("Diff: Index vs HEAD")
        act_wt_vs_disk = menu.addAction("Diff: Working Tree vs Disk")
        action = menu.exec(sender.mapToGlobal(pos))
        if action is None:
            return
        from pygitember.ui.diff_window import DiffWindow
        # fetch bytes for both sides
        if action == act_wt_vs_head:
            left = (self._repo.get_head_text(path) or "").encode("utf-8")
            right = self._repo.get_working_tree_text(path).encode("utf-8")
            win = DiffWindow(left, right, "HEAD", "Working Tree")
        elif action == act_wt_vs_index:
            left = (self._repo.get_index_text(path) or "").encode("utf-8")
            right = self._repo.get_working_tree_text(path).encode("utf-8")
            win = DiffWindow(left, right, "Index", "Working Tree")
        elif action == act_index_vs_head:
            left = (self._repo.get_head_text(path) or "").encode("utf-8")
            right = (self._repo.get_index_text(path) or "").encode("utf-8")
            win = DiffWindow(left, right, "HEAD", "Index")
        else:  # wt vs disk
            # disk is the same as working tree path; for now identical
            wt = self._repo.get_working_tree_text(path).encode("utf-8")
            win = DiffWindow(wt, wt, "Working Tree", "Disk")
        win.show()
        self._diff_windows.append(win)


