package com.az.gitember.data;

public class StatWPParameters {

    private String branchName;
    private int maxPeople;
    private int monthQty;
    private boolean perMonth = true;
    private boolean delta;
    private boolean workingHours;

    public StatWPParameters() {
    }

    public StatWPParameters(String branchName, int maxPeople, int monthQty, boolean perMonth, boolean delta, boolean workingHours) {
        this.branchName = branchName;
        this.maxPeople = maxPeople;
        this.monthQty = monthQty;
        this.perMonth = perMonth;
        this.delta = delta;
        this.workingHours = workingHours;
    }

    public boolean isWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(boolean workingHours) {
        this.workingHours = workingHours;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public int getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(int maxPeople) {
        this.maxPeople = maxPeople;
    }

    public int getMonthQty() {
        return monthQty;
    }

    public void setMonthQty(int monthQty) {
        this.monthQty = monthQty;
    }

    public boolean isPerMonth() {
        return perMonth;
    }

    public void setPerMonth(boolean perMonth) {
        this.perMonth = perMonth;
    }

    public boolean isDelta() {
        return delta;
    }

    public void setDelta(boolean delta) {
        this.delta = delta;
    }
}
