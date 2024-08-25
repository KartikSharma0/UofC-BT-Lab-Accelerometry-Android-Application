package com.example.metawearproject;

public class PatientData {
    // The patient data class will be the format for collected data
    private int time;   // Time data sample was recorded
    private double leftAccel;   // Value of accelerometer data for left arm during the time-stamp
    private double rightAccel;  // Value of accelerometer data for right arm during the time-stamp

    public PatientData(int time, double leftAccel, double rightAccel){
        this.time = time;
        this.leftAccel = leftAccel;
        this.rightAccel = rightAccel;
    }

    // Getters and setters:
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getLeftAccel() {
        return leftAccel;
    }

    public void setLeftAccel(double leftAccel) {
        this.leftAccel = leftAccel;
    }

    public double getRightAccel() {
        return rightAccel;
    }

    public void setRightAccel(double rightAccel) {
        this.rightAccel = rightAccel;
    }
}
