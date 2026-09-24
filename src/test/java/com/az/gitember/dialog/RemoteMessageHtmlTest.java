package com.az.gitember.dialog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoteMessageHtmlTest {

    @Test
    void sanitizeRemoteText_keepsLastCarriageReturnProgress() {
        String raw = "Resolving deltas: 0% (0/5)\rResolving deltas: 100% (5/5), done.\nOK";
        String cleaned = PullResultDialog.sanitizeRemoteText(raw);
        assertEquals("Resolving deltas: 100% (5/5), done.\nOK", cleaned);
        assertFalse(cleaned.contains("0% (0/5)"));
    }

    @Test
    void sanitizeRemoteText_stripsAnsiAndLoneCr() {
        String raw = "\r\u001B[32mGitHub\u001B[0m found issues";
        String cleaned = PullResultDialog.sanitizeRemoteText(raw);
        assertEquals("GitHub found issues", cleaned);
    }

    @Test
    void toHtml_encodesEmojiAsNumericEntities() {
        String html = PullResultDialog.toHtml("\u26A0\uFE0F GitHub found 28 vulnerabilities", 13);
        assertTrue(html.contains("&#9888;"), html);
        assertTrue(html.contains("&#65039;"), html);
        assertTrue(html.contains("GitHub found 28 vulnerabilities"), html);
        assertFalse(html.contains("\u26A0"), html);
        assertFalse(html.contains("charset="), html);
    }

    @Test
    void toHtml_linkifiesHttpsUrls() {
        String html = PullResultDialog.toHtml(
                "visit: https://github.com/iazarny/gitember/security/dependabot", 13);
        assertTrue(html.contains("<a href='https://github.com/iazarny/gitember/security/dependabot'>"), html);
    }

    @Test
    void createHtmlMessagePane_rendersMessageText() throws Exception {
        javax.swing.JEditorPane pane = PullResultDialog.createHtmlMessagePane(
                "refs/heads/master: OK\n\u26A0\uFE0F GitHub found 28 vulnerabilities");
        String shown = pane.getDocument().getText(0, pane.getDocument().getLength());
        assertTrue(shown.contains("refs/heads/master: OK"), shown);
        assertTrue(shown.contains("GitHub found 28 vulnerabilities"), shown);
        assertTrue(pane.getDocument().getLength() > 0);
    }
}
