from PyQt6.QtCore import Qt
from PyQt6.QtGui import QAction
from PyQt6.QtWidgets import QMainWindow, QFileDialog, QMessageBox

from pygitember.git import GitRepository, RepositoryOpenError
from pygitember.ui.repo_view import RepoView
from pygitember.ui.projects_view import ProjectsView
from pygitember.app.config import add_recent_project


class MainWindow(QMainWindow):
    def __init__(self) -> None:
        super().__init__()
        self.setWindowTitle("PyGitember")
        self.resize(1200, 800)
        self._create_menu()
        self._show_projects()

    def _create_menu(self) -> None:
        menubar = self.menuBar()

        file_menu = menubar.addMenu("&File")
        open_repo_action = QAction("Open Repository…", self)
        open_repo_action.setShortcut("Ctrl+O")
        open_repo_action.triggered.connect(self._on_open_repo)
        file_menu.addAction(open_repo_action)

        file_menu.addSeparator()
        exit_action = QAction("Exit", self)
        exit_action.setShortcut("Ctrl+Q")
        exit_action.triggered.connect(self.close)
        file_menu.addAction(exit_action)

        help_menu = menubar.addMenu("&Help")
        about_action = QAction("About", self)
        about_action.triggered.connect(self._on_about)
        help_menu.addAction(about_action)

    def _on_open_repo(self) -> None:
        directory = QFileDialog.getExistingDirectory(self, "Open Git Repository")
        if not directory:
            return
        self._open_repo_path(directory)

    def _show_projects(self) -> None:
        projects = ProjectsView()
        projects.openRequested.connect(self._open_repo_path)
        self.setCentralWidget(projects)

    def _open_repo_path(self, directory: str) -> None:
        try:
            repo = GitRepository.open(directory)
            # record recent project
            add_recent_project(directory)
            view = RepoView(repo)
            self.setCentralWidget(view)
        except RepositoryOpenError as exc:
            QMessageBox.critical(self, "Open Failed", str(exc))

    def _on_about(self) -> None:
        QMessageBox.about(
            self,
            "About PyGitember",
            "PyGitember — a PyQt6 Git client (work in progress)",
        )


