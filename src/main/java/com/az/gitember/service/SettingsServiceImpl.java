package com.az.gitember.service;

import com.az.gitember.misc.*;
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

    private GitemberSettings gitemberSettings;






    /**
     * Get home directory.
     *
     * @return home directory.
     */
    public String getUserHomeFolder() {
        return System.getProperty(SYSTEM_PROP_USER_HOME);
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
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
//todo read gitemberSettings read mustlean up !!!!!
        List<Pair<String, String>> pairs = new ArrayList<>();
        for(GitemberProjectSettings ps : gitemberSettings.getProjects()) {
            String folder = ps.getProjectHameFolder();
            Path projectHome = Paths.get(folder);
            if (Files.exists(projectHome)) {
                String shortName = new File(new File(folder).getParent()).getName();
                pairs.add(new Pair<>(shortName, folder));
            }
        }
        Collections.sort(pairs, Comparator.comparing(Pair::getFirst));
        return pairs;
    }




    /**
     * Read properties from disk.
     *
     * @return
     */
    private GitemberSettings read() {
        //todo add get
        //todo clead up !!!!! and rett empty in case if absent
        try {
            File file = new File(getAbsolutePathToPropertyFile());
            return objectMapper.readValue(file, GitemberSettings.class);
        } catch (IOException e) {
            return new GitemberSettings();
        }
    }

    public RepoInfo getRepositoryCred(String remoteUrl) {

        // todo read if absent
        GitemberSettings gitemberSettingsAll = read();
        ArrayList<RepoInfo> infos = gitemberSettingsAll.getLoginPassword();

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
        GitemberSettings gitemberSettings = read();
        gitemberSettings.getLoginPassword().removeIf(t -> StringUtils.isBlank(t.getUrl()));
        gitemberSettings.getLoginPassword().removeIf(t -> repoInfo.equals(t.getUrl()));
        gitemberSettings.getLoginPassword().add(repoInfo);
        log.log(Level.INFO, "Save global gitemberSettings " + gitemberSettings);
        save(gitemberSettings);

    }



    /**
     * Save given repository as last open repo.
     *
     * @param absPath given absolute path to repository
     */
    public void saveLastProject(final String absPath) {
        GitemberSettings gitemberSettings = read();
        gitemberSettings.setLastProject(absPath);
        Set<String> proj = new HashSet<>(gitemberSettings.getProjects());
        proj.add(absPath);
        gitemberSettings.getProjects().clear();
        gitemberSettings.getProjects().addAll(proj);
        save(gitemberSettings);
    }

    /**
     * Save properties.
     *
     * @param gitemberSettings given properties.
     */
    public void save(final GitemberSettings gitemberSettings) {
        try {
            cleanupOldCommitMessages(gitemberSettings);
            objectMapper.writerFor(GitemberSettings.class).writeValue(
                    new File(getAbsolutePathToPropertyFile()),
                    gitemberSettings
            );
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot save gitemberSettings", e);
        }
    }


}
