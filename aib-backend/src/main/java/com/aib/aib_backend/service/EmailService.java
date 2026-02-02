package com.aib.aib_backend.service;

import com.aib.aib_backend.model.Order;
import com.aib.aib_backend.model.OrderItem;
import com.aib.aib_backend.model.Product;
import java.util.Set;


public interface EmailService {


    void sendVerificationEmail(String toEmail, String token);

    void sendWelcomeEmail(String toEmail, String firstName);

    void sendPasswordResetEmail(String toEmail, String token);

    void sendPasswordResetConfirmationEmail(String toEmail, String firstName);

    void sendOrderConfirmationEmail(String toEmail, String firstName, Order order, Set<OrderItem> items);

    void sendOrderStatusUpdateEmail(String toEmail, String firstName, Order order);

    void sendRefundProcessedEmail(String toEmail, String firstName, Order order, double refundAmount);

    void sendStockAlertEmail(String toEmail, String firstName, Product product, int requestedQuantity);

    void sendProductDeactivatedEmail(String toEmail, String firstName, Product product, int requestedQuantity);
}
