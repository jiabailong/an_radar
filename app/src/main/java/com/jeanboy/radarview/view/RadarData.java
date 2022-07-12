package com.jeanboy.radarview.view;
public class RadarData {

    private String title;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String content;
    private double percentage;

    public RadarData(String title, double percentage) {
        this.title = title;
        this.percentage = percentage;
    }

    public String getTitle() {
        return title;
    }

    public double getPercentage() {
        return percentage;
    }
}
