package com.fitting.orderservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s con ID %d no encontrado", resource, id));
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
