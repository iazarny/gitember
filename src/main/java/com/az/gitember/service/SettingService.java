package com.az.gitember.service;

import com.az.gitember.data.Const;
import com.az.gitember.data.Settings;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingService {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final static Logger log = Logger.getLogger(SettingService.class.getName());

    private final static String SYSTEM_PROP_USER_HOME = "user.home";
    private final static int COMMIT_HISTORY_LIMIT = 50;





    /**
     * Read properties from disk.
     *
     * @return
     */
    public Settings read() {
        Settings settings = null;
        try {
            settings =  read(getAbsolutePathToPropertyFile());
            if (settings == null) {
                settings = new Settings();
            }
        } catch (Exception e) {
            settings = new Settings();
        }
        return settings;

    }

    /**
     * Read properties from disk.
     *
     * @return settings
     */
    Settings read(File file) throws IOException {
        return objectMapper.readValue(file, Settings.class);
    }


    /**
     * Save properties.
     */
    public void write(Settings settings) {
        try {
            createStorageIsAbsent();
            objectMapper.writerFor(Settings.class).writeValue(
                    getAbsolutePathToPropertyFile(),  settings );
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot save settings", e);
        }
    }

    /**
     * Create storage folder.
     * @throws IOException in case if error
     */
    private void createStorageIsAbsent() throws IOException {
        Path path = Paths.get(System.getProperty(SYSTEM_PROP_USER_HOME), Const.PROP_FOLDER);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
    }

    /**
     * Get absolute path path to property file with settings.
     *
     * @return path
     * @throws IOException in case of errors.
     */
    protected File getAbsolutePathToPropertyFile() throws IOException {
        return  Paths.get(System.getProperty(SYSTEM_PROP_USER_HOME),
                Const.PROP_FOLDER,  Const.PROP_FILE_NAME).toFile();
    }



}
