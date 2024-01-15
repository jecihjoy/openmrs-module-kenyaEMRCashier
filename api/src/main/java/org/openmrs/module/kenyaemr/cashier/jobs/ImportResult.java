package org.openmrs.module.kenyaemr.cashier.jobs;

import java.util.List;

public class ImportResult {
    private int createdCount = 0;

    private String uploadSessionId;

    private boolean hasErrorFile = false;

    private boolean success;

    private List<String> errors;

    private Exception exception;

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;

    }

    public String getUploadSessionId() {
        return uploadSessionId;
    }

    public void setUploadSessionId(String uploadSessionId) {
        this.uploadSessionId = uploadSessionId;
    }

    public boolean isHasErrorFile() {
        return hasErrorFile;
    }

    public void setHasErrorFile(boolean hasErrorFile) {
        this.hasErrorFile = hasErrorFile;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
