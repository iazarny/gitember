package com.az.gitember.service;

import com.az.gitember.data.Project;
import com.az.gitember.data.Workspace;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Generates and caches a combined commit-activity chart for a {@link Workspace}.
 * <p>
 * Each contained project's activity chart (commits per day, last 30 days) is painted on top of
 * the others onto a single transparent image. Because the bars are semi-transparent and drawn
 * with {@code SrcOver}, days on which several projects were active render darker as their alpha
 * accumulates — visually surfacing the workspace's busiest days.
 *
 * @see ActivityChartService
 */
public class WorkspaceChartService {

    /** Cache key: "workspaceName|YYYY-MM-DD" — automatically stale on next day. */
    private static final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final Set<String> inFlight = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "workspace-chart");
        t.setDaemon(true);
        return t;
    });

    private WorkspaceChartService() {}

    private static String cacheKey(String name) {
        return name + "|" + LocalDate.now();
    }

    /**
     * Returns the cached combined chart for {@code workspace}, or {@code null} if not yet ready.
     * Schedules background generation on the first call for a given workspace/day; calls
     * {@code onReady} on the EDT when the image is available.
     */
    public static BufferedImage getOrSchedule(Workspace workspace, Runnable onReady) {
        String name = workspace.getName() != null ? workspace.getName() : "";
        String key  = cacheKey(name);
        BufferedImage cached = cache.get(key);
        if (cached != null) return cached;

        if (inFlight.add(name)) {
            // Snapshot the project folders now — the workspace may be mutated on the EDT later.
            java.util.List<String> folders = workspace.getProjects().stream()
                    .map(Project::getProjectHomeFolder)
                    .toList();
            executor.submit(() -> {
                try {
                    BufferedImage img = renderCombined(folders);
                    cache.put(key, img);
                    SwingUtilities.invokeLater(onReady);
                } catch (Exception ignored) {
                    // repos missing or unreadable — no chart shown
                } finally {
                    inFlight.remove(name);
                }
            });
        }
        return null;
    }

    private static BufferedImage renderCombined(java.util.List<String> projectFolders) {
        BufferedImage img = new BufferedImage(
                ActivityChartService.CHART_W, ActivityChartService.CHART_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, ActivityChartService.CHART_W, ActivityChartService.CHART_H);
        g.setComposite(AlphaComposite.SrcOver);

        // Paint every project's chart on top of the previous ones. Overlapping days get darker
        // because the semi-transparent bars accumulate alpha.
        for (String folder : projectFolders) {
            try {
                int[] counts = ActivityChartService.loadDailyCounts(folder);
                ActivityChartService.drawBars(g, counts);
            } catch (Exception ignored) {
                // skip a project that can't be read; keep the rest of the combined chart
            }
        }

        g.dispose();
        return img;
    }
}
