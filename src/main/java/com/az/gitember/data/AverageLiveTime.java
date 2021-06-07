package com.az.gitember.data;

import java.util.Date;

public class AverageLiveTime {

    private String date;
    private int qty;
    private int hrs;

    public AverageLiveTime(String date, int qty, int hrs) {
        this.date = date;
        this.qty = qty;
        this.hrs = hrs;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getHrs() {
        return hrs;
    }

    public void setHrs(int hrs) {
        this.hrs = hrs;
    }
}
