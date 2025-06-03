package com.pfe.cars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    public String cleanHotelName(String rawHotelName) {
        // Replace %20 with space
        String withSpaces = rawHotelName.replace("%20", " ");
        // Lowercase everything then capitalize each word
        String[] words = withSpaces.toLowerCase().split(" ");
        StringBuilder finalName = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                finalName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return finalName.toString().trim();
    }

    public void sendBookingStatusEmail(String toEmail, String hotelName, String status, String reservationId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            String cleanedHotelName = cleanHotelName(hotelName);
            helper.setSubject("Booking Status Update - " + cleanedHotelName);
            helper.setFrom("noreply@medhabib.me");
            helper.setText(
                    "<html>" +
                            "<body>" +
                            "<h2>Booking Status Update</h2>" +
                            "<p>Dear Customer,</p>" +
                            "<p>Your booking for " + cleanedHotelName + " (Reservation ID: " + reservationId + ") has been " + status.toLowerCase() + ".</p>" +
                            "<p>Thank you for choosing our services.</p>" +
                            "<p>Best regards,<br>The Hotel Booking Team</p>" +
                            "</body>" +
                            "</html>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}