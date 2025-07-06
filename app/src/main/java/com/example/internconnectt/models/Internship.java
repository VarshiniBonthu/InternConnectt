package com.example.internconnectt.models;

import java.util.HashMap;
import java.util.Map;

public class Internship {
    private String title;
    private String description;
    private String deadline;
    private String link;
    private String branch;
    private boolean important;
    private String id;

    private String postedBy;

    public Internship() {
        // Required for Firebase
    }
    private Map<String, String> appliedStudents;

    public Internship(String title, String description, String branch, String link, String deadline, boolean important) {
        this.title = title;
        this.description = description;
        this.branch = branch;
        this.link = link;
        this.deadline = deadline;
        this.important = important;
        this.postedBy=postedBy;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getId() {
        return id;
    }
    public String getDeadline() {
        return deadline;
    }

    public String getLink() {
        return link;
    }

    public String getBranch() {
        return branch;
    }
    public String getPostedBy() {
        return postedBy;
    }
    public boolean isImportant() {
        return important;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }
    public Map<String, String> getAppliedStudents() {
        return appliedStudents;
    }

    public void setAppliedStudents(Map<String, String> appliedStudents) {
        this.appliedStudents = appliedStudents;
    }
}
