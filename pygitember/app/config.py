from __future__ import annotations

import json
import os
from dataclasses import dataclass
from typing import List, Optional


CONFIG_DIR = os.path.expanduser("~/.gitember")
CONFIG_PATH = os.path.join(CONFIG_DIR, "g2.json")


@dataclass
class ProjectEntry:
    path: str
    name: Optional[str] = None


def load_projects() -> List[ProjectEntry]:
    if not os.path.exists(CONFIG_PATH):
        return []
    try:
        with open(CONFIG_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
    except Exception:
        return []

    projects: List[ProjectEntry] = []
    # Support either array of strings or {projects:[{path,name}]}
    if isinstance(data, list):
        for p in data:
            if isinstance(p, str):
                projects.append(ProjectEntry(path=p, name=os.path.basename(p.rstrip("/"))))
    elif isinstance(data, dict):
        items = data.get("projects") or data.get("repos") or []
        if isinstance(items, list):
            for it in items:
                if isinstance(it, dict) and "path" in it:
                    name = it.get("name") or os.path.basename(it["path"].rstrip("/"))
                    projects.append(ProjectEntry(path=it["path"], name=name))
    # Filter existing paths only
    return [p for p in projects if os.path.isdir(p.path)]


def _ensure_config_dir() -> None:
    os.makedirs(CONFIG_DIR, exist_ok=True)


def save_projects(projects: List[ProjectEntry]) -> None:
    _ensure_config_dir()
    data = {"projects": [{"path": p.path, "name": p.name} for p in projects]}
    with open(CONFIG_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)


def add_recent_project(path: str, name: Optional[str] = None, max_items: int = 20) -> None:
    entries = load_projects()
    # Deduplicate by path, most recent first
    norm = os.path.abspath(path)
    filtered = [e for e in entries if os.path.abspath(e.path) != norm]
    display = name or os.path.basename(norm.rstrip("/"))
    filtered.insert(0, ProjectEntry(path=norm, name=display))
    save_projects(filtered[:max_items])


