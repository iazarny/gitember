package com.az.gitember.service;

import com.az.gitember.misc.Pair;
import com.az.gitember.misc.Settings;
import com.az.gitember.misc.Triplet;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Igor_Azarny on 18 Dec 2016.
 */
public class SettingsServiceImpl {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger log = Logger.getLogger(SettingsServiceImpl.class.getName());

    private final static String PROP_FOLDER = ".gitember";
    private final static String PROP_FILE_NAME = "gitember.json";
    private final static String SYSTEM_PROP_USER_HOME = "user.home";
    private final static String SYSTEM_PROP_OS_NAME = "os.name";

    private final static String OS = System.getProperty(SYSTEM_PROP_OS_NAME).toLowerCase();


    /**
     * Get stored login name.
     *
     * @return login name if was saved before
     */
    public String getLastLoginName() {
        return read().getLastLoginName();
    }

    /**
     * Save given string as last use login name.
     *
     * @param login given login name
     */
    public void saveLastLoginName(final String login) {
        Settings settings = read();
        settings.setLastLoginName(login);
        save(settings);
    }


    /**
     * Get absolute path to last project.
     *
     * @return path to last project or null if no last projects.
     */
    public String getLastProject() {
        return read().getLastProject();
    }

    /**
     * Save given repository as last open repo.
     *
     * @param absPath given absolute path to repository
     */
    public void saveLastProject(final String absPath) {
        Settings settings = read();
        settings.setLastProject(absPath);
        Set<String> proj = new HashSet<>(settings.getProjects());
        proj.add(absPath);
        settings.getProjects().clear();
        settings.getProjects().addAll(proj);
        save(settings);
    }


    /**
     * Get home directory.
     *
     * @return home directory.
     */
    public String getUserHomeFolder() {
        return System.getProperty(SYSTEM_PROP_USER_HOME);
    }

    /**
     * Get absolute path path to property file with settings.
     *
     * @return
     * @throws IOException
     */
    private String getAbsolutePathToPropertyFile() throws IOException {
        return Files.createDirectories(
                Paths.get(System.getProperty(SYSTEM_PROP_USER_HOME), PROP_FOLDER)
        ).toFile().getAbsolutePath() + File.separator + PROP_FILE_NAME;
    }

    /**
     * Get list of opened repositories.
     *
     * @return list of repo
     */
    public List<Pair<String, String>> getRecentProjects() {
        List<Pair<String, String>> pairs = new ArrayList<>();
        Settings props = read();
        Set<String> keyToRemove = new HashSet<>();
        for (String folder : props.getProjects()) {
            Path path = Paths.get(folder);
            if (Files.exists(path)) {
                String shortName = new File(new File(folder).getParent()).getName();
                pairs.add(new Pair<>(shortName, folder));
            } else {
                keyToRemove.add(folder);
            }
        }
        keyToRemove.stream().forEach(k -> props.getProjects().remove(k));
        save(props);
        Collections.sort(pairs, (o1, o2) -> o1.getFirst().compareTo(o2.getFirst()));
        return pairs;
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    public static String getOS() {
        if (isWindows()) {
            return "win";
        } else if (isMac()) {
            return "osx";
        } else if (isUnix()) {
            return "uni";
        } else if (isSolaris()) {
            return "sol";
        } else {
            return "err";
        }
    }


    /**
     * Save properties.
     *
     * @param settings given properties.
     */
    public void save(final Settings settings) {
        try {
            objectMapper.writerFor(Settings.class).writeValue(
                    new File(getAbsolutePathToPropertyFile()),
                    settings
            );
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot save settings", e);
        }
    }


    /**
     * Read properties from disk.
     *
     * @return
     */
    public Settings read() {
        try {
            File file = new File(getAbsolutePathToPropertyFile());
            return objectMapper.readValue(file, Settings.class);
        } catch (IOException e) {
            return new Settings();
        }
    }

    public Triplet<String, String, String> getLoginAndPassword(String remoteUrl) {
        return read().getLoginPassword()
                .stream()
                .filter(t -> remoteUrl.equals(t.getFirst()))
                .findFirst().orElse(Triplet.of(null, null, null));
    }

    public void saveLoginAndPassword(String repoUrl, String login, String pwd) {
        Settings settings = read();
        Optional<Triplet<String, String, String>> tr = settings.getLoginPassword()
                .stream()
                .filter(t -> repoUrl.equals(t.getFirst()))
                .findFirst();
        if (tr.isPresent()) {
            Triplet<String, String, String> t = tr.get();
            t.setSecond(login);
            t.setThird(pwd);
        } else {
            settings.getLoginPassword().add(Triplet.of(repoUrl, login, pwd));
        }
        save(settings);

    }
}
