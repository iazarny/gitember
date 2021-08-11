package com.az.gitember.service;

import com.az.gitember.data.ScmItem;
import com.az.gitember.data.ScmItemAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GitLfsUtil {

    private final static Pattern lsfListLinePattern = Pattern.compile("(\\w+)\\s+([-|*])\\s+(.*)");

    /**
     * Parse the result of "git lfs ls-files". Example
     * 49261a14bb * file.psd      <-- file
     * 8a68e419c9 - file1111.bmp  <-- pointer
     * d4946ec4da - file2.bmp
     * @param str given raw result
     * @return list of lfs scm items
     */
    public static List<ScmItem> parseLfsFilesList(String str) {
        ArrayList<String> lines = GitemberUtil.getLines(str);
        List<ScmItem> rez = lines.stream()
                .map(s -> adaptLsfListLine(s))
                .collect(Collectors.toList());
        return rez;
    }

    static ScmItem adaptLsfListLine(String line) {
        String [] parts = parseLsfListLine(line);
        return new ScmItem(parts[2], new ScmItemAttribute()
                .withStatus(ScmItem.Status.LFS)
                .withSubStatus("*".equals(parts[1]) ? ScmItem.Status.LFS_FILE : ScmItem.Status.LFS_POINTER)
        );
    }

    static String [] parseLsfListLine(String str) {
        Matcher matcher = lsfListLinePattern.matcher(str);
        if (matcher.matches()) {
            String [] rez = new String[3];
            rez[0]=matcher.group(1);
            rez[1]=matcher.group(2);
            rez[2]=matcher.group(3);
            return rez;
        }
        return null;
    }

}
