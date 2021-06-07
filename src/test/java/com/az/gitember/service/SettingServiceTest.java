package com.az.gitember.service;

import com.az.gitember.data.Project;
import com.az.gitember.data.Settings;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SettingServiceTest {

    private SettingService settingService = new SettingService() {

        File temp = null;

        @Override
        protected File getAbsolutePathToPropertyFile() throws IOException {
            if (temp == null) {
                temp = File.createTempFile("settings", "json");
            }
            return temp;
        }
    };

    @Test
    void readWrite() {

        Settings settings = settingService.read();

        assertNotNull(settings);

        assertEquals(0, settings.getProjects().size());

        settings.getProjects().add(new Project("~/somefolder", new Date()));

        settingService.write(settings);

        settings = settingService.read();

        assertEquals(1, settings.getProjects().size());

        assertEquals("~/somefolder", settings.getProjects().first().getProjectHomeFolder());

    }

}