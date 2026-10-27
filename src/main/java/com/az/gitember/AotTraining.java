package com.az.gitember;

import com.az.gitember.data.Project;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.mainframe.ReopenRepoHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Training run for the JVM AOT cache (JEP 483/514/515, plus AOT code from JEP 544 on JDK 28+).
 *
 * Two ways to get a cache:
 * <ul>
 *   <li>Packaging time (macOS, Linux): the installer scripts start the bundled runtime once with
 *       {@code -XX:AOTCacheOutput=gitember.aot ... --aot-train <repo>} and ship the cache in the
 *       app folder; launchers run with {@code -XX:AOTCache=$APPDIR/gitember.aot}.</li>
 *   <li>First start (Windows): the launcher runs with
 *       {@code -XX:AOTCache=$LOCALAPPDATA\Gitember\gitember.aot -Dgitember.aot.autotrain=true}.
 *       While the cache is missing, or was built for other jars / JVM options (an upgrade), the
 *       app starts a hidden training run of itself in the background; the next start uses the
 *       result. Training runs against the installed files, so the jar timestamps the JVM checks
 *       always match — Windows installers do not preserve the timestamps of a prebuilt cache.</li>
 * </ul>
 * In both cases the training run starts the app as usual, opens the given repository so the
 * working copy and history code paths get loaded and profiled, then exits; the JVM writes the
 * cache at exit.
 */
final class AotTraining {

    static final String ARG = "--aot-train";

    /** Opt-in for {@link #scheduleBackgroundTraining()}, set in the Windows launchers. */
    private static final String PROP_AUTOTRAIN = "gitember.aot.autotrain";
    /** Set on the background training process: build the UI but never show it. */
    private static final String PROP_HIDDEN = "gitember.aot.hidden";

    private static final String OPT_CACHE = "-XX:AOTCache=";

    private static final Logger log = Logger.getLogger(AotTraining.class.getName());

    /** How long the app keeps running after the repository has been requested. */
    private static final int RUN_MILLIS = 15_000;
    /** Background training waits for the user's own startup to settle. */
    private static final int BACKGROUND_DELAY_MILLIS = 20_000;
    /** A background training run that hangs is killed after this long. */
    private static final long BACKGROUND_TIMEOUT_MILLIS = 5 * 60_000;
    /** How long a closing app waits for a running background training to finish. */
    private static final long SHUTDOWN_WAIT_MILLIS = 60_000;

    private AotTraining() {
    }

    static boolean isRequested(String[] args) {
        return args.length > 0 && ARG.equals(args[0]);
    }

    /** True in the background training process, which must not show any window. */
    static boolean isHidden() {
        return Boolean.getBoolean(PROP_HIDDEN);
    }

    /** Called on the EDT once the main frame is built (and visible, unless {@link #isHidden()}). */
    static void start(MainFrame frame, String[] args) {
        if (args.length > 1 && new File(args[1], ".git").exists()) {
            String folder = new File(args[1]).getAbsolutePath();
            log.info("AOT training: opening " + folder);
            new ReopenRepoHandler(frame).accept(new Project(folder, new Date()));
        } else {
            log.info("AOT training: no repository given, training startup only");
        }
        Timer exitTimer = new Timer(RUN_MILLIS, e -> {
            log.info("AOT training: done, exiting");
            System.exit(0);
        });
        exitTimer.setRepeats(false);
        exitTimer.start();
    }

