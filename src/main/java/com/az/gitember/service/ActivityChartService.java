package com.az.gitember.service;

import com.az.gitember.data.Project;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Generates and caches per-project commit-activity bar charts (commits per day, last 30 days).
 * Charts are generated on background daemon threads and cached for the current calendar day.
 */
public class ActivityChartService {

    private static final int DAYS     = 30;
    static final int CHART_W = 300;
    static final int CHART_H = 60;

    /** Cache key: "projectPath|YYYY-MM-DD" — automatically stale on next day. */
    private static final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final Set<String> inFlight = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "activity-chart");
        t.setDaemon(true);
        return t;
    });

    private ActivityChartService() {}

    private static String cacheKey(String path) {
        return path + "|" + LocalDate.now();
    }

    /**
     * Returns the cached chart for {@code project}, or {@code null} if not yet ready.
     * Schedules background generation on the first call for a given project/day;
     * calls {@code onReady} on the EDT when the image is available.
     */
    public static BufferedImage getOrSchedule(Project project, Runnable onReady) {
        String folder = project.getProjectHomeFolder();
        String key    = cacheKey(folder);
        BufferedImage cached = cache.get(key);
        if (cached != null) return cached;

        if (inFlight.add(folder)) {
            executor.submit(() -> {
                try {
                    int[]       counts = loadDailyCounts(folder);
                    BufferedImage img  = renderChart(counts);
                    cache.put(key, img);
                    SwingUtilities.invokeLater(onReady);
                } catch (Exception ignored) {
                    // repo missing or unreadable — no chart shown
                } finally {
                    inFlight.remove(folder);
                }
            });
        }
        return null;
    }

    // ── git reading ──────────────────────────────────────────────────────────

    private static int[] loadDailyCounts(String projectFolder) throws Exception {
        File dotGit = new File(projectFolder, ".git");
        if (!dotGit.exists()) return new int[DAYS];

        File gitDir = dotGit.isFile() ? resolveGitFile(dotGit) : dotGit;

        try (Repository repo = new FileRepositoryBuilder()
                    .setGitDir(gitDir)
                    .build();
             Git git = new Git(repo)) {

            LocalDate today     = LocalDate.now();
            LocalDate firstDay  = today.minusDays(DAYS - 1);
            long sinceEpochSec  = firstDay.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            int[] counts = new int[DAYS];
            for (RevCommit commit : git.log().setMaxCount(2000).call()) {
                if (commit.getCommitTime() < sinceEpochSec) break;
                LocalDate d = Instant.ofEpochSecond(commit.getCommitTime())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                int idx = (int) ChronoUnit.DAYS.between(firstDay, d);
                if (idx >= 0 && idx < DAYS) counts[idx]++;
            }
            return counts;
        }
    }

    /** Handles linked-worktree .git files by resolving to the common git dir. */
    private static File resolveGitFile(File gitFile) throws Exception {
        String content = new String(Files.readAllBytes(gitFile.toPath())).trim();
        if (!content.startsWith("gitdir:")) return gitFile;

        String rel      = content.substring(7).trim();
        File   resolved = new File(rel);
        if (!resolved.isAbsolute()) resolved = new File(gitFile.getParentFile(), rel);
        resolved = resolved.getCanonicalFile();

        File commondirFile = new File(resolved, "commondir");
        if (commondirFile.exists()) {
            String commonRel = new String(Files.readAllBytes(commondirFile.toPath())).trim();
            File   commonDir = new File(commonRel);
            if (!commonDir.isAbsolute()) commonDir = new File(resolved, commonRel);
            return commonDir.getCanonicalFile();
        }
        return resolved;
    }

    // ── chart rendering ──────────────────────────────────────────────────────

    static BufferedImage renderChart(int[] counts) {
        BufferedImage img = new BufferedImage(CHART_W, CHART_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, CHART_W, CHART_H);
        g.setComposite(AlphaComposite.SrcOver);

        int max = Arrays.stream(counts).max().orElse(0);
        if (max > 0) {
            float barW = (float) CHART_W / DAYS;
            int   padV = 4;
            for (int i = 0; i < DAYS; i++) {
                if (counts[i] == 0) continue;
                int barH = Math.round((float) counts[i] / max * (CHART_H - padV * 2));
                int x    = Math.round(i * barW) + 1;
                int y    = CHART_H - padV - barH;
                int bw   = Math.max(1, Math.round(barW) - 2);
                g.setColor(new Color(0.2f, 0.5f, 1.0f, 0.350f));
                g.fillRoundRect(x, y, bw, barH, 3, 3);
            }
        }

        g.dispose();
        return img;
    }
}
