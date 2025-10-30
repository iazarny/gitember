from __future__ import annotations

from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import QWidget, QSplitter, QVBoxLayout, QListWidget, QListWidgetItem, QStackedWidget

from pygitember.git import GitRepository
from pygitember.ui.working_copy_view import WorkingCopyView
from pygitember.ui.history_view import HistoryView


class RepoView(QWidget):
    def __init__(self, repo: GitRepository) -> None:
        super().__init__()
        self._repo = repo
        self._init_ui()

    def _init_ui(self) -> None:
        layout = QVBoxLayout(self)
        splitter = QSplitter(Qt.Orientation.Horizontal)
        layout.addWidget(splitter)

        # Left: navigation
        self.nav = QListWidget()
        self.nav.addItem(QListWidgetItem("Working Copy"))
        self.nav.addItem(QListWidgetItem("History"))
        self.nav.setCurrentRow(0)

        # Right: stacked views
        self.stack = QStackedWidget()
        self.view_working = WorkingCopyView(self._repo)
        self.view_history = HistoryView(self._repo)
        self.stack.addWidget(self.view_working)
        self.stack.addWidget(self.view_history)

        splitter.addWidget(self.nav)
        splitter.addWidget(self.stack)
        splitter.setStretchFactor(1, 1)

        self.nav.currentRowChanged.connect(self.stack.setCurrentIndex)


