package com.az.gitember.service;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Igor_Azarny on 18 Dec 2016.
 */
public class SettingsServiceImpl {

    final static String  PROP_FOLDER = ".gitember";
    final static String  PROP_FILE_NAME = "gitember.properties";
    final static String  KEY_LAST_PROJECT = "lastProject";
    final static String  KEY_HISTORY = "history";
    final static String  KEY_LOGIN = "login";
    final static String  SYSTEM_PROP_USER_HOME = "user.home";

    public void saveRepository(String absPath)  {

        String shortName =  new File(new File(absPath).getParent()).getName();

        Properties properties = read();

        properties.put(shortName, absPath);
        properties.put(KEY_LAST_PROJECT, shortName);

        save(properties);
    }


    public String getLogin() {
        return  read().getProperty(KEY_LOGIN);
    }

    public void saveLogin(String login) {
        Properties properties = read();
        properties.put(KEY_LOGIN, login);
        save(properties);
    }

    /**
     * Get absolute path to last project.
     *
     * @return path to last project or null if no last projects.
     */
    public String getLastProject()  {

        Properties properties = read();

        if (properties.getProperty(KEY_LAST_PROJECT) != null) {
            return properties.getProperty(properties.getProperty(KEY_LAST_PROJECT));
        }

        return null;

    }

    void save(Properties properties) {
        try(OutputStream os = new FileOutputStream(getAbsolutePathToPropertyFile())) {
            properties.store(os, "Gitember settings " + new Date());
        } catch (IOException e) {}
    }


    private Properties read()  {
        Properties prop = new Properties();
        try (InputStream is = new FileInputStream(getAbsolutePathToPropertyFile())) {
            prop.load(is);
        } catch (IOException e) {}
        return prop;
    }

    public String getUserHomeFolder() {
        return System.getProperty(SYSTEM_PROP_USER_HOME);
    }

    private String getAbsolutePathToPropertyFile() throws IOException {
        return Files.createDirectories(
                Paths.get(System.getProperty(SYSTEM_PROP_USER_HOME), PROP_FOLDER)
        ).toFile().getAbsolutePath() + File.separator + PROP_FILE_NAME;
    }

    public List<Pair<String, String>> getRecentProjects() throws IOException {
        List<Pair<String, String>>  pairs = new ArrayList<>();
        Properties props = read();
        for(Object key : props.keySet()) {
            if ( ((String)props.get(key)).endsWith(Const.GIT_FOLDER)) {
                pairs.add(
                        new Pair<>(
                                (String) key,
                                (String) props.get(key)
                        )
                );
            }
        }
        Collections.sort(pairs, (o1, o2) -> o1.getFirst().compareTo(o2.getFirst()));
        return pairs;
    }
}
