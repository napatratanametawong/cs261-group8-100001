package com.example.lc2_booking_room.dto;

public class UserProfile {
    private String userName;
    private String displayname_th;
    private String email;
    private String department;
    private String faculty;

    public UserProfile() {}
    public UserProfile(String userName, String displayname_th, String email, String department, String faculty) {
        this.userName = userName;
        this.displayname_th = displayname_th;
        this.email = email;
        this.department = department;
        this.faculty = faculty;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDisplayname_th() { return displayname_th; }
    public void setDisplayname_th(String displayname_th) { this.displayname_th = displayname_th; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
}
