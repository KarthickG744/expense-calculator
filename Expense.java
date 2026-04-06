package com.example.expensecalculator;

public class Expense {
    public int id;
    public String date;
    public String name;
    public double amount;

    public Expense(int id, String date, String name, double amount) {
        this.id     = id;
        this.date   = date;
        this.name   = name;
        this.amount = amount;
    }
}
