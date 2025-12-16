package com.example.test_spring.security;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class InputSanitizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9\\s._-]+$"
    );

    public String sanitizeHtml(String input) {
        if (input == null) return null;
        return Encode.forHtml(input.trim());
    }

    public String sanitizeJavaScript(String input) {
        if (input == null) return null;
        return Encode.forJavaScript(input.trim());
    }

    public String sanitizeUrl(String input) {
        if (input == null) return null;
        return Encode.forUriComponent(input.trim());
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.replaceAll("\\s", "")).matches();
    }

    public boolean isValidAlphanumeric(String input) {
        return input != null && ALPHANUMERIC_PATTERN.matcher(input).matches();
    }

    public String removeScriptTags(String input) {
        if (input == null) return null;
        return input.replaceAll("(?i)<script[^>]*>.*?</script>", "")
                   .replaceAll("(?i)<.*?javascript:.*?>", "")
                   .replaceAll("(?i)on\\w+\\s*=", "");
    }

    public String sanitizeComplaintText(String text) {
        if (text == null) return null;
        text = removeScriptTags(text);
        text = sanitizeHtml(text);
        return text.length() > 5000 ? text.substring(0, 5000) : text;
    }
}