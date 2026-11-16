package io.github.malekinasim.cdcdemo.orderservice.exception;

/** A business rule of the order lifecycle was broken. Answered with 409. */
public class OrderRuleViolationException extends RuntimeException {

    public OrderRuleViolationException(String message) {
        super(message);
    }
}
