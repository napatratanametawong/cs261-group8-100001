package com.example.lc2_booking_room.service.login;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpStore {
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final Set<String> passedTuCheckEmails = ConcurrentHashMap.newKeySet();

    // --- Save OTP ---
    public void save(String email, String otp, long ttlSeconds) {
        String key = normalizeKey(email);
        long now = Instant.now().getEpochSecond();
        long expiresAt = now + ttlSeconds;

        String hash = otp; // (ถ้าต้องการใส่ hash จริง เปลี่ยนตรงนี้)
        store.put(key, new OtpEntry(hash, expiresAt, now, 0));
    }

    // --- Limit request frequency ---
    public boolean canRequest(String email, long now, long cooldown) {
        String key = normalizeKey(email);
        OtpEntry e = store.get(key);
        if (e == null)
            return true;
        return now - e.getLastRequestedEpoch() >= cooldown;
    }

    // --- Verify OTP ---
    public boolean verifyAndConsume(String email, String otp) {
        String key = normalizeKey(email);
        OtpEntry e = store.get(key);
        if (e == null)
            return false;

        long now = Instant.now().getEpochSecond();

        if (now > e.getExpiresAtEpoch()) {
            store.remove(key);
            return false; // หมดอายุ
        }
        if (!e.getOtpHash().equals(otp)) {
            e.setAttempts(e.getAttempts() + 1);
            return false; // ผิด
        }
        store.remove(key); // ใช้แล้วลบทิ้ง
        return true;
    }

    // --- TU check pass memory ---
    public void markTuCheckPassed(String email) {
        passedTuCheckEmails.add(normalizeKey(email));
    }

    public boolean isTuCheckPassed(String email) {
        return passedTuCheckEmails.contains(normalizeKey(email));
    }

    private String normalizeKey(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    // === INNER CLASS ===
    public static class OtpEntry {
        private String otpHash;
        private long expiresAtEpoch;
        private long lastRequestedEpoch;
        private int attempts;

        public OtpEntry() {
        }

        public OtpEntry(String otpHash, long expiresAtEpoch, long lastRequestedEpoch, int attempts) {
            this.otpHash = otpHash;
            this.expiresAtEpoch = expiresAtEpoch;
            this.lastRequestedEpoch = lastRequestedEpoch;
            this.attempts = attempts;
        }

        public String getOtpHash() {
            return otpHash;
        }

        public void setOtpHash(String otpHash) {
            this.otpHash = otpHash;
        }

        public long getExpiresAtEpoch() {
            return expiresAtEpoch;
        }

        public void setExpiresAtEpoch(long expiresAtEpoch) {
            this.expiresAtEpoch = expiresAtEpoch;
        }

        public long getLastRequestedEpoch() {
            return lastRequestedEpoch;
        }

        public void setLastRequestedEpoch(long lastRequestedEpoch) {
            this.lastRequestedEpoch = lastRequestedEpoch;
        }

        public int getAttempts() {
            return attempts;
        }

        public void setAttempts(int attempts) {
            this.attempts = attempts;
        }
    }

    private final Map<String, String> usernameMap = new ConcurrentHashMap<>();

    public void setUsernameFor(String email, String username) {
        usernameMap.put(email.toLowerCase(), username);
    }

    public String getUsernameFor(String email) {
        return usernameMap.get(email.toLowerCase());
    }

}
