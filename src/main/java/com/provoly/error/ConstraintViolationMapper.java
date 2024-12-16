package com.provoly.error;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(new ErrorDto(Response.Status.FORBIDDEN.getStatusCode(), buildMessage(exception)))
                .build();
    }

    private String buildMessage(ConstraintViolationException exception) {
        return exception.getConstraintViolations()
                .stream()
                .map(this::getMessageForViolation)
                .reduce((a, b) -> a + "; " + b)
                .orElse(null);
    }

    private String getMessageForViolation(ConstraintViolation<?> constraintViolation) {
        String propertyName = "";
        Integer index = null; // To manage constraint violations on entities collection

        for (Path.Node node : constraintViolation.getPropertyPath()) {
            propertyName = node.getName();
            index = node.getIndex();
        }
        return index == null ? "'%s' %s".formatted(propertyName, constraintViolation.getMessage())
                : "Entity n°%s : '%s' %s".formatted(index, propertyName, constraintViolation.getMessage());
    }
}
