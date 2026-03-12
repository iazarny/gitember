package com.az.gitember.service.avatar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.time.Duration;

/**
 * Shared HTTP + imaging utilities for avatar clients.
 * Package-private – not part of the public API.
 */
final class AvatarHttpHelper {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private AvatarHttpHelper() {}

    /**
     * Downloads the image at {@code url} and decodes it.
     * Returns {@code null} when the server responds with a non-200 status.
     *
     * @param url     absolute URL of the image
     * @param headers alternating header-name / header-value pairs (may be empty)
     */
    static BufferedImage downloadImage(String url, String... headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET();
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
        HttpResponse<byte[]> resp = HTTP.send(builder.build(),
                HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) return null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(resp.body())) {
            return ImageIO.read(bis);
        }
    }

    /**
     * Performs a JSON GET request and returns the response body, or
     * {@code null} when the server returns a non-200 status.
     */
    static String getJson(String url, String... headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET();
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
        HttpResponse<String> resp = HTTP.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) return null;
        return resp.body();
    }

    /** Returns the lowercase hex MD5 digest of the UTF-8 encoded string. */
    static String md5Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** URL-encodes {@code value} using UTF-8. Returns the original string on error. */
    static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Extracts the HTTPS base URL (scheme + host) from a git remote URL.
     * Works for both HTTPS ({@code https://host/path}) and SSH
     * ({@code git@host:path}) remote formats.
     *
     * <p>Returns {@code null} if the host cannot be determined.</p>
     */
    static String extractBaseUrl(String remoteUrl) {
        if (remoteUrl == null) return null;
        // HTTPS
        if (remoteUrl.startsWith("http://") || remoteUrl.startsWith("https://")) {
            try {
                URI uri = new URI(remoteUrl);
                return uri.getScheme() + "://" + uri.getHost();
            } catch (Exception e) {
                return null;
            }
        }
        // SSH: git@host:path  or  ssh://git@host/path
        String u = remoteUrl;
        if (u.startsWith("ssh://")) u = u.substring(6);
        int at = u.indexOf('@');
        if (at >= 0) u = u.substring(at + 1);
        int colon = u.indexOf(':');
        int slash  = u.indexOf('/');
        int end = colon >= 0 ? colon : (slash >= 0 ? slash : u.length());
        String host = u.substring(0, end);
        return host.isBlank() ? null : "https://" + host;
    }
}
