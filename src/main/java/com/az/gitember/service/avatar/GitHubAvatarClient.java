package com.az.gitember.service.avatar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches committer avatars from <a href="https://github.com">GitHub</a>.
 *
 * <p>Strategy:</p>
 * <ol>
 *   <li>Search GitHub users by e-mail:
 *       {@code GET /search/users?q={email}+in:email}.</li>
 *   <li>If no result (e-mail not public), fall back to searching by
 *       display name: {@code GET /search/users?q={authorName}+in:login}.</li>
 *   <li>Download the {@code avatar_url} returned in the first hit.</li>
 * </ol>
 *
 * <p>Rate limit: 10 req/min unauthenticated, 30 req/min with a token.
 * An access token is strongly recommended for repositories with many unique
 * authors.</p>
 */
public class GitHubAvatarClient implements AvatarClient {

    private static final Logger log = Logger.getLogger(GitHubAvatarClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_BASE = "https://api.github.com";

    @Override
    public boolean supports(String remoteUrl) {
        return remoteUrl != null && remoteUrl.contains("github.com");
    }

    @Override
    public BufferedImage fetchAvatar(String email,
                                     String authorName,
                                     String remoteUrl,
                                     String accessToken) throws Exception {
        String[] authHeaders = buildAuthHeaders(accessToken);

        // 1. Search by e-mail
        if (email != null && !email.isBlank()) {
            String avatarUrl = searchUser(
                    AvatarHttpHelper.urlEncode(email) + "+in:email", authHeaders);
            if (avatarUrl != null) return downloadAvatar(avatarUrl, authHeaders);
        }

        // 2. Fall back to display-name search
        if (authorName != null && !authorName.isBlank()) {
            String avatarUrl = searchUser(
                    AvatarHttpHelper.urlEncode(authorName) + "+in:login", authHeaders);
            if (avatarUrl != null) return downloadAvatar(avatarUrl, authHeaders);
        }

        return null;
    }

    // ── private helpers ────────────────────────────────────────────────────

    private String searchUser(String query, String[] authHeaders) {
        try {
            String url = API_BASE + "/search/users?q=" + query;
            String body = AvatarHttpHelper.getJson(url, authHeaders);
            if (body == null) return null;
            JsonNode items = MAPPER.readTree(body).path("items");
            if (items.isEmpty()) return null;
            String avatarUrl = items.get(0).path("avatar_url").asText(null);
            if (avatarUrl == null || avatarUrl.isBlank()) return null;
            // Append size parameter
            return avatarUrl.contains("?")
                    ? avatarUrl + "&s=64"
                    : avatarUrl + "?s=64";
        } catch (Exception e) {
            log.log(Level.FINE, "GitHub user search failed", e);
            return null;
        }
    }

    private BufferedImage downloadAvatar(String url, String[] authHeaders) throws Exception {
        return AvatarHttpHelper.downloadImage(url, authHeaders);
    }

    private String[] buildAuthHeaders(String accessToken) {
        String token = resolve(accessToken);
        if (token != null) {
            return new String[]{
                    "Authorization",        "Bearer " + token,
                    "Accept",               "application/vnd.github+json",
                    "X-GitHub-Api-Version", "2022-11-28"
            };
        }
        return new String[]{"Accept", "application/vnd.github+json"};
    }

    private String resolve(String projectToken) {
        if (projectToken != null && !projectToken.isBlank()) return projectToken;
        return System.getenv("GITHUB_TOKEN");
    }
}
