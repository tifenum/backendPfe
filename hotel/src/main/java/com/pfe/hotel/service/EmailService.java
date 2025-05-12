package com.pfe.hotel.service;

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

    public void sendBookingStatusEmail(String toEmail, String hotelName, String status, String reservationId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Booking Status Update - " + hotelName);
            helper.setText(
                    "<html>" +
                            "<body>" +
                            "<h2>Booking Status Update</h2>" +
                            "<p>Dear Customer,</p>" +
                            "<p>Your booking for " + hotelName + " (Reservation ID: " + reservationId + ") has been " + status.toLowerCase() + ".</p>" +
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