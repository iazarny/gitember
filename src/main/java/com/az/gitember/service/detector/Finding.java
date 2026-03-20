package com.az.gitember.service.detector;


import java.nio.file.Path;
import java.util.Objects;

/**
 * Represent source code scan finding.
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public class Finding {
    // sha of commit
    private String sha;
    private Path file;
    private int lineNo;
    private String type; // SECRET, TOKEN, DIRTY_WORD
    private String message;
    private Confidence confidence;
    private String line;
    private String matchedValue;

    public Finding(String sha, Path file, int lineNo, String keyMatch,
                   String message, Confidence confidence, String line, String matchedValue) {
        this.sha = sha;
        this.file = file;
        this.lineNo = lineNo;
        this.type = keyMatch;
        this.message = message;
        this.confidence = confidence;
        this.line = line;
        this.matchedValue = matchedValue;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public void setConfidence(Confidence confidence) {
        this.confidence = confidence;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getMatchedValue() {
        return matchedValue;
    }

    public void setMatchedValue(String matchedValue) {
        this.matchedValue = matchedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Finding finding = (Finding) o;
        return Objects.equals(file, finding.file) && Objects.equals(type, finding.type) && Objects.equals(matchedValue, finding.matchedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, type, matchedValue);
    }
}
