package com.iflytek.voicedemo;

import java.io.Serializable;

public class Scenario implements Serializable {
    private String id;
    private String title;
    private String description;
    private String iconName; // drawable resource name
    private String systemPrompt;
    private String welcomeMessage;

    public Scenario(String id, String title, String description, String iconName, String systemPrompt, String welcomeMessage) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.systemPrompt = systemPrompt;
        this.welcomeMessage = welcomeMessage;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIconName() { return iconName; }
    public String getSystemPrompt() { return systemPrompt; }
    public String getWelcomeMessage() { return welcomeMessage; }
}
