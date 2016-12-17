package com.az.gitember.scm.impl.git;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class GitUtil {

    public static Repository openRepository(final String path) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .readEnvironment() // scan environment GIT_* variables
                .setGitDir(new File(path))
                .findGitDir()
                .build();
    }


}
