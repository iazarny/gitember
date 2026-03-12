package com.az.gitember.service.avatar;

import java.awt.image.BufferedImage;

/**
 * Fetches avatars from <a href="https://gravatar.com">Gravatar</a> using
 * the MD5 hash of the committer's e-mail address.
 *
 * <p>This client acts as the universal fallback: {@link #supports} always
 * returns {@code true}, so it is tried last after all provider-specific
 * clients have been exhausted.</p>
 */
public class GravatarClient implements AvatarClient {

    private static final int SIZE = 64;

    @Override
    public boolean supports(String remoteUrl) {
        return true; // universal fallback – works for any remote
    }

    @Override
    public BufferedImage fetchAvatar(String email,
                                     String authorName,
                                     String remoteUrl,
                                     String accessToken) throws Exception {
        if (email == null || email.isBlank()) return null;
        String hash = AvatarHttpHelper.md5Hex(email.trim().toLowerCase());
        // d=404 → return HTTP 404 instead of a placeholder if no Gravatar is registered
        String url = "https://www.gravatar.com/avatar/" + hash + "?s=" + SIZE + "&d=404";
        return AvatarHttpHelper.downloadImage(url);
    }
}
