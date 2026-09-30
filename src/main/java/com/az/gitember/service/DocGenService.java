package com.az.gitember.service;

import org.eclipse.jgit.api.GitCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible to generate vanilla git command.
 */
public class DocGenService {

    public DocGenService() {
    }

    public List<String> commandLine(GitCommand jgitCommand) {
        List<String> commandLine = new ArrayList<>();
        return  commandLine;
    }

}
