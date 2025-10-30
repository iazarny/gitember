import os
import sys

from PyQt6.QtWidgets import QApplication

from pygitember.ui.main_window import MainWindow


def load_stylesheet(app: QApplication) -> None:
    qss_path = os.path.join(os.path.dirname(__file__), "..", "..", "resources", "qss", "dark.qss")
    qss_path = os.path.abspath(qss_path)
    if os.path.exists(qss_path):
        with open(qss_path, "r", encoding="utf-8") as f:
            app.setStyleSheet(f.read())


def main() -> int:
    app = QApplication(sys.argv)
    app.setApplicationName("PyGitember")
    load_stylesheet(app)

    window = MainWindow()
    window.show()
    return app.exec()


if __name__ == "__main__":
    raise SystemExit(main())


