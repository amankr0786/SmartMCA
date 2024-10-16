package com.study.smartmca;

import java.util.List;

public class University {
    private String name;
    private List<String> subjects;

    // Default constructor required for calls to DataSnapshot.getValue(University.class)
    public University() { }

    public University(String name, List<String> subjects) {
        this.name = name;
        this.subjects = subjects;
    }

    public String getName() {
        return name;
    }

    public List<String> getSubjects() {
        return subjects;
    }
}
