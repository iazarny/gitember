from __future__ import annotations

from PyQt6.QtWidgets import QMainWindow

from pygitember.ui.diff_viewer import DiffViewer


class DiffWindow(QMainWindow):
    def __init__(self, left_bytes: bytes, right_bytes: bytes, left_label: str, right_label: str) -> None:
        super().__init__()
        self.viewer = DiffViewer(left_bytes, right_bytes, left_label, right_label)
        self.setCentralWidget(self.viewer)


