package com.example.lc2_booking_room.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpStore {
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    // --- Save OTP ---
    public void save(String email, String otp, long ttlSeconds) {
        long now = Instant.now().getEpochSecond();
        long expiresAt = now + ttlSeconds;

        String hash = otp; // (จะใส่การ hash จริงก็ได้)
        store.put(email, new OtpEntry(hash, expiresAt, now, 0));
    }

    // --- Limit request frequency ---
    public boolean canRequest(String email, long now, long cooldown) {
        OtpEntry e = store.get(email);
        if (e == null) return true;
        return now - e.getLastRequestedEpoch() >= cooldown;
    }

    // --- Verify OTP ---
    public boolean verifyAndConsume(String email, String otp) {
        OtpEntry e = store.get(email);
        if (e == null) return false;
        long now = Instant.now().getEpochSecond();

        if (now > e.getExpiresAtEpoch()) {
            store.remove(email);
            return false; // หมดอายุ
        }
        if (!e.getOtpHash().equals(otp)) {
            e.setAttempts(e.getAttempts() + 1);
            return false; // ผิด
        }
        store.remove(email); // ใช้แล้วลบทิ้ง
        return true;
    }

    // === INNER CLASS ===
    public static class OtpEntry {
        private String otpHash;
        private long expiresAtEpoch;
        private long lastRequestedEpoch;
        private int attempts;

        public OtpEntry() {}

        public OtpEntry(String otpHash, long expiresAtEpoch, long lastRequestedEpoch, int attempts) {
            this.otpHash = otpHash;
            this.expiresAtEpoch = expiresAtEpoch;
            this.lastRequestedEpoch = lastRequestedEpoch;
            this.attempts = attempts;
        }

        public String getOtpHash() { return otpHash; }
        public void setOtpHash(String otpHash) { this.otpHash = otpHash; }

        public long getExpiresAtEpoch() { return expiresAtEpoch; }
        public void setExpiresAtEpoch(long expiresAtEpoch) { this.expiresAtEpoch = expiresAtEpoch; }

        public long getLastRequestedEpoch() { return lastRequestedEpoch; }
        public void setLastRequestedEpoch(long lastRequestedEpoch) { this.lastRequestedEpoch = lastRequestedEpoch; }

        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }
    }
}
