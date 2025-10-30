from __future__ import annotations

from dataclasses import dataclass, field
from typing import List, Dict, Optional


@dataclass
class PlotLane:
    index: int


@dataclass
class PlotCommit:
    sha: str  # short
    id_full: str
    summary: str
    author_name: str
    author_email: str
    author_time: int
    refs: List[str]
    parents: List[str]  # short shas

    lane: PlotLane | None = None
    forkingOffLanes: List[PlotLane] = field(default_factory=list)
    passingLanes: List[PlotLane] = field(default_factory=list)
    mergingLanes: List[PlotLane] = field(default_factory=list)
    children: List[str] = field(default_factory=list)  # short shas of children


def build_plot(commits: List[dict]) -> List[PlotCommit]:
    """Build PlotCommit list from repository history order (top -> bottom).

    'commits' entries must contain keys:
      sha, id_full, summary, author_name, author_email, author_time, refs, parents (short shas)
    """
    # Build child links first
    by_sha: Dict[str, PlotCommit] = {}
    out: List[PlotCommit] = []
    for c in commits:
        pc = PlotCommit(
            sha=c["sha"],
            id_full=c["id_full"],
            summary=c["summary"],
            author_name=c["author_name"],
            author_email=c["author_email"],
            author_time=c["author_time"],
            refs=c["refs"],
            parents=c["parents"],
        )
        by_sha[pc.sha] = pc
        out.append(pc)

    for pc in out:
        for p in pc.parents:
            parent = by_sha.get(p)
            if parent is not None and pc.sha not in parent.children:
                parent.children.append(pc.sha)

    # Lane assignment similar to JGit's PlotWalk compacting approach
    lanes: List[Optional[str]] = []  # current head sha per lane
    for pc in out:
        # place current commit lane
        lane_idx = None
        if pc.sha in lanes:
            lane_idx = lanes.index(pc.sha)
        else:
            try:
                lane_idx = lanes.index(None)
                lanes[lane_idx] = pc.sha
            except ValueError:
                lane_idx = len(lanes)
                lanes.append(pc.sha)
        pc.lane = PlotLane(lane_idx)

        # passing lanes are all non-empty lanes apart from this commit's lane
        for i, v in enumerate(lanes):
            if i != lane_idx and v is not None:
                pc.passingLanes.append(PlotLane(i))

        # parents mapping
        if pc.parents:
            # first parent continues in same lane
            first = pc.parents[0]
            lanes[lane_idx] = first
            # merging lanes for additional parents
            for extra in pc.parents[1:]:
                try:
                    # reuse existing lane if parent already has one
                    idx = lanes.index(extra)
                except ValueError:
                    try:
                        idx = lanes.index(None)
                        lanes[idx] = extra
                    except ValueError:
                        idx = len(lanes)
                        lanes.append(extra)
                pc.mergingLanes.append(PlotLane(idx))
        else:
            # no parents, lane ends here
            lanes[lane_idx] = None

        # forkingOffLanes: new lanes created by children other than the one that continues
        cont_child = pc.children[0] if pc.children else None
        for ch in pc.children[1:]:
            # find lane of child if already assigned, else allocate a placeholder
            if ch in lanes:
                idx = lanes.index(ch)
            else:
                try:
                    idx = lanes.index(None)
                    lanes[idx] = ch
                except ValueError:
                    idx = len(lanes)
                    lanes.append(ch)
            pc.forkingOffLanes.append(PlotLane(idx))

        # compact trailing Nones to keep graph tight
        while lanes and lanes[-1] is None:
            lanes.pop()

    return out


