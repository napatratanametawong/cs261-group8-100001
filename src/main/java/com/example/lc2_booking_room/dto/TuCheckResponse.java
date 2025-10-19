package com.example.lc2_booking_room.dto;

public class TuCheckResponse {
    private boolean valid;
    private String message;

    public TuCheckResponse() {}
    public TuCheckResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static TuCheckResponse valid() { return new TuCheckResponse(true, "MATCH"); }
    public static TuCheckResponse invalid(String msg) { return new TuCheckResponse(false, msg); }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
