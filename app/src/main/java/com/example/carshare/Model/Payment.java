package com.example.carshare.Model;

public class Payment {
    private int id;
    private double amount;
    private double distance;
    private String date;
    private int userId;

    public Payment(int id, double amount, double distance, String date, int userId) {
        this.id = id;
        this.amount = amount;
        this.distance = distance;
        this.date = date;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public double getDistance() {
        return distance;
    }

    public String getDate() {
        return date;
    }

    public int getUserId() {
        return userId;
    }
}
