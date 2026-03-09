package com.az.gitember.service;

import com.az.gitember.data.PullRequest;
import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmItemAttribute;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches pull requests / merge requests from GitHub, GitLab, Bitbucket or Gitea
 * using their REST APIs. Returns an empty list silently when the host is unsupported.
 * Works without a token for public repositories (rate limits may apply).
 */
public class PullRequestService {

    private static final Logger log = Logger.getLogger(PullRequestService.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private enum Host { GITHUB, GITLAB, BITBUCKET, GITEA, UNKNOWN }

    /**
     * Fetches open PRs for the repository identified by {@code remoteUrl}.
     *
     * @param remoteUrl   git remote URL (HTTPS, SSH, git://)
     * @param accessToken project-level token (may be null; env fallback is tried)
     */
    public static List<PullRequest> fetch(String remoteUrl, String accessToken) {
        if (remoteUrl == null || remoteUrl.isBlank()) return Collections.emptyList();

        Host host = detectHost(remoteUrl);
        if (host == Host.UNKNOWN) return Collections.emptyList();

        String token = resolveToken(accessToken, host);

        try {
            return switch (host) {
                case GITHUB    -> fetchGitHub(remoteUrl, token);
                case GITLAB    -> fetchGitLab(remoteUrl, token);
                case BITBUCKET -> fetchBitbucket(remoteUrl, token);
                case GITEA     -> fetchGitea(remoteUrl, token);
                default        -> Collections.emptyList();
            };
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to fetch pull requests from " + remoteUrl, e);
            return Collections.emptyList();
        }
    }

    // ---- host detection ----

    private static Host detectHost(String url) {
        if (url.contains("github.com"))                           return Host.GITHUB;
        if (url.contains("gitlab.com") || url.contains("gitlab.")) return Host.GITLAB;
        if (url.contains("bitbucket.org"))                        return Host.BITBUCKET;
        if (url.contains("gitea."))                               return Host.GITEA;
        return Host.UNKNOWN;
    }

    /** Project token first; fall back to well-known environment variable. */
    private static String resolveToken(String projectToken, Host host) {
        if (projectToken != null && !projectToken.isBlank()) return projectToken;
        return switch (host) {
            case GITHUB    -> System.getenv("GITHUB_TOKEN");
            case GITLAB    -> System.getenv("GITLAB_TOKEN");
            case BITBUCKET -> System.getenv("BITBUCKET_TOKEN");
            case GITEA     -> System.getenv("GITEA_TOKEN");
            default        -> null;
        };
    }

    // ---- URL parsing ----

    /**
     * Extracts the path segments after the host domain from any supported remote URL.
     * Returns null if parsing fails.
     *
     * Examples:
     *   https://github.com/owner/repo.git        → ["owner","repo"]
     *   https://user@github.com/owner/repo.git   → ["owner","repo"]
     *   git@github.com:owner/repo.git            → ["owner","repo"]
     *   ssh://git@github.com/owner/repo.git      → ["owner","repo"]
     *   ssh://git@github.com:22/owner/repo.git   → ["owner","repo"]
     *   git://github.com/owner/repo.git          → ["owner","repo"]
     *   https://gitlab.com/g/sub/repo.git        → ["g","sub","repo"]
     */
    private static String[] parsePathSegments(String url, String hostDomain) {
        String u = url.trim().replaceAll("\\.git$", "");

        int domainIdx = u.indexOf(hostDomain);
        if (domainIdx < 0) return null;

        String afterDomain = u.substring(domainIdx + hostDomain.length());
        String path;

        if (afterDomain.startsWith("/")) {
            // https://host/path  or  ssh://user@host/path  or  git://host/path
            path = afterDomain.substring(1);
        } else if (afterDomain.startsWith(":")) {
            String rest = afterDomain.substring(1);
            // SSH with explicit port: :22/owner/repo → strip port number
            if (rest.matches("\\d+/.*")) {
                path = rest.substring(rest.indexOf('/') + 1);
            } else {
                // SCP style: git@host:owner/repo
                path = rest;
            }
        } else {
            return null;
        }

        if (path == null || path.isBlank()) return null;
        String[] parts = path.split("/");
        return parts.length >= 2 ? parts : null;
    }

    /**
     * Extracts the hostname from any git remote URL format.
     *
     * Examples:
     *   https://gitea.example.com/owner/repo.git  → "gitea.example.com"
     *   git@gitea.example.com:owner/repo.git      → "gitea.example.com"
     *   ssh://git@gitea.example.com/owner/repo    → "gitea.example.com"
     */
    private static String extractHostname(String url) {
        String u = url.trim().replaceAll("\\.git$", "");

        int schemeEnd = u.indexOf("://");
        if (schemeEnd >= 0) {
            // Scheme-based: strip scheme and optional user@
            String rest = u.substring(schemeEnd + 3);
            int at = rest.indexOf('@');
            if (at >= 0) rest = rest.substring(at + 1);
            // Hostname ends at first ':' or '/'
            int slash = rest.indexOf('/');
            int colon = rest.indexOf(':');
            if (slash < 0 && colon < 0) return rest.isEmpty() ? null : rest;
            if (slash < 0) return rest.substring(0, colon);
            if (colon < 0) return rest.substring(0, slash);
            return rest.substring(0, Math.min(slash, colon));
        }

        // SCP style: user@host:path
        int at = u.indexOf('@');
        int colon = u.indexOf(':', at + 1);
        if (at >= 0 && colon > at) return u.substring(at + 1, colon);

        return null;
    }

    // ---- GitHub ----

    private static List<PullRequest> fetchGitHub(String remoteUrl, String token) throws Exception {
        String[] parts = parsePathSegments(remoteUrl, "github.com");
        if (parts == null) return Collections.emptyList();

        String apiUrl = "https://api.github.com/repos/" + parts[0] + "/" + parts[1]
                + "/pulls?state=open&per_page=100";

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28");
        if (token != null && !token.isBlank()) req.header("Authorization", "Bearer " + token);
        String body = get(apiUrl, req);
        if (body == null) return Collections.emptyList();

        List<PullRequest> result = new ArrayList<>();
        for (JsonNode pr : MAPPER.readTree(body)) {
            result.add(new PullRequest(
                    pr.get("number").asInt(),
                    pr.get("title").asText(""),
                    pr.path("user").path("login").asText(""),
                    pr.path("head").path("ref").asText(""),
                    pr.path("base").path("ref").asText(""),
                    pr.path("html_url").asText(""),
                    pr.path("state").asText("open")));
        }
        return result;
    }

    // ---- GitLab ----

    private static List<PullRequest> fetchGitLab(String remoteUrl, String token) throws Exception {
        String[] parts = parsePathSegments(remoteUrl, "gitlab.com");
        if (parts == null) return Collections.emptyList();

        // Join all segments with %2F for the project path (handles nested groups)
        String encodedPath = String.join("%2F", parts);
        String apiUrl = "https://gitlab.com/api/v4/projects/" + encodedPath
                + "/merge_requests?state=opened&per_page=100";

        HttpRequest.Builder req = HttpRequest.newBuilder();
        if (token != null && !token.isBlank()) req.header("PRIVATE-TOKEN", token);
        String body = get(apiUrl, req);
        if (body == null) return Collections.emptyList();

        List<PullRequest> result = new ArrayList<>();
        for (JsonNode mr : MAPPER.readTree(body)) {
            result.add(new PullRequest(
                    mr.get("iid").asInt(),
                    mr.get("title").asText(""),
                    mr.path("author").path("username").asText(""),
                    mr.path("source_branch").asText(""),
                    mr.path("target_branch").asText(""),
                    mr.path("web_url").asText(""),
                    mr.path("state").asText("opened")));
        }
        return result;
    }

    // ---- Bitbucket ----

    private static List<PullRequest> fetchBitbucket(String remoteUrl, String token) throws Exception {
        String[] parts = parsePathSegments(remoteUrl, "bitbucket.org");
        if (parts == null) return Collections.emptyList();

        String apiUrl = "https://api.bitbucket.org/2.0/repositories/" + parts[0] + "/" + parts[1]
                + "/pullrequests?state=OPEN&pagelen=50";

        HttpRequest.Builder req = HttpRequest.newBuilder();
        if (token != null && !token.isBlank()) req.header("Authorization", "Bearer " + token);
        String body = get(apiUrl, req);
        if (body == null) return Collections.emptyList();

        List<PullRequest> result = new ArrayList<>();
        for (JsonNode pr : MAPPER.readTree(body).path("values")) {
            result.add(new PullRequest(
                    pr.get("id").asInt(),
                    pr.get("title").asText(""),
                    pr.path("author").path("nickname").asText(""),
                    pr.path("source").path("branch").path("name").asText(""),
                    pr.path("destination").path("branch").path("name").asText(""),
                    pr.path("links").path("html").path("href").asText(""),
                    pr.path("state").asText("OPEN")));
        }
        return result;
    }

    // ---- Gitea ----

    private static List<PullRequest> fetchGitea(String remoteUrl, String token) throws Exception {
        String host = extractHostname(remoteUrl);
        if (host == null) return Collections.emptyList();

        String[] parts = parsePathSegments(remoteUrl, host);
        if (parts == null) return Collections.emptyList();

        String apiUrl = "https://" + host + "/api/v1/repos/" + parts[0] + "/" + parts[1]
                + "/pulls?state=open&limit=50";

        HttpRequest.Builder req = HttpRequest.newBuilder();
        // Gitea uses "token <token>", not "Bearer"
        if (token != null && !token.isBlank()) req.header("Authorization", "token " + token);
        String body = get(apiUrl, req);
        if (body == null) return Collections.emptyList();

        List<PullRequest> result = new ArrayList<>();
        for (JsonNode pr : MAPPER.readTree(body)) {
            result.add(new PullRequest(
                    pr.get("number").asInt(),
                    pr.get("title").asText(""),
                    pr.path("user").path("login").asText(""),
                    pr.path("head").path("label").asText(""),
                    pr.path("base").path("label").asText(""),
                    pr.path("html_url").asText(""),
                    pr.path("state").asText("open")));
        }
        return result;
    }

    // ---- PR file list via API (fallback when branches are not available locally) ----

    /**
     * Fetches the list of files changed in a PR/MR directly from the hosting API.
     * Use this when the branch is not available in the local clone (e.g. fork PRs).
     *
     * @param remoteUrl   git remote URL
     * @param accessToken project-level token (may be null)
     * @param prNumber    PR / MR number
     * @return list of ScmItems, or empty list if the host is unsupported / request fails
     */
    public static List<ScmItem> fetchPrFiles(String remoteUrl, String accessToken, int prNumber) {
        if (remoteUrl == null || remoteUrl.isBlank()) return Collections.emptyList();

        Host host = detectHost(remoteUrl);
        String token = resolveToken(accessToken, host);

        try {
            return switch (host) {
                case GITHUB -> fetchGitHubPrFiles(remoteUrl, token, prNumber);
                case GITLAB -> fetchGitLabMrFiles(remoteUrl, token, prNumber);
                default     -> Collections.emptyList();
            };
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to fetch PR files from API for PR #" + prNumber, e);
            return Collections.emptyList();
        }
    }

    private static List<ScmItem> fetchGitHubPrFiles(String remoteUrl, String token, int prNumber)
            throws Exception {
        String[] parts = parsePathSegments(remoteUrl, "github.com");
        if (parts == null) return Collections.emptyList();

        // GitHub paginates at 30 by default; fetch up to 100 pages of 100
        List<ScmItem> result = new ArrayList<>();
        int page = 1;
        while (true) {
            String apiUrl = "https://api.github.com/repos/" + parts[0] + "/" + parts[1]
                    + "/pulls/" + prNumber + "/files?per_page=100&page=" + page;

            HttpRequest.Builder req = HttpRequest.newBuilder()
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28");
            if (token != null && !token.isBlank()) req.header("Authorization", "Bearer " + token);

            String body = get(apiUrl, req);
            if (body == null) break;

            JsonNode arr = MAPPER.readTree(body);
            if (!arr.isArray() || arr.size() == 0) break;

            for (JsonNode f : arr) {
                String filename = f.path("filename").asText("");
                String prev     = f.path("previous_filename").asText(null);
                String status   = f.path("status").asText("modified");
                String scmStatus = switch (status) {
                    case "added"   -> ScmItem.Status.ADDED;
                    case "removed" -> ScmItem.Status.REMOVED;
                    case "renamed" -> ScmItem.Status.RENAMED;
                    default        -> ScmItem.Status.MODIFIED;
                };
                ScmItemAttribute attr = new ScmItemAttribute().withStatus(scmStatus);
                if (prev != null && !prev.isBlank()) attr.withOldName(prev);
                result.add(new ScmItem(filename, attr));
            }
            if (arr.size() < 100) break;
            page++;
        }
        return result;
    }

    private static List<ScmItem> fetchGitLabMrFiles(String remoteUrl, String token, int prNumber)
            throws Exception {
        String[] parts = parsePathSegments(remoteUrl, "gitlab.com");
        if (parts == null) return Collections.emptyList();

        String encodedPath = String.join("%2F", parts);
        String apiUrl = "https://gitlab.com/api/v4/projects/" + encodedPath
                + "/merge_requests/" + prNumber + "/changes";

        HttpRequest.Builder req = HttpRequest.newBuilder();
        if (token != null && !token.isBlank()) req.header("PRIVATE-TOKEN", token);

        String body = get(apiUrl, req);
        if (body == null) return Collections.emptyList();

        List<ScmItem> result = new ArrayList<>();
        for (JsonNode f : MAPPER.readTree(body).path("changes")) {
            String newPath  = f.path("new_path").asText("");
            String oldPath  = f.path("old_path").asText("");
            boolean isNew      = f.path("new_file").asBoolean(false);
            boolean isDeleted  = f.path("deleted_file").asBoolean(false);
            boolean isRenamed  = f.path("renamed_file").asBoolean(false);
            String scmStatus = isNew ? ScmItem.Status.ADDED
                    : isDeleted  ? ScmItem.Status.REMOVED
                    : isRenamed  ? ScmItem.Status.RENAMED
                    : ScmItem.Status.MODIFIED;
            ScmItemAttribute attr = new ScmItemAttribute().withStatus(scmStatus);
            if (isRenamed) attr.withOldName(oldPath);
            result.add(new ScmItem(newPath, attr));
        }
        return result;
    }

    // ---- HTTP helper ----

    /** Performs a GET request using the supplied pre-configured builder. Returns null on non-200. */
    private static String get(String url, HttpRequest.Builder builder) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpResponse<String> response = client.send(
                builder.uri(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warning("PR API returned HTTP " + response.statusCode() + " for " + url);
            return null;
        }
        return response.body();
    }
}
