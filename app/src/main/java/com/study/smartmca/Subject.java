package com.study.smartmca;

public class Subject {
    private String subjectName;
    private String subjectCode;
    private String iconUrl; // New field for the icon URL

    // Constructor
    public Subject() {
    }

    public Subject(String subjectName, String subjectCode, String iconUrl) {
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.iconUrl = iconUrl;
    }

    // Getters and setters
    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
