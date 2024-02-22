package com.az.gitember.controller;

import com.az.gitember.controller.lang.BaseTokenTypeAdapter;
import com.az.gitember.controller.lang.LangResolver;
import com.az.gitember.service.GitemberUtil;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.*;


/**
 * Adapt to Text for TextFlow.
 */
public class TextToSpanContentAdapter {

    private final String extension;

    private boolean rawDiff = false;
    private EditList patch = null;
    private boolean leftSide;

    private StyleSpans<Collection<String>> highlightingByKeywords = null;

    /**
     * @param extension The file extension
     * @param patch     patch
     * @param leftSide  what side ro read left - old, right - new
     */
    public TextToSpanContentAdapter(final String extension, final EditList patch,
                                    boolean leftSide, int activeParagraph) {
        this(extension, false);
        this.patch = patch;
        this.leftSide = leftSide;
    }

    TextToSpanContentAdapter(final String extension, final boolean rawDiff) {
        this.rawDiff = rawDiff;
        this.extension = extension;
    }

    public StyleSpans<Collection<String>> computeHighlighting(final String content) {
        final LangResolver langResolver = new LangResolver(extension, content);
        final CommonTokenStream commonTokenStream = new CommonTokenStream(langResolver.getLexer());
        commonTokenStream.fill();
        final List<Token> parsedCode = commonTokenStream.getTokens();

        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastKwEnd = 0;

        final Iterator<Token> tokenIterator = parsedCode.iterator();
        // TODO looks ugly , refactor
        if (tokenIterator.hasNext() && content.length() > 0) {
            if (highlightingByKeywords == null) {
                Token token = tokenIterator.next();
                final BaseTokenTypeAdapter adapter = langResolver.getAdapter();
                for (int i = 0; i < content.length(); i++) {
                    int startIdx = token.getStartIndex();
                    int stopIdx = token.getStopIndex() + 1;

                    if (i == token.getStartIndex()) {
                        int len = startIdx - lastKwEnd;
                        spansBuilder.add(Collections.emptyList(), len);

                        final String style = adapter.adaptToStyleClass(token.getType());
                        spansBuilder.add(Collections.singletonList(style), stopIdx - startIdx);
                        lastKwEnd = stopIdx;
                        token = tokenIterator.next();

                    }
                }
                highlightingByKeywords = spansBuilder.create();
            }
            return highlightingByKeywords;
        }
        return null ;
    }


    private Map<Integer, List<String>> diffDecoration = null;
    private Map<Integer, List<String>> pathcDecoration = null;

    public Map<Integer, List<String>> getDiffDecoration(String content) {
        if (diffDecoration == null) {
            if (rawDiff) {
                final ArrayList<String> lines = GitemberUtil.getLines(content);;
                diffDecoration = new HashMap<>();
                for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
                    final String line = lines.get(lineIdx);
                    final String style;
                    if (line.startsWith("+")) {
                        style = "diff-new";
                    } else if (line.startsWith("-")) {
                        style = "diff-deleted";
                    } else if (line.startsWith("@@")) {
                        style = "diff-modified";
                    } else {
                        style = null;
                    }
                    if (style != null) {
                        diffDecoration.put(lineIdx, Collections.singletonList(style));
                    }
                }
            } else {
                diffDecoration = Collections.EMPTY_MAP;
            }
        }
        return  diffDecoration;
    }

    /**
     * Highlight new , deleted and changed lines by patch.
     */

    public Map<Integer, List<String>> getDecorateByPatch(int activeParagraph) {
        if (pathcDecoration == null || activeParagraph > -1) {

            if (patch == null) {
                pathcDecoration = Collections.EMPTY_MAP;
            } else {
                pathcDecoration = new HashMap<>();
                for (int i = 0; i < patch.size(); i++) {
                    Edit delta = patch.get(i);
                    int origPos = delta.getBeginB();
                    int origLines = delta.getLengthB();
                    String styleClass = GitemberUtil.getDiffSyleClass(delta, "diff-line");
                    if (leftSide) {
                        origPos = delta.getBeginA();
                        origLines = delta.getLengthA();
                    }

                    for (int line = origPos; line < (origLines + origPos); line++) {
                        if (activeParagraph == i) {
                            pathcDecoration.put(line, List.of(styleClass, "diff-active"));
                        } else {
                            pathcDecoration.put(line, Collections.singletonList(styleClass));
                        }
                    }

                }
            }
        }
        return pathcDecoration;
    }

}
