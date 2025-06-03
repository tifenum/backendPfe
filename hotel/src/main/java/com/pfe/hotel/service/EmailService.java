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
                    "<html lang=\"en\">" +
                            "<head>" +
                            "    <meta charset=\"UTF-8\">" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                            "    <title>Booking Status Update</title>" +
                            "</head>" +
                            "<body style=\"margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.5; color: #333333;\">" +
                            "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #f4f4f4; padding: 20px 0;\">" +
                            "        <tr>" +
                            "            <td align=\"center\">" +
                            "                <table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);\">" +
                            "                    <!-- Header -->" +
                            "                    <tr>" +
                            "                        <td style=\"background: linear-gradient(135deg, #add8e6, #87ceeb); padding: 20px; text-align: center;\">" +
                            "                            <img src=\"https://booking.medhabib.me/img/favicon.png\" alt=\"Booking Platform Logo\" style=\"max-width: 100px; height: auto; display: block; margin: 0 auto;\">" +
                            "                        </td>" +
                            "                    </tr>" +
                            "                    <!-- Content -->" +
                            "                    <tr>" +
                            "                        <td style=\"padding: 32px; text-align: center;\">" +
                            "                            <h2 style=\"color: #1a1a1a; font-size: 28px; font-weight: 700; margin: 0 0 16px; letter-spacing: -0.5px;\">Booking Status Update</h2>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">Dear Customer,</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">Your booking for " + cleanedHotelName + " (Reservation ID: " + reservationId + ") has been " + status.toLowerCase() + ".</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">Thank you for choosing our services.</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 14px; line-height: 1.5; margin: 24px 0 0;\">Need help? Contact us at <a href=\"mailto:support@booking.medhabib.me\" style=\"color: #ff4500; text-decoration: underline; font-weight: 500;\">support@booking.medhabib.me</a>.</p>" +
                            "                        </td>" +
                            "                    </tr>" +
                            "                    <!-- Footer -->" +
                            "                    <tr>" +
                            "                        <td style=\"background-color: #f8f8f8; padding: 20px; text-align: center; font-size: 12px; color: #777777;\">" +
                            "                            <p style=\"margin: 0 0 8px;\">© 2025 BookingPlatform. All rights reserved.</p>" +
                            "                            <p style=\"margin: 0;\">" +
                            "                                Follow us on " +
                            "                                <a href=\"https://x.com/bookingplatform\" style=\"color: #ff4500; text-decoration: none;\">X</a> | " +
                            "                                <a href=\"https://www.instagram.com/bookingplatform\" style=\"color: #ff4500; text-decoration: none;\">Instagram</a>" +
                            "                            </p>" +
                            "                        </td>" +
                            "                    </tr>" +
                            "                </table>" +
                            "            </td>" +
                            "        </tr>" +
                            "    </table>" +
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