    /**
     * Starts a background training run when the launcher opted in and the cache it points
     * to is missing or stale. Does nothing otherwise, so it is safe to call on every start.
     */
    static void scheduleBackgroundTraining() {
        if (!Boolean.getBoolean(PROP_AUTOTRAIN)) {
            return;
        }
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        Path cache = jvmArgs.stream()
                .filter(a -> a.startsWith(OPT_CACHE))
                .map(a -> a.substring(OPT_CACHE.length()))
                .reduce((first, last) -> last)
                .filter(p -> !p.contains("$"))      // launcher could not expand the variable
                .map(p -> Path.of(p).toAbsolutePath())
                .orElse(null);
        if (cache == null) {
            log.fine("AOT autotrain: no usable " + OPT_CACHE + " option");
            return;
        }
        // -XX:AOTMode etc. would clash with -XX:AOTCacheOutput and do not change what is cached.
        List<String> trainingArgs = jvmArgs.stream()
                .filter(a -> !a.startsWith("-XX:AOT")
                        && !a.startsWith("-D" + PROP_AUTOTRAIN)
                        && !a.startsWith("-Djava.class.path="))
                .toList();
        String fingerprint = fingerprint(trainingArgs);
        Path stamp = sibling(cache, ".stamp");
        if (fingerprint.equals(readQuietly(stamp))) {
            return;     // trained (or failed to train) for exactly these jars and options
        }
        Thread t = new Thread(() -> train(cache, stamp, trainingArgs, fingerprint), "aot-training");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        Timer delay = new Timer(BACKGROUND_DELAY_MILLIS, e -> {
            t.start();
            // Closing the app right after startup would otherwise throw the finished cache away.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    t.join(SHUTDOWN_WAIT_MILLIS);
                } catch (InterruptedException ignored) {
                }
            }));
        });
        delay.setRepeats(false);
        delay.start();
    }

    private static void train(Path cache, Path stamp, List<String> jvmArgs, String fingerprint) {
        Path tmp = sibling(cache, ".tmp");
        Path home = null;
        try {
            Files.createDirectories(cache.getParent());
            try (FileChannel lockChannel = FileChannel.open(sibling(cache, ".lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock lock = lockChannel.tryLock()) {
                if (lock == null) {
                    log.info("AOT autotrain: another instance is training");
                    return;
                }
                Files.deleteIfExists(tmp);
                home = Files.createTempDirectory("gitember-aot-home");

                List<String> cmd = new ArrayList<>();
                cmd.add(javaExecutable());
                cmd.addAll(jvmArgs);
                cmd.add("-D" + PROP_HIDDEN + "=true");
                // Keeps the user's settings and recent list out of the training run.
                cmd.add("-Duser.home=" + home);
                cmd.add("-XX:AOTCacheOutput=" + tmp);
                cmd.add("-cp");
                cmd.add(System.getProperty("java.class.path"));
                cmd.add(App.class.getName());
                cmd.add(ARG);
                cmd.add(Objects.requireNonNullElse(trainingRepository(), "-"));

                log.info("AOT autotrain: building " + cache);
                Process p = new ProcessBuilder(cmd)
                        .redirectErrorStream(true)
                        .redirectOutput(sibling(cache, ".log").toFile())
                        .start();
                boolean finished = p.waitFor(BACKGROUND_TIMEOUT_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (!finished) {
                    p.destroyForcibly();
                }
                if (finished && p.exitValue() == 0 && Files.isRegularFile(tmp)) {
                    // Fails on Windows while this JVM has the old cache mapped; next start retries.
                    Files.move(tmp, cache, StandardCopyOption.REPLACE_EXISTING);
                    log.info("AOT autotrain: cache ready, used from the next start");
                } else {
                    log.warning("AOT autotrain: training failed, see " + sibling(cache, ".log"));
                }
                // Also written after a failure, so a broken setup does not retrain on every start.
                Files.writeString(stamp, fingerprint, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "AOT autotrain: cannot build " + cache, e);
        } finally {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
            }
            deleteQuietly(home);
        }
    }

    /** The open repository, else any recent one: training without a repository misses JGit. */
    private static String trainingRepository() {
        Project active = Context.getActiveProject();
        if (active != null) {
            return active.getProjectHomeFolder();
        }
        if (Context.getSettings() == null) {
            return null;
        }
        return Context.getSettings().getProjects().stream()
                .map(Project::getProjectHomeFolder)
                .filter(f -> f != null && new File(f, ".git").exists())
                .findFirst()
                .orElse(null);
    }

    /** javaw on Windows: java.exe started from a GUI process would open a console window. */
    private static String javaExecutable() {
        Path bin = Path.of(System.getProperty("java.home"), "bin");
        for (String name : new String[]{"javaw.exe", "java.exe", "java"}) {
            if (Files.isRegularFile(bin.resolve(name))) {
                return bin.resolve(name).toString();
            }
        }
        throw new IllegalStateException("no java executable in " + bin);
    }

    /**
     * What the cache depends on: the JVM build, its options (the GC must match) and the
     * class path jars, by size and timestamp — the same things the JVM validates.
     */
    private static String fingerprint(List<String> jvmArgs) {
        StringBuilder sb = new StringBuilder(System.getProperty("java.vm.version"));
        jvmArgs.forEach(a -> sb.append('\n').append(a));
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            File f = new File(entry);
            sb.append('\n').append(f.getAbsolutePath())
                    .append(' ').append(f.length())
                    .append(' ').append(f.lastModified());
        }
        return sb.toString();
    }

    private static Path sibling(Path file, String suffix) {
        return file.resolveSibling(file.getFileName() + suffix);
    }

    private static String readQuietly(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static void deleteQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignored) {
        }
    }
}
