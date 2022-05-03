package com.techyu.LocationAttendance;

public class User {
    private String userID;
    private String userName;
    private String password;
    private String categories;

    User(String userID, String userName,String password, String categories) {
        this.userID = userID;
        this.userName = userName;
        this.password = password;
        this.categories = categories;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

}
