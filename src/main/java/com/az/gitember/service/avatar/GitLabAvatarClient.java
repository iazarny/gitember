package com.az.gitember.service.avatar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches committer avatars from GitLab (cloud or self-hosted).
 *
 * <p>Strategy:</p>
 * <ol>
 *   <li>Query {@code GET {base}/api/v4/users?search={email}} with the
 *       committer e-mail. GitLab returns users whose <em>public</em> e-mail
 *       or username matches the query.</li>
 *   <li>If no result, fall back to searching by display name.</li>
 *   <li>Download the {@code avatar_url} of the first hit.</li>
 * </ol>
 *
 * <p>Self-hosted GitLab instances are supported: the API base URL is derived
 * automatically from the remote URL (e.g. {@code https://gitlab.myco.com}).</p>
 */
public class GitLabAvatarClient implements AvatarClient {

    private static final Logger log = Logger.getLogger(GitLabAvatarClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean supports(String remoteUrl) {
        if (remoteUrl == null) return false;
        return remoteUrl.contains("gitlab.com") || remoteUrl.contains("gitlab.");
    }

    @Override
    public BufferedImage fetchAvatar(String email,
                                     String authorName,
                                     String remoteUrl,
                                     String accessToken) throws Exception {
        String base = AvatarHttpHelper.extractBaseUrl(remoteUrl);
        if (base == null) base = "https://gitlab.com";

        String token = resolve(accessToken);
        String[] headers = token != null
                ? new String[]{"PRIVATE-TOKEN", token}
                : new String[0];

        // 1. Search by e-mail
        if (email != null && !email.isBlank()) {
            String avatarUrl = searchUser(base, AvatarHttpHelper.urlEncode(email), headers);
            if (avatarUrl != null) return AvatarHttpHelper.downloadImage(avatarUrl, headers);
        }

        // 2. Fall back to display-name search
        if (authorName != null && !authorName.isBlank()) {
            String avatarUrl = searchUser(base, AvatarHttpHelper.urlEncode(authorName), headers);
            if (avatarUrl != null) return AvatarHttpHelper.downloadImage(avatarUrl, headers);
        }

        return null;
    }

    // ── private helpers ────────────────────────────────────────────────────

    private String searchUser(String base, String query, String[] headers) {
        try {
            String url = base + "/api/v4/users?search=" + query;
            String body = AvatarHttpHelper.getJson(url, headers);
            if (body == null) return null;
            JsonNode arr = MAPPER.readTree(body);
            if (!arr.isArray() || arr.isEmpty()) return null;
            String avatarUrl = arr.get(0).path("avatar_url").asText(null);
            return (avatarUrl == null || avatarUrl.isBlank()) ? null : avatarUrl;
        } catch (Exception e) {
            log.log(Level.FINE, "GitLab user search failed", e);
            return null;
        }
    }

    private String resolve(String projectToken) {
        if (projectToken != null && !projectToken.isBlank()) return projectToken;
        return System.getenv("GITLAB_TOKEN");
    }
}
