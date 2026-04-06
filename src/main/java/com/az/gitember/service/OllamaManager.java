package com.az.gitember.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for managing a local Ollama installation.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Detect whether Ollama is running (HTTP ping)</li>
 *   <li>Locate an existing Ollama binary in common paths</li>
 *   <li>Download the appropriate Ollama binary/archive for the current OS/arch</li>
 *   <li>Start the Ollama server and wait for it to become ready</li>
 *   <li>Check whether a model is locally available</li>
 *   <li>Pull (download) a model</li>
 * </ul>
 *
 * <p>Downloaded binaries are stored in {@code ~/.gitember/ollama/}.</p>
 */
public final class OllamaManager {

    private static final Logger log = Logger.getLogger(OllamaManager.class.getName());

    public static final int    PORT     = 11434;
    public static final String BASE_URL = "http://localhost:" + PORT;

    /** Directory where Gitember stores a locally-downloaded Ollama binary. */
    public static final Path OLLAMA_HOME =
            Paths.get(System.getProperty("user.home"), ".gitember", "ollama");

    // GitHub releases base (follows redirects to the latest CDN asset)
    private static final String GH_RELEASE =
            "https://github.com/ollama/ollama/releases/latest/download/";

    private OllamaManager() {}

    // =========================================================================
    //  Status
    // =========================================================================

    public enum Status { RUNNING, STOPPED, NOT_INSTALLED }

    /** Returns the current status of Ollama on this machine. */
    public static Status getStatus() {
        if (isRunning())   return Status.RUNNING;
        if (findBinary().isPresent()) return Status.STOPPED;
        return Status.NOT_INSTALLED;
    }

