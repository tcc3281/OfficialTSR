package com.example.officialtsr.models;

public class TrafficSign {
    private String name;
    private String description;
    private String imageLink;
    private String lawId;
    private String signName;
    private String type;
    private String label; // New variable

    public TrafficSign(String name, String description, String imageLink, String lawId, String signName, String type, String label) {
        this.name = name;
        this.description = description;
        this.imageLink = imageLink;
        this.lawId = lawId;
        this.signName = signName;
        this.type = type;
        this.label = label; // Initialize new variable
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
