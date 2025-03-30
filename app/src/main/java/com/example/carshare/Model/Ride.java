package com.example.carshare.Model;

public class Ride {
    private int id;
    private String date;
    private int initialCounter;
    private int finalCounter;
    private int userId;

    private int archive;
    private int correct;

    public Ride(int id, String date, int initialCounter, int finalCounter, int userId, int archive, int correct) {
        this.id = id;
        this.date = date;
        this.initialCounter = initialCounter;
        this.finalCounter = finalCounter;
        this.userId = userId;
        this.archive = archive;
        this.correct = correct;
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

    public int getArchive() {
        return archive;
    }

    public int getCorrect() {
        return correct;
    }

    public int getDistance() {
        return finalCounter - initialCounter;
    }

    public double getTotalCost(){
        double fuelPrice = 3;
        double petrolConsumption = 10;
        double totalCost = ((petrolConsumption * getDistance()) / 100) * fuelPrice;
        return Math.round(totalCost * 100.0) / 100.0;  // Zaokrąglenie do 2 miejsc po przecinku

    }
}