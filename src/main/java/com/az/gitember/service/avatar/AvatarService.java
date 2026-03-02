package com.az.gitember.service.avatar;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates avatar retrieval across multiple {@link AvatarClient}
 * implementations and caches the results in memory.
 *
 * <p>Resolution order for a given remote URL:</p>
 * <ol>
 *   <li>{@link GitHubAvatarClient}  — for {@code github.com} remotes</li>
 *   <li>{@link GitLabAvatarClient}  — for {@code gitlab.*} remotes</li>
 *   <li>{@link BitbucketAvatarClient} — for {@code bitbucket.org} remotes</li>
 *   <li>{@link GravatarClient}      — universal fallback (any remote)</li>
 * </ol>
 *
 * <p>Each client's {@link AvatarClient#supports} gate ensures that only
 * the appropriate clients are tried for a given repository. {@link GravatarClient}
 * always returns {@code true} from {@code supports}, acting as a final fallback.</p>
 *
 * <p>A failed lookup ({@code null}) is cached so that the same author is not
 * queried again during the session. Call {@link #clearCache()} when switching
 * repositories.</p>
 */
public final class AvatarService {

    private static final Logger log = Logger.getLogger(AvatarService.class.getName());

    /** Ordered list of clients — provider-specific first, Gravatar last. */
    private static final List<AvatarClient> CLIENTS = List.of(
            new GitHubAvatarClient(),
            new GitLabAvatarClient(),
            new BitbucketAvatarClient(),
            new GravatarClient()
    );

    /**
     * In-memory cache keyed by the normalised e-mail (or author name when
     * e-mail is absent). {@code Optional.empty()} means "looked up, not found".
     */
    private static final Map<String, Optional<BufferedImage>> CACHE =
            new ConcurrentHashMap<>();

    private AvatarService() {}

    // ── public API ────────────────────────────────────────────────────────

    /**
     * Returns the avatar image for the committer, or {@code null} when no
     * avatar is available from any provider.
     *
     * <p>The call is fast when the result is cached. Otherwise it performs
     * synchronous HTTP requests — callers should invoke this method from a
     * background thread (e.g. inside a {@code SwingWorker}).</p>
     *
     * @param email       committer e-mail (preferred lookup key)
     * @param authorName  committer display name (fallback key)
     * @param remoteUrl   git remote URL of the current repository
     * @param accessToken optional API token for the hosting provider
     */
    public static BufferedImage getAvatar(String email,
                                          String authorName,
                                          String remoteUrl,
                                          String accessToken) {
        String cacheKey = cacheKey(email, authorName);
        if (cacheKey == null) return null;

        Optional<BufferedImage> cached = CACHE.get(cacheKey);
        if (cached != null) return cached.orElse(null);

        for (AvatarClient client : CLIENTS) {
            if (!client.supports(remoteUrl)) continue;
            try {
                BufferedImage img = client.fetchAvatar(email, authorName, remoteUrl, accessToken);
                if (img != null) {
                    CACHE.put(cacheKey, Optional.of(img));
                    return img;
                }
            } catch (Exception e) {
                log.log(Level.FINE,
                        "Avatar client " + client.getClass().getSimpleName() + " failed", e);
            }
        }

        // Cache the negative result to avoid repeated network calls
        CACHE.put(cacheKey, Optional.empty());
        return null;
    }

    /**
     * Clears the avatar image cache.
     * Should be called when the user opens a different repository.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    // ── private helpers ───────────────────────────────────────────────────

    private static String cacheKey(String email, String authorName) {
        if (email != null && !email.isBlank()) return email.trim().toLowerCase();
        if (authorName != null && !authorName.isBlank()) return authorName.trim().toLowerCase();
        return null;
    }
}
