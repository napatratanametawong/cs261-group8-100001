package com.example.lc2_booking_room.dto.login;

public class TokenResponse {
    private String token;
    private String tokenType = "Bearer";
    private String role;
    private String username;     // เก็บไว้ใน JWT ด้วยได้
    private UserProfile profile; // << เพิ่ม

    public TokenResponse() {}
    public TokenResponse(String token, String role, String username, UserProfile profile) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.profile = profile;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UserProfile getProfile() { return profile; }
    public void setProfile(UserProfile profile) { this.profile = profile; }
}