    /**
     * Returns {@code true} if the Ollama API is reachable on {@code localhost:11434}.
     * Uses a short timeout so it doesn't block the UI.
     */
    public static boolean isRunning() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tags"))
                    .timeout(Duration.ofSeconds(3))
                    .GET().build();
            HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Locates an Ollama binary, checking (in order):
     * <ol>
     *   <li>Gitember's own download directory {@link #OLLAMA_HOME}</li>
     *   <li>Common system-install paths ({@code /usr/local/bin}, {@code /opt/homebrew/bin}, …)</li>
     *   <li>Each directory on {@code $PATH}</li>
     *   <li>macOS {@code Ollama.app} in {@code ~/Applications} or {@code /Applications}</li>
     * </ol>
     */
    public static Optional<Path> findBinary() {
        // 1. Our own download
        Path local = localBinaryPath();
        if (Files.isExecutable(local)) return Optional.of(local);

        // 2. Well-known prefixes
        List<String> candidates = new ArrayList<>();
        candidates.add("/usr/local/bin/ollama");
        candidates.add("/opt/homebrew/bin/ollama");
        candidates.add("/usr/bin/ollama");
        candidates.add(System.getProperty("user.home") + "/.local/bin/ollama");

        // macOS Ollama.app locations
        if (Context.isMac()) {
            candidates.add("/Applications/Ollama.app/Contents/Resources/ollama");
            candidates.add(System.getProperty("user.home")
                    + "/Applications/Ollama.app/Contents/Resources/ollama");
        }

        // Windows
        if (Context.isWindows()) {
            candidates.add(System.getenv("LOCALAPPDATA") + "\\Programs\\Ollama\\ollama.exe");
            candidates.add(System.getProperty("user.home") + "\\AppData\\Local\\Programs\\Ollama\\ollama.exe");
        }

        for (String c : candidates) {
            if (c != null) {
                Path p = Paths.get(c);
                if (Files.isExecutable(p)) return Optional.of(p);
            }
        }

        // 3. PATH
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String binaryName = Context.isWindows() ? "ollama.exe" : "ollama";
            for (String dir : pathEnv.split(File.pathSeparator)) {
                Path p = Paths.get(dir, binaryName);
                if (Files.isExecutable(p)) return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    // =========================================================================
    //  Download
    // =========================================================================

    /**
     * Downloads the appropriate Ollama release for the current OS/architecture.
     *
     * @param onProgress callback receiving bytes downloaded so far (called periodically)
     * @param onTotal    callback receiving the total file size in bytes (called once,
     *                   may receive -1 if the server does not send {@code Content-Length})
     * @throws Exception on network or I/O failure
     */
    public static void download(LongConsumer onProgress, LongConsumer onTotal) throws Exception {
        Files.createDirectories(OLLAMA_HOME);
        String downloadUrl = resolveDownloadUrl();
        log.info("Downloading Ollama from: " + downloadUrl);

        // Follow redirects manually so we can read Content-Length after the final hop
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = openFollowingRedirects(url);
        long total = conn.getContentLengthLong();
        onTotal.accept(total);

        Path dest = OLLAMA_HOME.resolve(archiveFileName());
        try (InputStream in = conn.getInputStream();
             OutputStream out = Files.newOutputStream(dest)) {
            byte[] buf = new byte[64 * 1024];
            long downloaded = 0;
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
                downloaded += n;
                onProgress.accept(downloaded);
            }
        } finally {
            conn.disconnect();
        }

        // Extract if ZIP; mark executable if a raw binary
        if (archiveFileName().endsWith(".zip")) {
            extractZip(dest, OLLAMA_HOME);
            Files.deleteIfExists(dest);
        } else {
            // Raw binary — mark executable
            dest.toFile().setExecutable(true, false);
            // Rename to standard name
            Path binPath = localBinaryPath();
            if (!dest.equals(binPath)) {
                Files.move(dest, binPath, StandardCopyOption.REPLACE_EXISTING);
                binPath.toFile().setExecutable(true, false);
            }
        }
    }

    // =========================================================================
    //  Start server
    // =========================================================================

    /**
     * Starts the Ollama server in the background.
     *
     * @return the {@link Process} running {@code ollama serve}
     * @throws IOException          if the binary cannot be found or started
     * @throws IllegalStateException if already running
     */
    public static Process startServer() throws IOException {
        Path bin = findBinary()
                .orElseThrow(() -> new IOException("Ollama binary not found"));
        log.info("Starting Ollama: " + bin);
        ProcessBuilder pb = new ProcessBuilder(bin.toString(), "serve");
        pb.environment().put("OLLAMA_HOST", "127.0.0.1:" + PORT);
        pb.redirectErrorStream(true);
        return pb.start();
    }

    /**
     * Starts the Ollama server and blocks until the API becomes available or
     * the timeout elapses.
     *
     * @param timeoutMs maximum wait in milliseconds
     * @return the running {@link Process}
     * @throws Exception if the server does not start within the timeout
     */
    public static Process startServerAndWait(long timeoutMs) throws Exception {
        Process proc = startServer();
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isRunning()) return proc;
            //noinspection BusyWait
            Thread.sleep(500);
        }
        proc.destroyForcibly();
        throw new IOException("Ollama did not start within " + (timeoutMs / 1000) + " s");
    }

    // =========================================================================
    //  Model management
    // =========================================================================

    /**
     * Returns {@code true} if the named model is present in the local Ollama library.
     * Requires Ollama to be running.
     */
    public static boolean isModelAvailable(String modelName) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return false;
            // Simple substring check — avoids pulling in a JSON library
            String body = resp.body();
            // Normalise "model:tag" vs bare "model"
            String base = modelName.contains(":") ? modelName : modelName + ":";
            return body.contains("\"" + modelName + "\"")
                    || body.contains("\"" + base);
        } catch (Exception e) {
            log.log(Level.FINE, "isModelAvailable check failed", e);
            return false;
        }
    }

    /**
     * Starts an {@code ollama pull <modelName>} process whose stdout can be consumed
     * by the caller to display streaming progress.
     *
     * @param modelName model identifier (e.g. {@code "llama3.2"})
     * @return the running {@link Process}
     */
    public static Process startModelPull(String modelName) throws IOException {
        Path bin = findBinary()
                .orElseThrow(() -> new IOException("Ollama binary not found"));
        ProcessBuilder pb = new ProcessBuilder(bin.toString(), "pull", modelName);
        pb.redirectErrorStream(true);
        return pb.start();
    }

    // =========================================================================
    //  Platform helpers
    // =========================================================================

    /** Returns the local path where the ready-to-run binary should live. */
    public static Path localBinaryPath() {
        String name = Context.isWindows() ? "ollama.exe" : "ollama";
        if (Context.isMac()) {
            // After extracting Ollama.app zip, the CLI binary is here:
            return OLLAMA_HOME.resolve("Ollama.app")
                    .resolve("Contents").resolve("Resources").resolve("ollama");
        }
        return OLLAMA_HOME.resolve(name);
    }

    private static String resolveDownloadUrl() {
        if (Context.isMac()) {
            // Universal binary in the app bundle (works on both Intel and Apple Silicon)
            return GH_RELEASE + "Ollama-darwin.zip";
        }
        if (Context.isWindows()) {
            return GH_RELEASE + "ollama-windows-amd64.zip";
        }
        // Linux — pick arch
        String arch = System.getProperty("os.arch", "").toLowerCase();
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return GH_RELEASE + "ollama-linux-arm64";
        }
        return GH_RELEASE + "ollama-linux-amd64";
    }

    private static String archiveFileName() {
        if (Context.isMac())    return "Ollama-darwin.zip";
        if (Context.isWindows()) return "ollama-windows-amd64.zip";
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return (arch.contains("aarch64") || arch.contains("arm64"))
                ? "ollama-linux-arm64" : "ollama-linux-amd64";
    }

    /** Opens a URL, following HTTP redirects, and returns the final connection. */
    private static HttpURLConnection openFollowingRedirects(URL url) throws IOException {
        int maxHops = 10;
        while (maxHops-- > 0) {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(60_000);
            conn.setRequestProperty("User-Agent", "Gitember/1.0");
            int status = conn.getResponseCode();
            if (status >= 300 && status < 400) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                url = new URL(location);
            } else {
                return conn;
            }
        }
        throw new IOException("Too many redirects");
    }

    /** Extracts a ZIP archive into {@code destDir}. */
    private static void extractZip(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path target = destDir.resolve(entry.getName()).normalize();
                if (!target.startsWith(destDir))
                    throw new IOException("Zip slip detected: " + entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (OutputStream out = Files.newOutputStream(target)) {
                        zis.transferTo(out);
                    }
                    // Mark executables
                    if (entry.getName().endsWith("/ollama")
                            || entry.getName().equals("ollama")
                            || entry.getName().endsWith(".exe")) {
                        target.toFile().setExecutable(true, false);
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
