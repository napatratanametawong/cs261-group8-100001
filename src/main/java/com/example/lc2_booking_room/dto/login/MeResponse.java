package com.example.lc2_booking_room.dto.login;

public class MeResponse {
    private String email;     
    private String role;      
    private String username;  
    private UserProfile profile;

    public MeResponse() {}
    public MeResponse(String email, String role, String username, UserProfile profile) {
        this.email = email;
        this.role = role;
        this.username = username;
        this.profile = profile;
    }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
    public UserProfile getProfile() { return profile; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setUsername(String username) { this.username = username; }
    public void setProfile(UserProfile profile) { this.profile = profile; }
}
