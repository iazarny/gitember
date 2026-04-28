package com.az.gitember.service.avatar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches committer avatars from Azure DevOps.
 *
 * <p>Supported remote URL formats:</p>
 * <ul>
 *   <li>{@code https://dev.azure.com/{org}/{project}/_git/{repo}}</li>
 *   <li>{@code https://{org}.visualstudio.com/{project}/_git/{repo}}</li>
 *   <li>{@code git@ssh.dev.azure.com:v3/{org}/{project}/{repo}}</li>
 * </ul>
 *
 * <p>Strategy:</p>
 * <ol>
 *   <li>Extract the organisation name from the remote URL.</li>
 *   <li>Call the Azure DevOps Identities API to search by e-mail
 *       ({@code searchFilter=MailAddress}).</li>
 *   <li>If no match, retry with the display name
 *       ({@code searchFilter=General}).</li>
 *   <li>Download the {@code imageUrl} returned in the first identity hit.</li>
 * </ol>
 *
 * <p>Authentication: a Personal Access Token (PAT) is encoded as HTTP Basic
 * auth with an empty username — the standard Azure DevOps approach. Without a
 * PAT most organisation endpoints return 401. The token may also be supplied
 * via the {@code AZURE_DEVOPS_TOKEN} environment variable.</p>
 */
public class AzureDevOpsAvatarClient implements AvatarClient {

    private static final Logger log = Logger.getLogger(AzureDevOpsAvatarClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean supports(String remoteUrl) {
        if (remoteUrl == null) return false;
        return remoteUrl.contains("dev.azure.com")
                || remoteUrl.contains("visualstudio.com")
                || remoteUrl.contains("ssh.dev.azure.com");
    }

    @Override
    public BufferedImage fetchAvatar(String email,
                                     String authorName,
                                     String remoteUrl,
                                     String accessToken) throws Exception {
        String org = extractOrg(remoteUrl);
        if (org == null || org.isBlank()) return null;

        String[] headers = buildAuthHeaders(accessToken);

        // 1. Search by e-mail
        if (email != null && !email.isBlank()) {
            String imageUrl = findImageUrl(org, "MailAddress", email, headers);
            if (imageUrl != null) {
                BufferedImage img = AvatarHttpHelper.downloadImage(imageUrl, headers);
                if (img != null) return img;
            }
        }

        // 2. Fall back to display name
        if (authorName != null && !authorName.isBlank()) {
            String imageUrl = findImageUrl(org, "General", authorName, headers);
            if (imageUrl != null) {
                BufferedImage img = AvatarHttpHelper.downloadImage(imageUrl, headers);
                if (img != null) return img;
            }
        }

        return null;
    }

    // ── private helpers ───────────────────────────────────────────────────

    private String findImageUrl(String org, String searchFilter,
                                 String filterValue, String[] headers) {
        try {
            String url = "https://vssps.dev.azure.com/" + org
                    + "/_apis/identities?searchFilter=" + searchFilter
                    + "&filterValue=" + AvatarHttpHelper.urlEncode(filterValue)
                    + "&queryMembership=None&api-version=7.1";
            String body = AvatarHttpHelper.getJson(url, headers);
            if (body == null) return null;
            JsonNode values = MAPPER.readTree(body).path("value");
            if (values.isEmpty()) return null;
            String imageUrl = values.get(0).path("imageUrl").asText(null);
            return (imageUrl == null || imageUrl.isBlank()) ? null : imageUrl;
        } catch (Exception e) {
            log.log(Level.FINE, "Azure DevOps identity search failed", e);
            return null;
        }
    }

    /**
     * Extracts the Azure DevOps organisation name from a git remote URL.
     *
     * <ul>
     *   <li>{@code https://dev.azure.com/myorg/...}         → {@code myorg}</li>
     *   <li>{@code git@ssh.dev.azure.com:v3/myorg/...}       → {@code myorg}</li>
     *   <li>{@code https://myorg.visualstudio.com/...}       → {@code myorg}</li>
     * </ul>
     */
    static String extractOrg(String remoteUrl) {
        if (remoteUrl == null) return null;

        // dev.azure.com — HTTPS and SSH
        if (remoteUrl.contains("dev.azure.com")) {
            int idx = remoteUrl.indexOf("dev.azure.com");
            String tail = remoteUrl.substring(idx + "dev.azure.com".length());
            // SSH: ":v3/{org}/..."  — strip the :v{n}/ prefix
            if (tail.startsWith(":")) {
                int slash = tail.indexOf('/');
                if (slash < 0) return null;
                tail = tail.substring(slash + 1); // now "{org}/..."
            }
            // Remove any leading slash (HTTPS case)
            if (tail.startsWith("/")) tail = tail.substring(1);
            int end = tail.indexOf('/');
            String org = end > 0 ? tail.substring(0, end) : tail;
            return org.isBlank() ? null : org;
        }

        // {org}.visualstudio.com — HTTPS
        if (remoteUrl.contains("visualstudio.com")) {
            String url = remoteUrl;
            int schemeEnd = url.indexOf("://");
            if (schemeEnd >= 0) url = url.substring(schemeEnd + 3);
            int dot = url.indexOf(".visualstudio.com");
            if (dot > 0) return url.substring(0, dot);
        }

        return null;
    }

    /**
     * Builds HTTP Basic auth headers using the PAT.
     * Azure DevOps Basic auth uses an empty username and the PAT as password:
     * {@code Authorization: Basic base64(":" + PAT)}.
     */
    private static String[] buildAuthHeaders(String accessToken) {
        String token = resolve(accessToken);
        if (token != null && !token.isBlank()) {
            String encoded = Base64.getEncoder()
                    .encodeToString((":" + token).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return new String[]{
                    "Authorization", "Basic " + encoded,
                    "Accept",        "application/json"
            };
        }
        return new String[]{ "Accept", "application/json" };
    }

    private static String resolve(String projectToken) {
        if (projectToken != null && !projectToken.isBlank()) return projectToken;
        return System.getenv("AZURE_DEVOPS_TOKEN");
    }
}
