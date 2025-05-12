package com.pfe.flight.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingStatusEmail(String toEmail, String flightDetails, String status, String bookingId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Flight Booking Status Update - Booking ID: " + bookingId);
            helper.setText(
                    "<html>" +
                            "<body>" +
                            "<h2>Flight Booking Status Update</h2>" +
                            "<p>Dear Customer,</p>" +
                            "<p>Your booking for flight " + flightDetails + " (Booking ID: " + bookingId + ") has been " + status.toLowerCase() + ".</p>" +
                            "<p>Thank you for choosing our services.</p>" +
                            "<p>Best regards,<br>The Flight Booking Team</p>" +
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