package io.github.malekinasim.cdcdemo.orderservice.exception;

/** Something was asked for by id or number and does not exist. Answered with 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
