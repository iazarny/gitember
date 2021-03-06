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

    String APP_NAME = "Gitember 2.0";

    String ICON = "/icon/GE-icon.png";

}
