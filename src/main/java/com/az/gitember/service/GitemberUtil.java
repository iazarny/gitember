package com.az.gitember.service;

import com.az.gitember.data.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.revwalk.RevCommit;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GitemberUtil {

    private final static String OS = System.getProperty("os.name").toLowerCase();

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String formatDate(Date date) {
        return simpleDateFormat.format(date);
    }

    public static Date intToDate(int time) {
        return new Date(1000L * time);
    }


    public static String getLastPart(String input) {
        if (input == null || !input.contains("/")) {
            return input;
        }
        return input.substring(input.lastIndexOf('/') + 1);
    }


    public static IsClass is(String str) {
        return new IsClass(str);
    }

    public static class IsClass {
        private final String obj;
        public IsClass(String obj) {
            this.obj = obj;
        }
        public boolean oneOf(String... objes) {
            return Arrays.stream(objes).filter(s -> s.equals(obj)).findFirst().isPresent();
        }
    }

    public static ArrayList<String> getLines(final String content) {
        return (ArrayList<String>) new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }


    public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = null;
        privateStringField = obj.getClass().getDeclaredField(fieldName);
        privateStringField.setAccessible(true);
        return privateStringField.get(obj);
    }


    public static String getFolderName(String fullPath) {
        if (fullPath != null) {
            String tmp =  fullPath.replace(".git", "")
                    .replace("/", File.separator)
                    .replace("\\", File.separator);
            Path tmpPath = Path.of(tmp);
            return tmpPath.getNameCount() > 0 ? tmpPath.getName(tmpPath.getNameCount() - 1).toString() : "";
        }
        return "";
    }

    /**
     * Generates a hexadecimal encoded MD5 hash for the input String.
     * @param input The string to hash.
     * @return The 32-character hexadecimal MD5 hash.
     */
    public static String getMd5Hash(String input) {
        try {
            // Get the MD5 MessageDigest instance
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Digest the input bytes
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array into a signum representation (BigInteger)
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert the BigInteger into a hexadecimal string
            String hashtext = no.toString(16);

            // Pad the hash with leading zeros if needed to ensure 32 characters
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            // MD5 is guaranteed to be in the standard Java library, so this should not happen
            throw new RuntimeException(e);
        }
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static boolean isLinux() {
        return (OS.contains("linux"));
    }

    public static String getHomeFolder() {
        // get users home folder , with last path delimiter
        String home = System.getProperty("user.home");
        if (home != null && !home.endsWith(java.io.File.separator)) {
            home = home + java.io.File.separator;
        }
        return home;
    }

    public static boolean areBranchesEqualIgnoreOrder(List<ScmBranch> left, List<ScmBranch> right) {
        if (left == right) {
            return true;
        }

        if (left == null || right == null) {
            return false;
        }

        if (left.size() != right.size()) {
            return false;
        }

        List<ScmBranch> l1 = new ArrayList<>(left);
        List<ScmBranch> l2 = new ArrayList<>(right);

        Collections.sort(l1);
        Collections.sort(l2);

        return l1.equals(l2);
    }


    public static boolean areEqualIgnoreOrder(List<ScmItem> left, List<ScmItem> right) {
        if (left == right) {
            return true;
        }

        if (left == null || right == null) {
            return false;
        }

        if (left.size() != right.size()) {
            return false;
        }

        List<ScmItem> l1 = new ArrayList<>(left);
        List<ScmItem> l2 = new ArrayList<>(right);

        Collections.sort(l1);
        Collections.sort(l2);

        return l1.equals(l2);
    }



}
