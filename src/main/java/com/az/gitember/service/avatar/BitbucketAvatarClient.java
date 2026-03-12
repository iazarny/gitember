package com.az.gitember.service.avatar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches committer avatars from
 * <a href="https://bitbucket.org">Bitbucket Cloud</a>.
 *
 * <p>Strategy (Bitbucket does not expose a public email-search endpoint, so
 * we use the display name as best approximation):</p>
 * <ol>
 *   <li>Search workspaces / users by nickname (display name):
 *       {@code GET /2.0/users?q=nickname%3D%22{name}%22}.</li>
 *   <li>Extract {@code links.avatar.href} from the first hit and download.</li>
 * </ol>
 *
 * <p>An OAuth access token is recommended for authenticated users; the
 * {@code BITBUCKET_TOKEN} environment variable is used as fallback.</p>
 */
public class BitbucketAvatarClient implements AvatarClient {

    private static final Logger log = Logger.getLogger(BitbucketAvatarClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_BASE = "https://api.bitbucket.org/2.0";

    @Override
    public boolean supports(String remoteUrl) {
        return remoteUrl != null && remoteUrl.contains("bitbucket.org");
    }

    @Override
    public BufferedImage fetchAvatar(String email,
                                     String authorName,
                                     String remoteUrl,
                                     String accessToken) throws Exception {
        if (authorName == null || authorName.isBlank()) return null;

        String token = resolve(accessToken);
        String[] headers = token != null
                ? new String[]{"Authorization", "Bearer " + token}
                : new String[0];

        // Search by exact nickname match
        String url = API_BASE + "/users?q=nickname%3D%22"
                + AvatarHttpHelper.urlEncode(authorName) + "%22"
                + "&fields=values.links.avatar";
        try {
            String body = AvatarHttpHelper.getJson(url, headers);
            if (body == null) return null;
            JsonNode values = MAPPER.readTree(body).path("values");
            if (!values.isArray() || values.isEmpty()) return null;
            String avatarUrl = values.get(0)
                    .path("links").path("avatar").path("href").asText(null);
            if (avatarUrl == null || avatarUrl.isBlank()) return null;
            return AvatarHttpHelper.downloadImage(avatarUrl, headers);
        } catch (Exception e) {
            log.log(Level.FINE, "Bitbucket user search failed", e);
            return null;
        }
    }

    private String resolve(String projectToken) {
        if (projectToken != null && !projectToken.isBlank()) return projectToken;
        return System.getenv("BITBUCKET_TOKEN");
    }
}
