package com.blexta.Eventra.notification;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.blexta.Eventra.common.exceptions.EmailDeliveryException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class EmailService {

    private static final String FROM_EMAIL = "info@blexta.co.zw";
    private static final String FROM_NAME = "Eventra Support";
    private static final String PLATFORM_NAME = "Eventra";
    private static final String BASE_URL = "https://eventra.blexta.co.zw";

    @Autowired
    private JavaMailSender mailSender;

    private String cachedTemplate;

    @Async("notificationExecutor")
    public void sendAccountActivationEmail(String recipientEmail) {
        EmailBuilder builder = new EmailBuilder()
            .to(recipientEmail)
            .subject("Welcome to " + PLATFORM_NAME)
            .greeting("Hi " + recipientEmail + ",")
            .message("Welcome to " + PLATFORM_NAME + "! Discover amazing events around you and join the ones that excite you. Your account is now active.")
            .cta("Explore Events", BASE_URL + "/dashboard")
            .features(new String[]{
                "🎟 Browse and RSVP to events",
                "⭐ Track your interests and favorite events",
                "📁 Manage your profile and event registrations"
            })
            .footerMessage("Questions? Just hit reply. Eventra is here to make your event experience smooth!");

        dispatchEmail(builder);
    }

    @Async("notificationExecutor")
    public void sendPasswordResetEmail(String email, String token) {
        EmailBuilder builder = new EmailBuilder()
            .to(email)
            .subject("Reset Your " + PLATFORM_NAME + " Password")
            .greeting("Hi " + email + ",")
            .message("We received a request to reset your password. Click the button below to set a new one.")
            .cta("Reset Password", BASE_URL + "/reset-password?token=" + token)
            .footerMessage("If you didn’t request this, ignore this email. Your account remains secure.");

        dispatchEmail(builder);
    }

    @Async("notificationExecutor")
    public void sendOtpVerificationEmail(String email, String otp) {
        EmailBuilder builder = new EmailBuilder()
            .to(email)
            .subject("Confirm Your Email for " + PLATFORM_NAME)
            .greeting("Hi " + email + ",")
            .message("Thanks for signing up! Verify your email to start exploring events around you.")
            .infoBox("Your Verification Code", otp)
            .footerMessage("This code will expire in 2 hours. If you didn’t create this account, ignore this email.");

        dispatchEmail(builder);
    }

    @Async("notificationExecutor")
    public void sendRequestOtpEmail(String email, String username, String otp) {
        EmailBuilder builder = new EmailBuilder()
            .to(email)
            .subject("Your Login OTP for " + PLATFORM_NAME)
            .greeting("Hey " + username + ",")
            .message("You requested a One-Time Password (OTP) to log into " + PLATFORM_NAME + ". Use the code below to log in.")
            .infoBox("Your OTP", otp)
            .footerMessage("Valid for 2 hours. If you didn’t request this, ignore this email.");

        dispatchEmail(builder);
    }

    @Async("notificationExecutor")
    public void sendPasswordChangeConfirmation(String email) {
        EmailBuilder builder = new EmailBuilder()
            .to(email)
            .subject("Your " + PLATFORM_NAME + " Password was changed")
            .greeting("Hi " + email + ",")
            .message("Your password was successfully updated.")
            .footerMessage("If this wasn’t you, reset your password immediately or contact Eventra support.");

        dispatchEmail(builder);
    }

    @Async("notificationExecutor")
    public void sendEventRegistrationConfirmation(String email, String eventName, String eventUrl) {
        EmailBuilder builder = new EmailBuilder()
            .to(email)
            .subject("You’re Registered for " + eventName)
            .greeting("Hello,")
            .message("You have successfully registered for " + eventName + ".")
            .cta("View Event Details", eventUrl)
            .footerMessage("Looking forward to seeing you at the event!");

        dispatchEmail(builder);
    }

    @Async("notificationExecutor")
    private void dispatchEmail(EmailBuilder builder) {
        try {
            String htmlTemplate = getTemplate();
            String processed = fillTemplate(htmlTemplate, builder);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            helper.setTo(builder.toEmail);
            helper.setSubject(builder.subject);
            helper.setText(processed, true);

            mailSender.send(message);
            System.out.println("email sent to " + builder.toEmail);
        } catch (Exception ex) {
            System.err.println("Failed to send email to " + builder.toEmail + ": " + ex.getMessage());
            throw new EmailDeliveryException("Email delivery failed", ex);
        }
    }

    private String getTemplate() throws IOException {
        if (cachedTemplate == null) {
            Path path = new ClassPathResource("templates/email-template.html").getFile().toPath();
            cachedTemplate = Files.readString(path, StandardCharsets.UTF_8);
        }
        return cachedTemplate;
    }

    private String fillTemplate(String template, EmailBuilder builder) {
    String result = template
        .replace("{{EMAIL_TITLE}}", builder.subject)
        .replace("{{GREETING}}", builder.greeting)
        .replace("{{MESSAGE_CONTENT}}", builder.message)
        .replace("{{USER_EMAIL}}", builder.toEmail)
        .replace("{{FOOTER_MESSAGE}}", builder.footerMessage)
        .replace("{{UNSUBSCRIBE_URL}}", BASE_URL + "/unsubscribe")
        .replace("{{PRIVACY_URL}}", BASE_URL + "/privacy")
        .replace("{{SUPPORT_URL}}", BASE_URL + "/contact");

    // CTA Button
    if (builder.ctaText != null && builder.ctaUrl != null) {
        result = result
            .replaceAll("(?s)\\{\\{#CTA_BUTTON\\}\\}(.*?)\\{\\{/CTA_BUTTON\\}\\}", 
                        "<a href=\"" + builder.ctaUrl + "\" class=\"btn\">" + builder.ctaText + "</a>");
    } else {
        result = result.replaceAll("(?s)\\{\\{#CTA_BUTTON\\}\\}.*?\\{\\{/CTA_BUTTON\\}\\}", "");
    }

    // Info Box
    if (builder.infoTitle != null && builder.infoContent != null) {
        result = result
            .replaceAll("(?s)\\{\\{#INFO_BOX\\}\\}(.*?)\\{\\{/INFO_BOX\\}\\}", 
                        "<div class=\"info-box\">" +
                        "<div class=\"info-title\">" + builder.infoTitle + "</div>" +
                        "<div class=\"info-content\">" + builder.infoContent + "</div>" +
                        "</div>");
    } else {
        result = result.replaceAll("(?s)\\{\\{#INFO_BOX\\}\\}.*?\\{\\{/INFO_BOX\\}\\}", "");
    }

    // Features List
    if (builder.features != null && builder.features.length > 0) {
        StringBuilder featureList = new StringBuilder("<ul class=\"features-list\">");
        for (String item : builder.features) {
            featureList.append("<li>").append(item).append("</li>");
        }
        featureList.append("</ul>");
        result = result.replaceAll("(?s)\\{\\{#FEATURES\\}\\}.*?\\{\\{/FEATURES\\}\\}", featureList.toString());
    } else {
        result = result.replaceAll("(?s)\\{\\{#FEATURES\\}\\}.*?\\{\\{/FEATURES\\}\\}", "");
    }

    return result;
}

    private static class EmailBuilder {
        private String toEmail;
        private String subject;
        private String greeting;
        private String message;
        private String ctaText;
        private String ctaUrl;
        private String infoTitle;
        private String infoContent;
        private String[] features;
        private String footerMessage = "Powered by Blexta. Thank you for trusting Eventra.";

        public EmailBuilder to(String email) { this.toEmail = email; return this; }
        public EmailBuilder subject(String subject) { this.subject = subject; return this; }
        public EmailBuilder greeting(String greeting) { this.greeting = greeting; return this; }
        public EmailBuilder message(String message) { this.message = message; return this; }
        public EmailBuilder cta(String text, String url) { this.ctaText = text; this.ctaUrl = url; return this; }
        public EmailBuilder infoBox(String title, String content) { this.infoTitle = title; this.infoContent = content; return this; }
        public EmailBuilder features(String[] features) { this.features = features; return this; }
        public EmailBuilder footerMessage(String footer) { this.footerMessage = footer; return this; }
    }
}
