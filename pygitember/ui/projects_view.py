from __future__ import annotations

from PyQt6.QtCore import pyqtSignal
from PyQt6.QtWidgets import QWidget, QVBoxLayout, QListWidget, QListWidgetItem, QLabel

from pygitember.app.config import load_projects


class ProjectsView(QWidget):
    openRequested = pyqtSignal(str)

    def __init__(self) -> None:
        super().__init__()
        self._init_ui()
        self.refresh()

    def _init_ui(self) -> None:
        layout = QVBoxLayout(self)
        self.title = QLabel("Recent Projects")
        self.list = QListWidget()
        self.list.itemDoubleClicked.connect(self._on_open)
        layout.addWidget(self.title)
        layout.addWidget(self.list)

    def refresh(self) -> None:
        self.list.clear()
        projects = load_projects()
        for p in projects:
            text = f"{p.name or ''} — {p.path}"
            item = QListWidgetItem(text)
            item.setData(256, p.path)  # Qt.UserRole
            self.list.addItem(item)

    def _on_open(self, item: QListWidgetItem) -> None:
        path = item.data(256)
        if isinstance(path, str):
            self.openRequested.emit(path)


