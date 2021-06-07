package com.az.gitember.data;

/**
 * Entry to hold keywords description per language.
 */
public class LangDefinition {
    private String name;
    private String [] extension;
    private String [] keywords;
    private boolean comments = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getExtension() {
        return extension;
    }

    public void setExtension(String[] extension) {
        this.extension = extension;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public boolean isComments() {
        return comments;
    }

    public void setComments(boolean comments) {
        this.comments = comments;
    }
}
