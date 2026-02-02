package com.aib.aib_backend.service.impl;

import com.aib.aib_backend.model.Order;
import com.aib.aib_backend.model.OrderItem;
import com.aib.aib_backend.model.Product;
import com.aib.aib_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private String buildEmail(String iconEmoji, String title, String content) {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <title>%s</title>
            <!--[if mso]>
            <style type="text/css">
                body, table, td {font-family: Arial, Helvetica, sans-serif !important;}
            </style>
            <![endif]-->
        </head>
        <body style="margin: 0; padding: 0; background-color: #0a0e17; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color: #0a0e17;">
                <tr>
                    <td style="padding: 40px 20px;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="margin: 0 auto; max-width: 600px;">
                            
                            <!-- Logo & Header -->
                            <tr>
                                <td style="text-align: center; padding-bottom: 30px;">
                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin: 0 auto;">
                                        <tr>
                                            <td style="background: linear-gradient(135deg, #00d4ff 0%%, #00ff88 100%%); -webkit-background-clip: text; background-clip: text;">
                                                <h1 style="margin: 0; font-size: 28px; font-weight: 700; color: #00d4ff; letter-spacing: 2px;">
                                                    ⚡ AIB
                                                </h1>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding-top: 5px;">
                                                <span style="color: #64748b; font-size: 12px; letter-spacing: 3px; text-transform: uppercase;">Electronics</span>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            
                            <!-- Main Card -->
                            <tr>
                                <td>
                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background: linear-gradient(145deg, #131a2b 0%%, #0d1321 100%%); border-radius: 24px; border: 1px solid #1e293b; overflow: hidden;">
                                        
                                        <!-- Card Header with Gradient Accent -->
                                        <tr>
                                            <td style="height: 4px; background: linear-gradient(90deg, #00d4ff 0%%, #a855f7 50%%, #00ff88 100%%);"></td>
                                        </tr>
                                        
                                        <!-- Icon & Title Section -->
                                        <tr>
                                            <td style="padding: 40px 40px 20px 40px; text-align: center;">
                                                <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin: 0 auto;">
                                                    <tr>
                                                        <td style="width: 80px; height: 80px; background: linear-gradient(135deg, rgba(0, 212, 255, 0.15) 0%%, rgba(168, 85, 247, 0.15) 100%%); border-radius: 50%%; text-align: center; vertical-align: middle; border: 2px solid rgba(0, 212, 255, 0.3);">
                                                            <span style="font-size: 36px; line-height: 80px;">%s</span>
                                                        </td>
                                                    </tr>
                                                </table>
                                                <h2 style="margin: 24px 0 0 0; font-size: 24px; font-weight: 600; color: #f1f5f9; letter-spacing: 0.5px;">%s</h2>
                                            </td>
                                        </tr>
                                        
                                        <!-- Content Section -->
                                        <tr>
                                            <td style="padding: 10px 40px 40px 40px;">
                                                <div style="color: #94a3b8; font-size: 16px; line-height: 1.8;">
                                                    %s
                                                </div>
                                            </td>
                                        </tr>
                                        
                                    </table>
                                </td>
                            </tr>
                            
                            <!-- Footer -->
                            <tr>
                                <td style="padding: 40px 20px 20px 20px; text-align: center;">
                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin: 0 auto;">
                                        <tr>
                                            <td style="padding-bottom: 16px;">
                                                <a href="%s" style="display: inline-block; padding: 8px 16px; margin: 0 6px; background: rgba(0, 212, 255, 0.1); border: 1px solid rgba(0, 212, 255, 0.3); border-radius: 8px; color: #00d4ff; text-decoration: none; font-size: 13px;">🌐 Website</a>
                                                <a href="%s/products" style="display: inline-block; padding: 8px 16px; margin: 0 6px; background: rgba(0, 212, 255, 0.1); border: 1px solid rgba(0, 212, 255, 0.3); border-radius: 8px; color: #00d4ff; text-decoration: none; font-size: 13px;">🛒 Shop</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <p style="margin: 0 0 8px 0; color: #475569; font-size: 13px;">© %d AIB Electronics. All rights reserved.</p>
                                                <p style="margin: 0; color: #334155; font-size: 12px;">Powering your digital lifestyle ⚡</p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(title, iconEmoji, title, content, frontendUrl, frontendUrl, LocalDateTime.now().getYear());
    }

    private String buildButton(String text, String url, String type) {
        String gradientColors = switch (type) {
            case "primary" -> "linear-gradient(135deg, #00d4ff 0%, #0099cc 100%)";
            case "success" -> "linear-gradient(135deg, #00ff88 0%, #00cc6a 100%)";
            case "warning" -> "linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)";
            case "danger" -> "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)";
            default -> "linear-gradient(135deg, #a855f7 0%, #7c3aed 100%)";
        };

        return String.format("""
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin: 30px auto;">
                <tr>
                    <td style="border-radius: 12px; background: %s; box-shadow: 0 4px 20px rgba(0, 212, 255, 0.3);">
                        <a href="%s" target="_blank" style="display: inline-block; padding: 16px 40px; color: #0a0e17; text-decoration: none; font-weight: 600; font-size: 15px; letter-spacing: 0.5px;">%s →</a>
                    </td>
                </tr>
            </table>
        """, gradientColors, url, text);
    }

    private String buildInfoBox(String content) {
        return String.format("""
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="background: rgba(0, 212, 255, 0.05); border: 1px solid rgba(0, 212, 255, 0.2); border-radius: 16px; padding: 24px;">
                        %s
                    </td>
                </tr>
            </table>
        """, content);
    }

    private String buildDivider() {
        return """
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="height: 1px; background: linear-gradient(90deg, transparent 0%, #1e293b 50%, transparent 100%);"></td>
                </tr>
            </table>
        """;
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "✨ Verify Your Email - AIB Electronics";
        String verificationLink = frontendUrl + "/auth/verify-email?token=" + token;

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Welcome to the future! 🚀</p>
            <p>You're just one step away from joining the AIB community. Click the button below to verify your email and unlock your account.</p>
            %s
            %s
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin-top: 30px;">
                <tr>
                    <td style="background: rgba(251, 191, 36, 0.1); border-left: 3px solid #fbbf24; padding: 16px 20px; border-radius: 0 12px 12px 0;">
                        <p style="margin: 0; color: #fbbf24; font-size: 14px;">
                            <strong>⚠️ Didn't create an account?</strong><br>
                            <span style="color: #94a3b8;">No worries! Simply ignore this email and nothing will happen.</span>
                        </p>
                    </td>
                </tr>
            </table>
        """, buildButton("Verify My Email", verificationLink, "primary"), buildDivider());

        sendEmail(toEmail, subject, buildEmail("📧", "Verify Your Email", content));
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "🎉 Welcome to AIB, " + firstName + "!";

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Hey %s! 👋</p>
            <p>Welcome aboard! Your account is now fully activated and ready to go. You've just unlocked access to the most cutting-edge electronics and tech gear on the planet.</p>
            %s
            <p style="color: #e2e8f0; font-size: 15px; margin: 24px 0 16px 0;"><strong>Here's what you can do now:</strong></p>
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                    <td style="padding: 12px 0;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                            <tr>
                                <td style="width: 40px; height: 40px; background: rgba(0, 255, 136, 0.15); border-radius: 10px; text-align: center; vertical-align: middle;">
                                    <span style="font-size: 18px;">🛍️</span>
                                </td>
                                <td style="padding-left: 16px;">
                                    <p style="margin: 0; color: #e2e8f0; font-size: 14px; font-weight: 500;">Browse Products</p>
                                    <p style="margin: 4px 0 0 0; color: #64748b; font-size: 13px;">Explore our curated collection of tech</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 12px 0;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                            <tr>
                                <td style="width: 40px; height: 40px; background: rgba(168, 85, 247, 0.15); border-radius: 10px; text-align: center; vertical-align: middle;">
                                    <span style="font-size: 18px;">💳</span>
                                </td>
                                <td style="padding-left: 16px;">
                                    <p style="margin: 0; color: #e2e8f0; font-size: 14px; font-weight: 500;">Secure Checkout</p>
                                    <p style="margin: 4px 0 0 0; color: #64748b; font-size: 13px;">Fast & encrypted payment processing</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 12px 0;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                            <tr>
                                <td style="width: 40px; height: 40px; background: rgba(0, 212, 255, 0.15); border-radius: 10px; text-align: center; vertical-align: middle;">
                                    <span style="font-size: 18px;">📦</span>
                                </td>
                                <td style="padding-left: 16px;">
                                    <p style="margin: 0; color: #e2e8f0; font-size: 14px; font-weight: 500;">Track Orders</p>
                                    <p style="margin: 4px 0 0 0; color: #64748b; font-size: 13px;">Real-time updates on your deliveries</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            %s
            %s
        """, firstName, buildDivider(), buildDivider(), buildButton("Start Shopping", frontendUrl, "success"));

        sendEmail(toEmail, subject, buildEmail("🎉", "Welcome to AIB!", content));
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "🔐 Password Reset - AIB Electronics";
        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Password Reset Request</p>
            <p>We received a request to reset the password for your AIB account. Click the button below to create a new password.</p>
            %s
            %s
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                    <td style="background: rgba(239, 68, 68, 0.1); border-left: 3px solid #ef4444; padding: 16px 20px; border-radius: 0 12px 12px 0;">
                        <p style="margin: 0; color: #ef4444; font-size: 14px;">
                            <strong>🔒 Security Notice</strong><br>
                            <span style="color: #94a3b8;">This link expires in 1 hour. If you didn't request this, please ignore this email — your account is safe.</span>
                        </p>
                    </td>
                </tr>
            </table>
        """, buildButton("Reset Password", resetLink, "warning"), buildDivider());

        sendEmail(toEmail, subject, buildEmail("🔑", "Reset Your Password", content));
    }

    public void sendPasswordResetConfirmationEmail(String toEmail, String firstName) {
        String subject = "✅ Password Changed - AIB Electronics";

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Hey %s,</p>
            <p>Your password has been successfully updated. You can now log in with your new credentials.</p>
            %s
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="background: rgba(0, 255, 136, 0.1); border: 1px solid rgba(0, 255, 136, 0.3); border-radius: 16px; padding: 20px; text-align: center;">
                        <span style="font-size: 32px;">✅</span>
                        <p style="margin: 12px 0 0 0; color: #00ff88; font-size: 15px; font-weight: 500;">Password Changed Successfully</p>
                    </td>
                </tr>
            </table>
            %s
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                    <td style="background: rgba(239, 68, 68, 0.1); border-left: 3px solid #ef4444; padding: 16px 20px; border-radius: 0 12px 12px 0;">
                        <p style="margin: 0; color: #ef4444; font-size: 14px;">
                            <strong>⚠️ Wasn't you?</strong><br>
                            <span style="color: #94a3b8;">If you didn't make this change, please contact our support team immediately.</span>
                        </p>
                    </td>
                </tr>
            </table>
        """, firstName, buildDivider(), buildDivider());

        sendEmail(toEmail, subject, buildEmail("🔒", "Password Updated", content));
    }

    public void sendOrderConfirmationEmail(String toEmail, String firstName, Order order, Set<OrderItem> items) {
        String subject = "🎉 Order Confirmed #" + order.getOrderNumber() + " - AIB Electronics";

        StringBuilder itemsHtml = new StringBuilder();
        for (OrderItem item : items) {
            itemsHtml.append(String.format("""
                <tr>
                    <td style="padding: 16px 0; border-bottom: 1px solid #1e293b;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                            <tr>
                                <td style="vertical-align: top;">
                                    <p style="margin: 0 0 6px 0; color: #e2e8f0; font-size: 15px; font-weight: 500;">%s</p>
                                    <p style="margin: 0; color: #64748b; font-size: 13px;">Qty: %d</p>
                                </td>
                                <td style="text-align: right; vertical-align: top;">
                                    <p style="margin: 0; color: #00d4ff; font-size: 15px; font-weight: 600;">%.2f TND</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            """, item.getProduct().getName(), item.getQuantity(), item.getTotalPrice()));
        }

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Hey %s! 🎊</p>
            <p>Awesome choice! Your order has been confirmed and we're preparing it with care. Here's a summary of what's coming your way:</p>
            
            <!-- Order Number Badge -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%%, rgba(168, 85, 247, 0.1) 100%%); border: 1px solid rgba(0, 212, 255, 0.3); border-radius: 12px; padding: 16px; text-align: center;">
                        <p style="margin: 0; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 1px;">Order Number</p>
                        <p style="margin: 6px 0 0 0; color: #00d4ff; font-size: 20px; font-weight: 700; letter-spacing: 1px;">%s</p>
                    </td>
                </tr>
            </table>
            
            <!-- Items List -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin-bottom: 24px;">
                <tr>
                    <td style="padding-bottom: 12px;">
                        <p style="margin: 0; color: #e2e8f0; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">📦 Items</p>
                    </td>
                </tr>
                %s
            </table>
            
            <!-- Pricing Summary -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background: rgba(0, 0, 0, 0.3); border-radius: 12px; padding: 20px;">
                <tr>
                    <td>
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                            <tr>
                                <td style="padding: 8px 0; color: #94a3b8; font-size: 14px;">Subtotal</td>
                                <td style="padding: 8px 0; color: #e2e8f0; font-size: 14px; text-align: right;">%.2f TND</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #94a3b8; font-size: 14px;">Shipping</td>
                                <td style="padding: 8px 0; color: #e2e8f0; font-size: 14px; text-align: right;">%.2f TND</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #94a3b8; font-size: 14px;">Tax</td>
                                <td style="padding: 8px 0; color: #e2e8f0; font-size: 14px; text-align: right;">%.2f TND</td>
                            </tr>
                            <tr>
                                <td colspan="2" style="padding-top: 12px;">
                                    <div style="height: 1px; background: #1e293b;"></div>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 16px 0 8px 0; color: #e2e8f0; font-size: 16px; font-weight: 600;">Total</td>
                                <td style="padding: 16px 0 8px 0; color: #00ff88; font-size: 20px; font-weight: 700; text-align: right;">%.2f TND</td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            
            %s
            <p style="text-align: center; color: #64748b; font-size: 14px;">Track your order status anytime:</p>
            %s
        """, firstName, order.getOrderNumber(), itemsHtml.toString(),
                order.getSubtotal(), order.getShippingFee(), order.getTax(), order.getTotal(),
                buildDivider(), buildButton("View Order Details", frontendUrl + "/orders/" + order.getId(), "primary"));

        sendEmail(toEmail, subject, buildEmail("📦", "Order Confirmed!", content));
    }

    public void sendOrderStatusUpdateEmail(String toEmail, String firstName, Order order) {
        String subject = "📬 Order Update #" + order.getOrderNumber() + " - AIB Electronics";

        String[] statusConfig = switch (order.getStatus()) {
            case "PROCESSING" -> new String[]{"🔧", "Processing Your Order", "Your order is now being prepared by our team. We're making sure everything is perfect!", "primary", "#a855f7"};
            case "SHIPPED" -> new String[]{"🚚", "Order Shipped!", "Great news! Your package is on its way. Keep an eye on your doorstep!", "success", "#00ff88"};
            case "DELIVERED" -> new String[]{"🎉", "Order Delivered!", "Your order has arrived! We hope you love your new tech. Enjoy!", "success", "#00ff88"};
            case "CANCELLED" -> new String[]{"❌", "Order Cancelled", "Your order has been cancelled as requested. If you have any questions, we're here to help.", "warning", "#ef4444"};
            default -> new String[]{"📦", "Order Update", "There's an update on your order.", "primary", "#00d4ff"};
        };

        // Build status timeline
        String[] allStatuses = {"PLACED", "PROCESSING", "SHIPPED", "DELIVERED"};
        StringBuilder timelineHtml = new StringBuilder();
        boolean reachedCurrent = false;

        if (!order.getStatus().equals("CANCELLED")) {
            timelineHtml.append("""
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%" style="margin: 24px 0;">
                    <tr>
            """);

            for (int i = 0; i < allStatuses.length; i++) {
                String status = allStatuses[i];
                boolean isActive = !reachedCurrent;
                boolean isCurrent = status.equals(order.getStatus());
                if (isCurrent) reachedCurrent = true;

                String bgColor = isActive ? "rgba(0, 255, 136, 0.2)" : "rgba(100, 116, 139, 0.2)";
                String borderColor = isActive ? "#00ff88" : "#475569";
                String textColor = isActive ? "#00ff88" : "#64748b";
                String emoji = switch (status) {
                    case "PLACED" -> "📝";
                    case "PROCESSING" -> "🔧";
                    case "SHIPPED" -> "🚚";
                    case "DELIVERED" -> "✅";
                    default -> "📦";
                };

                timelineHtml.append(String.format("""
                    <td style="text-align: center; width: 25%%;">
                        <div style="width: 40px; height: 40px; margin: 0 auto; background: %s; border: 2px solid %s; border-radius: 50%%; line-height: 36px;">
                            <span style="font-size: 16px;">%s</span>
                        </div>
                        <p style="margin: 8px 0 0 0; font-size: 11px; color: %s; text-transform: uppercase; letter-spacing: 0.5px;">%s</p>
                    </td>
                """, bgColor, borderColor, emoji, textColor, status));
            }

            timelineHtml.append("""
                    </tr>
                </table>
            """);
        }

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Hey %s,</p>
            <p>%s</p>
            
            <!-- Status Badge -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="background: rgba(0, 0, 0, 0.3); border: 2px solid %s; border-radius: 16px; padding: 24px; text-align: center;">
                        <span style="font-size: 48px;">%s</span>
                        <p style="margin: 16px 0 0 0; color: %s; font-size: 20px; font-weight: 700; text-transform: uppercase; letter-spacing: 2px;">%s</p>
                    </td>
                </tr>
            </table>
            
            <!-- Order Number -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin-bottom: 24px;">
                <tr>
                    <td style="text-align: center;">
                        <p style="margin: 0; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 1px;">Order Number</p>
                        <p style="margin: 6px 0 0 0; color: #e2e8f0; font-size: 18px; font-weight: 600;">%s</p>
                    </td>
                </tr>
            </table>
            
            %s
            
            %s
            %s
        """, firstName, statusConfig[2], statusConfig[4], statusConfig[0], statusConfig[4], order.getStatus(),
                order.getOrderNumber(), timelineHtml.toString(), buildDivider(),
                buildButton("View Order Details", frontendUrl + "/orders/" + order.getId(), statusConfig[3]));

        sendEmail(toEmail, subject, buildEmail(statusConfig[0], statusConfig[1], content));
    }

    public void sendRefundProcessedEmail(String toEmail, String firstName, Order order, double refundAmount) {
        String subject = "💸 Refund Processed - Order #" + order.getOrderNumber();

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Hey %s,</p>
            <p>Good news! We've processed your refund for order #%s. The funds should appear in your account within 5-10 business days, depending on your bank.</p>
            
            <!-- Refund Amount Badge -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="background: linear-gradient(135deg, rgba(0, 255, 136, 0.1) 0%%, rgba(0, 212, 255, 0.1) 100%%); border: 1px solid rgba(0, 255, 136, 0.3); border-radius: 16px; padding: 24px; text-align: center;">
                        <p style="margin: 0; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 1px;">Refund Amount</p>
                        <p style="margin: 8px 0 0 0; color: #00ff88; font-size: 32px; font-weight: 700;">%.2f TND</p>
                    </td>
                </tr>
            </table>
            
            %s
            
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                    <td style="background: rgba(0, 212, 255, 0.1); border-left: 3px solid #00d4ff; padding: 16px 20px; border-radius: 0 12px 12px 0;">
                        <p style="margin: 0; color: #00d4ff; font-size: 14px;">
                            <strong>💡 Questions?</strong><br>
                            <span style="color: #94a3b8;">If you don't see the refund within 10 business days, please contact your bank or reach out to us.</span>
                        </p>
                    </td>
                </tr>
            </table>
            
            %s
            %s
        """, firstName, order.getOrderNumber(), refundAmount, buildDivider(), buildDivider(),
                buildButton("View Order History", frontendUrl + "/orders", "success"));

        sendEmail(toEmail, subject, buildEmail("💰", "Refund Processed", content));
    }

    public void sendStockAlertEmail(String toEmail, String firstName, Product product, int requestedQuantity) {
        String subject = "⚠️ Stock Alert: " + product.getName() + " - AIB Electronics";

        boolean isOutOfStock = product.getStockQuantity() == 0;
        String stockMessage = isOutOfStock
                ? "is currently <strong style=\"color: #ef4444;\">out of stock</strong>"
                : "has only <strong style=\"color: #fbbf24;\">" + product.getStockQuantity() + " items</strong> left in stock";

        String iconEmoji = isOutOfStock ? "🚫" : "⚠️";
        String badgeColor = isOutOfStock ? "#ef4444" : "#fbbf24";
        String badgeBgColor = isOutOfStock ? "rgba(239, 68, 68, 0.1)" : "rgba(251, 191, 36, 0.1)";

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Hey %s,</p>
            <p>We noticed you have an item in your cart that needs your attention.</p>
            
            <!-- Product Alert Card -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="background: %s; border: 1px solid %s; border-radius: 16px; padding: 24px;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                            <tr>
                                <td style="vertical-align: top; width: 60px;">
                                    <div style="width: 50px; height: 50px; background: rgba(0, 0, 0, 0.3); border-radius: 12px; text-align: center; line-height: 50px;">
                                        <span style="font-size: 24px;">📦</span>
                                    </div>
                                </td>
                                <td style="vertical-align: top; padding-left: 16px;">
                                    <p style="margin: 0 0 8px 0; color: #e2e8f0; font-size: 16px; font-weight: 600;">%s</p>
                                    <p style="margin: 0; color: #94a3b8; font-size: 14px; line-height: 1.6;">
                                        This item %s.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            
            <!-- Quantity Info -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="text-align: center;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin: 0 auto;">
                            <tr>
                                <td style="padding: 0 20px; text-align: center;">
                                    <p style="margin: 0; color: #64748b; font-size: 11px; text-transform: uppercase; letter-spacing: 1px;">You Requested</p>
                                    <p style="margin: 6px 0 0 0; color: #e2e8f0; font-size: 24px; font-weight: 700;">%d</p>
                                </td>
                                <td style="width: 1px; background: #1e293b;"></td>
                                <td style="padding: 0 20px; text-align: center;">
                                    <p style="margin: 0; color: #64748b; font-size: 11px; text-transform: uppercase; letter-spacing: 1px;">Available</p>
                                    <p style="margin: 6px 0 0 0; color: %s; font-size: 24px; font-weight: 700;">%d</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            
            %s
            
            <p style="text-align: center; color: #94a3b8; font-size: 14px;">Please review your cart and adjust the quantity or remove the item to proceed with checkout.</p>
            
            %s
        """, firstName, badgeBgColor, badgeColor, product.getName(), stockMessage,
                requestedQuantity, badgeColor, product.getStockQuantity(),
                buildDivider(), buildButton("Review My Cart", frontendUrl + "/cart", "warning"));

        sendEmail(toEmail, subject, buildEmail(iconEmoji, "Stock Alert", content));
    }

    public void sendProductDeactivatedEmail(String toEmail, String firstName, Product product, int requestedQuantity) {
        String subject = "🚫 Product Unavailable: " + product.getName() + " - AIB Electronics";

        String content = String.format("""
            <p style="color: #e2e8f0; font-size: 17px; margin-bottom: 8px;">Hey %s,</p>
            <p>We're writing to let you know about an item in your cart.</p>
            
            <!-- Product Alert Card -->
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 24px 0;">
                <tr>
                    <td style="background: rgba(239, 68, 68, 0.1); border: 1px solid #ef4444; border-radius: 16px; padding: 24px;">
                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                            <tr>
                                <td style="vertical-align: top; width: 60px;">
                                    <div style="width: 50px; height: 50px; background: rgba(0, 0, 0, 0.3); border-radius: 12px; text-align: center; line-height: 50px;">
                                        <span style="font-size: 24px;">🛑</span>
                                    </div>
                                </td>
                                <td style="vertical-align: top; padding-left: 16px;">
                                    <p style="margin: 0 0 8px 0; color: #e2e8f0; font-size: 16px; font-weight: 600;">%s</p>
                                    <p style="margin: 0; color: #94a3b8; font-size: 14px; line-height: 1.6;">
                                        This product is <strong style="color: #ef4444;">no longer available</strong> for purchase.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            
            <p style="text-align: center; color: #94a3b8; font-size: 14px;">Please remove this item from your cart to proceed with checkout.</p>
            
            %s
            %s
        """, firstName, product.getName(), buildDivider(), buildButton("Update Cart", frontendUrl + "/cart", "danger"));

        sendEmail(toEmail, subject, buildEmail("🚫", "Product Unavailable", content));
    }
}
