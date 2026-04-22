package soyosa.userservice.Config.MailConfig;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String verificationLink)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Verify Your Account");

        String htmlContent = buildVerificationEmail(verificationLink);

        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }

    public String buildVerificationEmail(String link) {

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color:#f4f4f4; padding:20px;">
                    <div style="max-width:600px; margin:auto; background:white; padding:30px; border-radius:10px;">
                        <h2 style="color:#2c3e50;">Welcome 👋</h2>
                        <p>Thank you for registering.</p>
                        <p>Please click the button below to verify your account:</p>

                        <div style="text-align:center; margin:30px 0;">
                            <a href="%s"
                               style="background-color:#3498db;
                                      color:white;
                                      padding:12px 20px;
                                      text-decoration:none;
                                      border-radius:5px;
                                      display:inline-block;">
                                Verify Account
                            </a>
                        </div>

                        <p>If you did not create this account, you can ignore this email.</p>

                        <hr>
                        <small>© 2025 Your Application</small>
                    </div>
                </body>
                </html>
                """.formatted(link);
    }

    public String buildPasswordResetEmail(String email,String link) {

        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; background-color:#f4f4f4; padding:20px;">
                <div style="max-width:600px; margin:auto; background:white; padding:30px; border-radius:10px;">
                    
                    <h2 style="color:#2c3e50;">Reset Your Password 🔐</h2>

                    <p>We received a request to reset your password.</p>

                    <p>Click the button below to choose a new password:</p>

                    <div style="text-align:center; margin:30px 0;">
                        <a href="%s"
                           style="background-color:#e67e22;
                                  color:white;
                                  padding:12px 20px;
                                  text-decoration:none;
                                  border-radius:5px;
                                  display:inline-block;
                                  font-weight:bold;">
                            Reset Password
                        </a>
                    </div>

                    <p>This link will expire in <b>15 minutes</b>.</p>

                    <p>If you did not request a password reset, you can safely ignore this email.</p>

                    <hr>
                    <small>© 2026 Your Application</small>

                </div>
            </body>
            </html>
            """.formatted(link);
    }

    public void forgotPasswordMail(String to, String verificationLink)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Verify Your Account");

        String htmlContent = buildPasswordResetEmail(to, verificationLink);

        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }



}
