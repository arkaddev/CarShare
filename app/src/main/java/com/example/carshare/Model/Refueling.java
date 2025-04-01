package com.example.carshare.Model;

public class Refueling {

    private int id;
    private double amountRefueling;
    private double pricePerLiter;
    private String dateRefueling;
    private int userId;

    public Refueling(int id, String dateRefueling, double pricePerLiter, double amountRefueling, int userId) {
        this.userId = userId;
        this.dateRefueling = dateRefueling;
        this.pricePerLiter = pricePerLiter;
        this.amountRefueling = amountRefueling;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getAmountRefueling() {
        return amountRefueling;
    }

    public double getPricePerLiter() {
        return pricePerLiter;
    }

    public String getDateRefueling() {
        return dateRefueling;
    }

    public int getUserId() {
        return userId;
    }
}
