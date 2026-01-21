package com.aib.aib_backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeService {

    @Value("sk_test_51S2yJSC7SA5mF3zZ7CFCHsDA3HQr3a1lnqSpBHoq9rcy8mUzu74jzDiJEL93eZZszSIlW3HWv4UWO795br7VMLKE00wzT2hNX1")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Create a full refund for a payment intent
     * @param paymentIntentId The Stripe PaymentIntent ID
     * @return The Refund object
     */
    public Refund createFullRefund(String paymentIntentId) throws StripeException {
        log.info("Creating full refund for PaymentIntent: {}", paymentIntentId);

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();

        Refund refund = Refund.create(params);
        log.info("Refund created successfully: {} - Amount: {} {}",
                refund.getId(),
                refund.getAmount(),
                refund.getCurrency());

        return refund;
    }

    /**
     * Create a partial refund for a payment intent
     * @param paymentIntentId The Stripe PaymentIntent ID
     * @param amount The amount to refund (in smallest currency unit, e.g., millimes for TND)
     * @return The Refund object
     */
    public Refund createPartialRefund(String paymentIntentId, Long amount) throws StripeException {
        log.info("Creating partial refund for PaymentIntent: {} - Amount: {}", paymentIntentId, amount);

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amount)
                .build();

        Refund refund = Refund.create(params);
        log.info("Partial refund created successfully: {} - Amount: {} {}",
                refund.getId(),
                refund.getAmount(),
                refund.getCurrency());

        return refund;
    }

    /**
     * Get payment intent details
     * @param paymentIntentId The Stripe PaymentIntent ID
     * @return The PaymentIntent object
     */
    public PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    /**
     * Check if a payment intent can be refunded
     * @param paymentIntentId The Stripe PaymentIntent ID
     * @return true if refundable
     */
    public boolean canRefund(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            // Can only refund if payment was successful
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            log.error("Error checking refund eligibility: {}", e.getMessage());
            return false;
        }
    }
}