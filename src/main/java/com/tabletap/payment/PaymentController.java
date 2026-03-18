package com.tabletap.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.tabletap.dto.response.PaymentIntentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final StripeService stripeService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestBody CreateIntentRequest request) {
        return ResponseEntity.ok(stripeService.createPaymentIntent(request.orderId()));
    }

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("Stripe event: {}", event.getType());
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        switch (event.getType()) {
            case "payment_intent.succeeded" -> deserializer.getObject().ifPresent(obj -> {
                PaymentIntent intent = (PaymentIntent) obj;
                stripeService.handlePaymentSucceeded(intent.getId(), intent.getLatestCharge(), intent.getAmount());
            });
            case "payment_intent.payment_failed" -> deserializer.getObject().ifPresent(obj -> {
                PaymentIntent intent = (PaymentIntent) obj;
                stripeService.handlePaymentFailed(intent.getId());
            });
            case "charge.refunded" -> deserializer.getObject().ifPresent(obj -> {
                Charge charge = (Charge) obj;
                if (charge.getPaymentIntent() != null) stripeService.handleRefund(charge.getPaymentIntent());
            });
            default -> log.debug("Unhandled Stripe event: {}", event.getType());
        }

        return ResponseEntity.ok("{\"received\": true}");
    }

    public record CreateIntentRequest(UUID orderId) {}
}
