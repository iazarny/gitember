package com.az.gitember.service.avatar;

import java.awt.image.BufferedImage;

/**
 * Strategy interface for fetching a committer's avatar image from a specific
 * hosting provider (GitHub, GitLab, Bitbucket, Gravatar, …).
 *
 * <p>Implementations must be stateless and thread-safe.</p>
 */
public interface AvatarClient {

    /**
     * Returns {@code true} when this client can serve avatars for the
     * repository identified by {@code remoteUrl}.
     */
    boolean supports(String remoteUrl);

    /**
     * Fetches the avatar image for the given committer.
     *
     * @param email       committer e-mail (may be null or blank)
     * @param authorName  committer display name
     * @param remoteUrl   git remote URL – used to derive the API base URL for
     *                    self-hosted instances
     * @param accessToken optional API/personal-access token; may be null
     * @return scaled {@link BufferedImage}, or {@code null} if the avatar
     *         could not be found (caller should fall back to next client)
     * @throws Exception on unexpected network or parsing errors that the
     *                   caller should log
     */
    BufferedImage fetchAvatar(String email,
                              String authorName,
                              String remoteUrl,
                              String accessToken) throws Exception;
}
