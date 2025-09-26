package com.example.forexeventalarm.model;

public class NewsEvents {
    private int id;
    private String title;
    private String date;   // Example: "Sun, Sep 21"
    private String time;   // Example: "6:00 PM"

    public NewsEvents(int id, String title, String date, String time) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}

