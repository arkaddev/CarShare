package com.example.carshare;

public class Ride {
    private int id;
    private String date;
    private int initialCounter;
    private int finalCounter;
    private int userId; // Dodane pole user_id

    public Ride(int id, String date, int initialCounter, int finalCounter, int userId) {
        this.id = id;
        this.date = date;
        this.initialCounter = initialCounter;
        this.finalCounter = finalCounter;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getInitialCounter() {
        return initialCounter;
    }

    public int getFinalCounter() {
        return finalCounter;
    }

    public int getUserId() {
        return userId;
    }

    public int getDistance() {
        return finalCounter - initialCounter;
    }
}