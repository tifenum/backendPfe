        package com.pfe.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;

@Service
public class NewsletterEmailService {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterEmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public String cleanName(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return "";
        }
        String decodedName = URLDecoder.decode(rawName, StandardCharsets.UTF_8)
                .toLowerCase()
                .trim();
        String[] words = decodedName.split("\\s+");
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

    public void sendNewsletterSubscriptionEmail(String toEmail, String name) {
        try {
            if (mailSender == null) {
                logger.error("JavaMailSender is not initialized");
                throw new IllegalStateException("JavaMailSender is not initialized");
            }
            logger.debug("Creating MimeMessage for email to {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Welcome to Our Travel Deals Newsletter!");
            helper.setFrom("noreply@medhabib.me");
            helper.setText(
                    "<html lang=\"en\">" +
                            "<head>" +
                            "    <meta charset=\"UTF-8\">" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                            "    <title>Newsletter Subscription Confirmation</title>" +
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
                            "                            <h2 style=\"color: #1a1a1a; font-size: 28px; font-weight: 700; margin: 0 0 16px; letter-spacing: -0.5px;\">Welcome to Our Travel Deals Newsletter!</h2>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">Dear " + cleanName(name) + ",</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">You're now subscribed to our newsletter! Get ready for exclusive discounts on flights, hotels, and car rentals, plus inspiration for your next adventure.</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">Thank you for joining us!</p>" +
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

            logger.debug("Sending email to {}", toEmail);
            mailSender.send(message);
            logger.info("Newsletter subscription email sent to {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send newsletter subscription email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send newsletter subscription email: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.error("JavaMailSender is null when sending email to {}", toEmail, e);
            throw new IllegalStateException("JavaMailSender is not initialized", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }
    public void sendContactConfirmationEmail(String toEmail, String name) {
        try {
            if (mailSender == null) {
                logger.error("JavaMailSender is not initialized");
                throw new IllegalStateException("JavaMailSender is not initialized");
            }
            logger.debug("Creating MimeMessage for contact confirmation email to {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Thank You for Contacting Us!");
            helper.setFrom("noreply@medhabib.me");
            helper.setText(
                    "<html lang=\"en\">" +
                            "<head>" +
                            "    <meta charset=\"UTF-8\">" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                            "    <title>Contact Request Confirmation</title>" +
                            "</head>" +
                            "<body style=\"margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.5; color: #333333;\">" +
                            "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #f4f4f4; padding: 20px 0;\">" +
                            "        <tr>" +
                            "            <td align=\"center\">" +
                            "                <table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);\">" +
                            "                    <!-- Header -->" +
                            "                    <tr>" +
                            "                        <td style=\"background: linear-gradient(135deg, #add8e6, #87ceeb); padding: 20px; text-align: center;\">" +
                            "                            <img src=\"https://booking.medhabib.me/images/favicon.png\" alt=\"Booking Platform Logo\" style=\"max-width: 100px; height: auto; display: block; margin: 0 auto;\">" +
                            "                        </td>" +
                            "                    </tr>" +
                            "                    <!-- Content -->" +
                            "                    <tr>" +
                            "                        <td style=\"padding: 32px; text-align: center;\">" +
                            "                            <h2 style=\"color: #1a1a1a; font-size: 28px; font-weight: 700; margin: 0 0 16px; letter-spacing: -0.5px;\">Thank You for Reaching Out!</h2>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">Dear " + cleanName(name) + ",</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">We've received your message and our travel experts will respond soon to assist with your travel plans or questions.</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 16px; line-height: 1.5; margin: 0 0 24px;\">Thank you for choosing us!</p>" +
                            "                            <p style=\"color: #4a4a4a; font-size: 14px; line-height: 1.5; margin: 24px 0 0;\">Need immediate help? Contact us at <a href=\"mailto:support@booking.medhabib.me\" style=\"color: #ff4500; text-decoration: underline; font-weight: 500;\">support@booking.medhabib.me</a>.</p>" +
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

            logger.debug("Sending contact confirmation email to {}", toEmail);
            mailSender.send(message);
            logger.info("Contact confirmation email sent to {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send contact confirmation email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send contact confirmation email: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.error("JavaMailSender is null when sending email to {}", toEmail, e);
            throw new IllegalStateException("JavaMailSender is not initialized", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }
}