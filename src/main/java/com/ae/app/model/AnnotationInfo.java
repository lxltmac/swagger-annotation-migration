package com.ae.app.model;

import java.util.HashMap;
import java.util.Map;

public class AnnotationInfo {
    public AnnotationInfo() {
        oldAnnotationParameters = new HashMap<>();
        newAnnotationParameters = new HashMap<>();
    }

    private int line;
    private String oldString;
    private String newString;
    private Map<String, String> oldAnnotationParameters;
    private Map<String, String> newAnnotationParameters;

    public String getErrorMessage() {
        return errorMessage;
    }

    public AnnotationInfo setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    private String errorMessage;

    public int getLine() {
        return line;
    }

    public AnnotationInfo setLine(int line) {
        this.line = line;
        return this;
    }

    public String getOldString() {
        return oldString;
    }

    public AnnotationInfo setOldString(String oldString) {
        this.oldString = oldString;
        return this;
    }

    public String getNewString() {
        return newString;
    }

    public AnnotationInfo setNewString(String newString) {
        this.newString = newString;
        return this;
    }


    public Map<String, String> getOldAnnotationParameters() {
        return oldAnnotationParameters;
    }

    public AnnotationInfo setOldAnnotationParameters(Map<String, String> oldAnnotationParameters) {
        this.oldAnnotationParameters = oldAnnotationParameters;
        return this;
    }

    public Map<String, String> getNewAnnotationParameters() {
        return newAnnotationParameters;
    }

    public AnnotationInfo setNewAnnotationParameters(Map<String, String> newAnnotationParameters) {
        this.newAnnotationParameters = newAnnotationParameters;
        return this;
    }

}
