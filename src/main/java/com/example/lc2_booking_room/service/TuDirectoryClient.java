package com.example.lc2_booking_room.service;

import com.example.lc2_booking_room.dto.UserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Component
public class TuDirectoryClient {

    private final RestTemplate restTemplate;

    @Value("${app.tu.base-url}")
    private String baseUrl; // e.g. https://restapi.tu.ac.th/api/

    // ใช้ Application-Key (จากที่คุณเทสแล้วเวิร์ค)
    @Value("${app.tu.application-key:}")
    private String applicationKey;

    // (รองรับ Bearer ถ้าจำเป็นในอนาคต)
    @Value("${app.tu.bearer-token:}")
    private String bearerToken;

    public TuDirectoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** ดึงอีเมลทั้งหมดของนิสิตจาก userName (ใช้สำหรับ /auth/tucheck เทียบกับอีเมลที่ผู้ใช้กรอก) */
    public Set<String> findStudentEmails(String userName) {
        Set<String> emails = new HashSet<>();
        JsonNode data = callStdProfileByUsername(userName);
        if (data == null) return emails;

        // รองรับหลายรูปแบบคีย์
        addIfText(emails, data, "email");            // ทั่วไป
        addIfText(emails, data, "studentEmail");     // บางระบบ
        addIfText(emails, data, "student_email");
        addIfText(emails, data, "mail");             // สำรอง

        // เผื่อมี array emails[]
        JsonNode arr = data.get("emails");
        if (arr != null && arr.isArray()) {
            for (JsonNode n : arr) {
                if (n != null && n.isTextual() && !n.asText().isBlank()) {
                    emails.add(n.asText());
                }
            }
        }
        return emails;
    }

    /** ดึงโปรไฟล์ผู้ใช้ด้วย userName เท่านั้น (ตามที่ต้องการ) */
    public UserProfile getStudentProfile(String userName) {
        JsonNode data = callStdProfileByUsername(userName);
        if (data == null) return null;

        String uname   = pick(data, userName, "userName", "username", "student_id", "studentid", "id");
        String dispTH  = pick(data, null, "displayname_th", "displayName_th", "displaynameTh", "name_th");
        String email   = pick(data, null, "email", "studentEmail", "student_email", "mail");
        String dept    = pick(data, null, "department", "dept", "department_name");
        String faculty = pick(data, null, "faculty", "faculty_name");

        return new UserProfile(uname, dispTH, email, dept, faculty);
    }

    /** ===== HELPERS ===== */

    private JsonNode callStdProfileByUsername(String userName) {
        try {
            String url = normalizeBase(baseUrl) + "v2/profile/std/info/?id={userName}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

            // ให้ความสำคัญกับ Application-Key ตามที่คุณใช้ได้จริง
            if (applicationKey != null && !applicationKey.isBlank()) {
                headers.set("Application-Key", applicationKey);
            } else if (bearerToken != null && !bearerToken.isBlank()) {
                headers.setBearerAuth(bearerToken);
            }

            ResponseEntity<JsonNode> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class, userName);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) return null;

            JsonNode body = resp.getBody();
            // บาง API ห่อใน data, บางอันอยู่ root
            return body.has("data") && body.get("data").isObject() ? body.get("data") : body;
        } catch (Exception e) {
            return null;
        }
    }

    private String pick(JsonNode node, String fallback, String... keys) {
        if (node != null) {
            for (String k : keys) {
                JsonNode v = node.get(k);
                if (v != null && v.isTextual() && !v.asText().isBlank()) return v.asText();
            }
        }
        return fallback;
    }

    private void addIfText(Set<String> out, JsonNode node, String key) {
        if (node == null) return;
        JsonNode v = node.get(key);
        if (v != null && v.isTextual() && !v.asText().isBlank()) out.add(v.asText());
    }

    private String normalizeBase(String base) {
        if (base == null) return "";
        return base.endsWith("/") ? base : base + "/";
    }
}
