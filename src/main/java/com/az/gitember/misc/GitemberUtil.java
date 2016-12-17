package com.az.gitember.misc;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Date;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016.
 */
public class GitemberUtil {

    public static Date intToDate(int time) {
        return new Date(1000L * time);
    }

    public static boolean isBinaryFile(final File f) throws IOException {
        String type = Files.probeContentType(f.toPath());
        if (type == null) {
            return true;
        } else if (type.startsWith("text")) {
            return false;
        } else {
            //type isn't text
            return true;
        }
    }

    @NotNull
    public static String getExtension(String fileName) {
        char ch;
        int len;
        if(fileName==null ||
                (len = fileName.length())==0 ||
                (ch = fileName.charAt(len-1))=='/' || ch=='\\' || //in the case of a directory
                ch=='.' ) //in the case of . or ..
            return "";
        int dotInd = fileName.lastIndexOf('.'),
                sepInd = Math.max(fileName.lastIndexOf(File.separatorChar), fileName.lastIndexOf('\\'));
        if( dotInd<=sepInd )
            return "";
        else
            return fileName.substring(dotInd+1).toLowerCase();
    }





}
