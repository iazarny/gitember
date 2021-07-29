package com.az.gitember.data;

public interface Const {

    String TEMP_FILE_PREFIX = "gtmbr";
    String GIT_FOLDER = ".git";

    String PROP_FOLDER = ".gitember";
    String PROP_FILE_NAME = "gitember2.json";

    int SEARCH_LIMIT_CHAR = 2;
    String DIFF_EXTENSION = "diff";


    interface Config {
        String HOME = "home";
        String HTTP = "http";
        String HTTPS = "https";
        String GIT = "git@";
        String SSH = "ssh:";
        String SLL_VERIFY = "sslVerify";
    }

    interface View {
        String BRANCH_DIFF = "branchdiff";
        String FILE_DIFF = "diffviewer";
        String EDITOR = "editor";
        String FILE_HISTORY = "filehistory";
        String HISTORY = "history";
        String HISTORY_DETAIL = "historydetail";
        String MAIN = "main";

        String STASH = "stash";
        String STAT = "stat";
        String STAT_WORK_PROGRESS = "statworkingprogress";
        String STAT_BRANCH_LIFETOME = "statbranchlifetime";
        String STAT_BRANCHES = "statbranches";
        String WORKING_COPY = "workingcopy";
    }

    String APP_NAME = "Gitember 2.2";

    String ICON = "/icon/GE-icon.png";

}
