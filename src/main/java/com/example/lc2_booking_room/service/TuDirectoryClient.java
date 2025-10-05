package com.example.lc2_booking_room.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class TuDirectoryClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    private final String appKey;      // <<<< Application-Key
    private final String bearerToken; // <<<< เผื่ออนาคต

    public TuDirectoryClient(RestTemplate restTemplate,
                             @Value("${app.tu.base-url}") String baseUrl,
                             @Value("${app.tu.application-key:}") String appKey,
                             @Value("${app.tu.bearer-token:}") String bearerToken) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.appKey = appKey;
        this.bearerToken = bearerToken;

        // เพิ่ม interceptor เผื่อ endpoint ใด ๆ ต้องใช้ Bearer ด้วย (optional)
        if (bearerToken != null && !bearerToken.isBlank()) {
            this.restTemplate.getInterceptors().add((req, body, ex) -> {
                req.getHeaders().setBearerAuth(bearerToken);
                return ex.execute(req, body);
            });
        }
    }

    /** เรียกโปรไฟล์ "นักศึกษา" : /v2/profile/std/info/?id={userName} */
    public Set<String> findStudentEmails(String userName) {
        try {
            String url = baseUrl + "v2/profile/std/info/?id={userName}";

            // ---- สร้าง headers ตามที่ TU ต้องการ ----
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            if (appKey != null && !appKey.isBlank()) {
                headers.set("Application-Key", appKey);
            }
            // หมายเหตุ: ถ้าบาง endpoint ต้อง Bearer และคุณมีค่าแล้ว Interceptor จะเติมให้อยู่แล้ว

            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> resp = restTemplate.exchange(
                    url, HttpMethod.GET, httpEntity, JsonNode.class, userName
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return Set.of();
            }
            return extractEmails(resp.getBody());
        } catch (RestClientException ex) {
            return Set.of();
        }
    }

    /** ดึงอีเมลจาก JSON แบบยืดหยุ่น */
    private Set<String> extractEmails(JsonNode root) {
        Set<String> emails = new LinkedHashSet<>();
        if (root == null) return emails;

        java.util.function.Consumer<JsonNode> addIfText = node -> {
            if (node != null && node.isTextual()) {
                String v = node.asText();
                if (v != null && !v.isBlank()) emails.add(v);
            }
        };
        java.util.function.Consumer<JsonNode> addArray = node -> {
            if (node != null && node.isArray()) {
                for (JsonNode n : node) addIfText.accept(n);
            }
        };

        addIfText.accept(root.get("email"));
        addIfText.accept(root.get("studentEmail"));
        addIfText.accept(root.get("student_email"));
        addArray.accept(root.get("emails"));

        JsonNode data = root.get("data");
        if (data != null) {
            addIfText.accept(data.get("email"));
            addIfText.accept(data.get("studentEmail"));
            addIfText.accept(data.get("student_email"));
            addArray.accept(data.get("emails"));
            JsonNode contact = data.get("contact");
            if (contact != null) {
                addIfText.accept(contact.get("email"));
                addArray.accept(contact.get("emails"));
            }
        }

        // ลูปเผื่อคีย์ที่มีคำว่า email
        root.fields().forEachRemaining(e -> {
            String k = e.getKey().toLowerCase();
            if (k.contains("email")) {
                JsonNode v = e.getValue();
                addIfText.accept(v);
                addArray.accept(v);
            }
        });

        return emails;
    }
}
