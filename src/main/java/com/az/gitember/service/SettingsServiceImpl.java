package com.az.gitember.service;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.Pair;
import com.az.gitember.misc.RepoInfo;
import com.az.gitember.misc.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;

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

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private final static Logger log = Logger.getLogger(SettingsServiceImpl.class.getName());

    private final static String SYSTEM_PROP_USER_HOME = "user.home";
    private final static String SYSTEM_PROP_OS_NAME = "os.name";

    private final int COMMIT_HISTORY_LIMIT = 50;

    private final static String OS = System.getProperty(SYSTEM_PROP_OS_NAME).toLowerCase();

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
                Paths.get(System.getProperty(SYSTEM_PROP_USER_HOME), Const.PROP_FOLDER)
        ).toFile().getAbsolutePath() + File.separator + Const.PROP_FILE_NAME;
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
        Collections.sort(pairs, Comparator.comparing(Pair::getFirst));
        return pairs;
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }


    private void cleanupOldCommitMessages(Settings settings) {

        if (settings.getCommitMessages().size() > COMMIT_HISTORY_LIMIT) {

            settings.setCommitMessages(
                    new ArrayList<>(
                            settings.getCommitMessages().subList(
                                    settings.getCommitMessages().size() - COMMIT_HISTORY_LIMIT,
                                    settings.getCommitMessages().size())
                    )
            );

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

    public RepoInfo getRepositoryCred(String remoteUrl) {
        Settings settingsAll = read();
        ArrayList<RepoInfo> infos = settingsAll.getLoginPassword();

        Optional<RepoInfo> ri = infos
                .stream()
                .filter(  repoInfo -> StringUtils.equals(repoInfo.getUrl(), remoteUrl)   )
                .findFirst();
        if (ri.isPresent()) {
            return ri.get();
        } else {
            return RepoInfo.of(null, null, null, null, false);

        }
    }

    public void saseRepositoryCred(RepoInfo repoInfo) {
        Settings settings = read();
        settings.getLoginPassword().removeIf(t -> StringUtils.isBlank(t.getUrl()));
        settings.getLoginPassword().removeIf(t -> repoInfo.equals(t.getUrl()));
        settings.getLoginPassword().add(repoInfo);
        log.log(Level.INFO, "Save global settings " + settings);
        save(settings);

    }


    /**
     * Save properties.
     *
     * @param settings given properties.
     */
    public void save(final Settings settings) {
        try {
            cleanupOldCommitMessages(settings);
            objectMapper.writerFor(Settings.class).writeValue(
                    new File(getAbsolutePathToPropertyFile()),
                    settings
            );
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot save settings", e);
        }
    }


}
