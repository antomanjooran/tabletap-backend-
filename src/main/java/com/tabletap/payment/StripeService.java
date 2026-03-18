package com.tabletap.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.tabletap.dto.response.PaymentIntentResponse;
import com.tabletap.entity.Order;
import com.tabletap.entity.Payment;
import com.tabletap.exception.ApiException;
import com.tabletap.repository.OrderRepository;
import com.tabletap.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.currency:gbp}")
    private String currency;

    private final OrderRepository   orderRepository;
    private final PaymentRepository paymentRepository;

    public StripeService(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository   = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    void init() { Stripe.apiKey = secretKey; }

    @Transactional
    public PaymentIntentResponse createPaymentIntent(UUID orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ApiException("Order not found", "ORDER_NOT_FOUND", 404));

        if ("PAID".equals(order.getPaymentStatus()))
            throw new ApiException("Order already paid", "ALREADY_PAID", 409);

        long amountPence = order.getTotal()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountPence).setCurrency(currency)
                    .putMetadata("order_id",     orderId.toString())
                    .putMetadata("table_number", String.valueOf(order.getTable().getNumber()))
                    .setDescription("TableTap Table %d Order %s"
                            .formatted(order.getTable().getNumber(), orderId.toString().substring(0, 8)))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            order.setStripePaymentIntentId(intent.getId());
            orderRepository.save(order);

            log.info("PaymentIntent created: {} for order {}", intent.getId(), orderId);
            return new PaymentIntentResponse(intent.getClientSecret(), amountPence, currency);

        } catch (StripeException e) {
            log.error("Stripe error for order {}", orderId, e);
            throw new ApiException("Payment setup failed: " + e.getMessage(), "STRIPE_ERROR", 502);
        }
    }

    @Transactional
    public void handlePaymentSucceeded(String intentId, String chargeId, long amountPence) {
        orderRepository.findAll().stream()
                .filter(o -> intentId.equals(o.getStripePaymentIntentId()))
                .findFirst().ifPresent(order -> {
                    order.setPaymentStatus("PAID");
                    orderRepository.save(order);
                    if (paymentRepository.findByStripePaymentIntent(intentId).isEmpty()) {
                        paymentRepository.save(Payment.builder().order(order)
                                .stripePaymentIntent(intentId).stripeChargeId(chargeId)
                                .amount(BigDecimal.valueOf(amountPence, 2)).currency(currency)
                                .status("succeeded").build());
                    }
                    log.info("Payment succeeded for order {}", order.getId());
                });
    }

    @Transactional
    public void handlePaymentFailed(String intentId) {
        paymentRepository.findByStripePaymentIntent(intentId).ifPresent(p -> {
            p.setStatus("failed");
            paymentRepository.save(p);
        });
    }

    @Transactional
    public void handleRefund(String intentId) {
        orderRepository.findAll().stream()
                .filter(o -> intentId.equals(o.getStripePaymentIntentId()))
                .findFirst().ifPresent(o -> { o.setPaymentStatus("REFUNDED"); orderRepository.save(o); });
        paymentRepository.findByStripePaymentIntent(intentId).ifPresent(p -> {
            p.setStatus("refunded"); paymentRepository.save(p);
        });
    }
}
