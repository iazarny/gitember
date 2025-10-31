from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Dict, Iterable, List, Optional, Tuple

import pygit2


class RepositoryOpenError(Exception):
    pass


@dataclass
class FileStatus:
    added: List[str]
    modified: List[str]
    deleted: List[str]
    untracked: List[str]
    conflicted: List[str]


@dataclass
class CommitInfo:
    sha: str  # short
    id_full: str
    summary: str
    author_name: str
    author_email: str
    author_time: int  # epoch seconds
    refs: List[str]
    parents: List[str]


@dataclass
class FileChange:
    status: str  # added, modified, deleted, renamed, copied
    old_path: Optional[str]
    new_path: Optional[str]


@dataclass
class CommitDetails:
    sha: str
    summary: str
    author_name: str
    author_email: str
    author_time: int
    refs: List[str]
    parents_full: List[str]
    files: List[FileChange]


class GitRepository:
    def __init__(self, repo: pygit2.Repository) -> None:
        self._repo = repo

    @classmethod
    def open(cls, path: str) -> "GitRepository":
        repo_path = cls._discover_repo_path(path)
        if repo_path is None:
            raise RepositoryOpenError(f"Not a git repository: {path}")
        try:
            repo = pygit2.Repository(repo_path)
            return cls(repo)
        except Exception as exc:  # noqa: BLE001
            raise RepositoryOpenError(str(exc)) from exc

    @staticmethod
    def _discover_repo_path(path: str) -> Optional[str]:
        abspath = os.path.abspath(path)
        if os.path.isdir(os.path.join(abspath, ".git")):
            return abspath
        try:
            found = pygit2.discover_repository(abspath)
            if found:
                return os.path.dirname(found.rstrip(os.sep))
        except Exception:  # noqa: BLE001
            return None
        return None

    @property
    def workdir(self) -> Optional[str]:
        return self._repo.workdir

    def status(self) -> FileStatus:
        status = self._repo.status()
        added: List[str] = []
        modified: List[str] = []
        deleted: List[str] = []
        untracked: List[str] = []
        conflicted: List[str] = []
        for path, flags in status.items():
            if flags & (pygit2.GIT_STATUS_WT_MODIFIED | pygit2.GIT_STATUS_INDEX_MODIFIED):
                modified.append(path)
            if flags & (pygit2.GIT_STATUS_WT_DELETED | pygit2.GIT_STATUS_INDEX_DELETED):
                deleted.append(path)
            if flags & pygit2.GIT_STATUS_INDEX_NEW:
                added.append(path)
            if flags & pygit2.GIT_STATUS_WT_NEW:
                untracked.append(path)
            if flags & pygit2.GIT_STATUS_CONFLICTED:
                conflicted.append(path)
        return FileStatus(added=added, modified=modified, deleted=deleted, untracked=untracked, conflicted=conflicted)

    # ----- content helpers for diffs -----
    def _read_blob_text(self, oid: pygit2.Oid) -> str:
        try:
            blob = self._repo[oid]
            if isinstance(blob, pygit2.Blob):
                return blob.data.decode("utf-8", errors="ignore")
        except Exception:  # noqa: BLE001
            pass
        return ""

    def _read_blob_bytes(self, oid: pygit2.Oid) -> bytes:
        try:
            blob = self._repo[oid]
            if isinstance(blob, pygit2.Blob):
                return bytes(blob.data)
        except Exception:  # noqa: BLE001
            pass
        return b""

    def get_head_text(self, path: str) -> str:
        try:
            head = self._repo.revparse_single("HEAD").peel(pygit2.Tree)
        except Exception:  # unborn or no HEAD
            return ""
        try:
            entry = head[path]
            return self._read_blob_text(entry.id)
        except KeyError:
            return ""

    def get_index_text(self, path: str) -> str:
        try:
            index = self._repo.index
            entry = index[path]
            return self._read_blob_text(entry.id)
        except KeyError:
            return ""

    def get_working_tree_text(self, path: str) -> str:
        workdir = self.workdir or ""
        fs_path = os.path.join(workdir, path)
        try:
            with open(fs_path, "r", encoding="utf-8", errors="ignore") as f:
                return f.read()
        except Exception:  # noqa: BLE001
            return ""

    # ----- content for commits -----
    def get_commit_text(self, commit_id: str, path: str) -> str:
        commit = self._repo.revparse_single(commit_id)
        if not isinstance(commit, pygit2.Commit):
            commit = commit.peel(pygit2.Commit)
        try:
            entry = commit.tree[path]
            return self._read_blob_text(entry.id)
        except KeyError:
            return ""

    def get_commit_bytes(self, commit_id: str, path: str) -> bytes:
        commit = self._repo.revparse_single(commit_id)
        if not isinstance(commit, pygit2.Commit):
            commit = commit.peel(pygit2.Commit)
        try:
            entry = commit.tree[path]
            return self._read_blob_bytes(entry.id)
        except KeyError:
            return b""

    def get_parent_text(self, commit_id: str, path: str) -> str:
        commit = self._repo.revparse_single(commit_id)
        if not isinstance(commit, pygit2.Commit):
            commit = commit.peel(pygit2.Commit)
        if not commit.parents:
            return ""
        parent = commit.parents[0]
        try:
            entry = parent.tree[path]
            return self._read_blob_text(entry.id)
        except KeyError:
            return ""

    def get_parent_bytes(self, commit_id: str, path: str) -> bytes:
        commit = self._repo.revparse_single(commit_id)
        if not isinstance(commit, pygit2.Commit):
            commit = commit.peel(pygit2.Commit)
        if not commit.parents:
            return b""
        parent = commit.parents[0]
        try:
            entry = parent.tree[path]
            return self._read_blob_bytes(entry.id)
        except KeyError:
            return b""

    # ----- history -----
    def _refs_by_commit(self) -> Dict[str, List[str]]:
        mapping: Dict[str, List[str]] = {}
        for ref_name in self._repo.listall_references():
            try:
                ref = self._repo.references[ref_name]
                target = ref.resolve().target
                sha = str(target)
                mapping.setdefault(sha, []).append(ref_name)
            except Exception:  # noqa: BLE001
                continue
        return mapping

    def history(self, limit: int = 200) -> List[CommitInfo]:
        if self._repo.head_is_unborn:
            return []
        walker = self._repo.walk(self._repo.head.target, pygit2.GIT_SORT_TIME)
        refs_map = self._refs_by_commit()
        commits: List[CommitInfo] = []
        for i, commit in enumerate(walker):
            commits.append(
                CommitInfo(
                    sha=str(commit.id)[:12],
                    id_full=str(commit.id),
                    summary=commit.message.splitlines()[0] if commit.message else "",
                    author_name=commit.author.name or "",
                    author_email=commit.author.email or "",
                    author_time=commit.author.time,
                    refs=refs_map.get(str(commit.id), []),
                    parents=[str(p.id)[:12] for p in commit.parents],
                )
            )
            if i + 1 >= limit:
                break
        return commits

    def commit_details(self, commit_id: str) -> CommitDetails:
        commit = self._repo.revparse_single(commit_id)
        if not isinstance(commit, pygit2.Commit):
            commit = commit.peel(pygit2.Commit)

        parents = list(commit.parents)
        old_tree = parents[0].tree if parents else self._repo.TreeBuilder().write() and self._repo[pygit2.Oid(hex="4b825dc642cb6eb9a060e54bf8d69288fbee4904")]  # empty tree
        new_tree = commit.tree

        diff = self._repo.diff(old_tree, new_tree)
        try:
            diff.find_similar()
        except Exception:
            pass

        files: List[FileChange] = []
        for d in diff.deltas:
            status = d.status_char()
            if status == "A":
                files.append(FileChange("added", None, d.new_file.path))
            elif status == "D":
                files.append(FileChange("deleted", d.old_file.path, None))
            elif status == "M":
                files.append(FileChange("modified", d.old_file.path, d.new_file.path))
            elif status == "R":
                files.append(FileChange("renamed", d.old_file.path, d.new_file.path))
            elif status == "C":
                files.append(FileChange("copied", d.old_file.path, d.new_file.path))
            else:
                files.append(FileChange("modified", d.old_file.path, d.new_file.path))

        return CommitDetails(
            sha=str(commit.id),
            summary=commit.message or "",
            author_name=commit.author.name or "",
            author_email=commit.author.email or "",
            author_time=commit.author.time,
            refs=self._refs_by_commit().get(str(commit.id), []),
            parents_full=[str(p.id) for p in parents],
            files=files,
        )

    def stage(self, paths: Iterable[str]) -> None:
        index = self._repo.index
        for p in paths:
            index.add(p)
        index.write()

    def unstage(self, paths: Iterable[str]) -> None:
        index = self._repo.index
        for p in paths:
            index.remove(p)
        index.write()

    def commit(self, message: str, author_name: Optional[str] = None, author_email: Optional[str] = None) -> str:
        index = self._repo.index
        tree_oid = index.write_tree()
        if not self._repo.head_is_unborn:
            parents = [self._repo.head.target]
        else:
            parents = []

        cfg = self._repo.config
        name = author_name or cfg.get("user.name")
        email = author_email or cfg.get("user.email")
        if not name or not email:
            raise ValueError("Git user.name and user.email must be set")
        author = committer = pygit2.Signature(name, email)

        oid = self._repo.create_commit(
            self._repo.head.name if not self._repo.head_is_unborn else "HEAD",
            author,
            committer,
            message,
            tree_oid,
            parents,
        )
        return str(oid)


