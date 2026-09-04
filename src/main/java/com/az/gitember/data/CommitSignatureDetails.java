package com.az.gitember.data;

/**
 * Result of an on-demand commit-signature verification, shown in the signature dialog.
 */
public class CommitSignatureDetails {

    private SignatureStatus status = SignatureStatus.UNSIGNED;
    private String format;
    private String signer;
    private String keyFingerprint;
    private String trust;
    private String allowedSignersPath;
    private String message;

    public SignatureStatus getStatus() {
        return status;
    }

    public void setStatus(SignatureStatus status) {
        this.status = status != null ? status : SignatureStatus.UNSIGNED;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getKeyFingerprint() {
        return keyFingerprint;
    }

    public void setKeyFingerprint(String keyFingerprint) {
        this.keyFingerprint = keyFingerprint;
    }

    public String getTrust() {
        return trust;
    }

    public void setTrust(String trust) {
        this.trust = trust;
    }

    public String getAllowedSignersPath() {
        return allowedSignersPath;
    }

    public void setAllowedSignersPath(String allowedSignersPath) {
        this.allowedSignersPath = allowedSignersPath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
