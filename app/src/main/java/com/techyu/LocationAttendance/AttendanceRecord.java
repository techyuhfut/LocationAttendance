package com.techyu.LocationAttendance;

public class AttendanceRecord {
    private String userID;
    private String userName;
    private String checkState;
    private String date;
    private String checkTime;
    private String checklatiude;
    private String checklongtidue;
    private String checkaddr;

    public AttendanceRecord(String userID, String userName, String checkState, String date, String checkTime, String checklatiude, String checklongtidue, String checkaddr) {
        this.userID = userID;
        this.userName = userName;
        this.checkState = checkState;
        this.date = date;
        this.checkTime = checkTime;
        this.checklatiude = checklatiude;
        this.checklongtidue = checklongtidue;
        this.checkaddr = checkaddr;
    }

    public AttendanceRecord() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public String getChecklatiude() {
        return checklatiude;
    }

    public void setChecklatiude(String checklatiude) {
        this.checklatiude = checklatiude;
    }

    public String getChecklongtidue() {
        return checklongtidue;
    }

    public void setChecklongtidue(String checklongtidue) {
        this.checklongtidue = checklongtidue;
    }

    public String getCheckaddr() {
        return checkaddr;
    }

    public void setCheckaddr(String checkaddr) {
        this.checkaddr = checkaddr;
    }

    public String getCheckState() {
        return checkState;
    }

    public void setCheckState(String checkState) {
        this.checkState = checkState;
    }
}
