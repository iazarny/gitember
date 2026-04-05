package com.az.gitember.data;

import java.util.List;

/**
 * Represents a single conflict block parsed from a file with git conflict markers.
 * Lines between &lt;&lt;&lt;&lt;&lt;&lt;&lt; and ======= are "ours"; between ======= and &gt;&gt;&gt;&gt;&gt;&gt;&gt; are "theirs".
 *
 * <p>The {@code oursStartLine}/{@code oursEndLine} fields hold 0-based line numbers
 * inside the <em>staged OURS file</em> (git stage 2), computed by counting context
 * lines that precede the block.  Similarly for the THEIRS side (git stage 3).
 * Using line numbers rather than character offsets keeps the highlighting correct
 * even when trailing-newline handling differs between the parsed and staged content.
 */
public class MergeConflictBlock {

    /** 0-based index among all blocks in the file. */
    public final int blockIndex;

    /** The &lt;&lt;&lt;&lt;&lt;&lt;&lt; … marker line (e.g. {@code <<<<<<< HEAD}). */
    public final String oursMarker;

    /** The &gt;&gt;&gt;&gt;&gt;&gt;&gt; … marker line (e.g. {@code >>>>>>> feature-branch}). */
    public final String theirsMarker;

    /** Lines belonging to our side (between &lt;&lt;&lt;&lt;&lt;&lt;&lt; and =======). */
    public final List<String> oursLines;

    /** Lines belonging to their side (between ======= and &gt;&gt;&gt;&gt;&gt;&gt;&gt;). */
    public final List<String> theirsLines;

    /**
     * 0-based first line of this block's ours section in the staged OURS file (stage 2).
     * The highlighted range is {@code [oursStartLine, oursEndLine)}.
     */
    public int oursStartLine;
    public int oursEndLine;

    /**
     * 0-based first line of this block's theirs section in the staged THEIRS file (stage 3).
     * The highlighted range is {@code [theirsStartLine, theirsEndLine)}.
     */
    public int theirsStartLine;
    public int theirsEndLine;

    public MergeConflictBlock(int blockIndex, String oursMarker, String theirsMarker,
                              List<String> oursLines, List<String> theirsLines) {
        this.blockIndex = blockIndex;
        this.oursMarker = oursMarker;
        this.theirsMarker = theirsMarker;
        this.oursLines = List.copyOf(oursLines);
        this.theirsLines = List.copyOf(theirsLines);
    }
}
