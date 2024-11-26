package com.provoly.error;

import java.util.NoSuchElementException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
@ApplicationScoped
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    Logger log;

    @Override
    public Response toResponse(Exception exception) {
        log.error("Error :", exception);

        return switch (exception) {
            case AlreadyExistsException e -> Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorDto(Response.Status.CONFLICT.getStatusCode(), e.getMessage())).build();
            case ForbiddenException e -> Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorDto(Response.Status.FORBIDDEN.getStatusCode(), e.getMessage())).build();
            case NotFoundException e -> Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorDto(Response.Status.NOT_FOUND.getStatusCode(), e.getMessage())).build();
            case NoSuchElementException e -> Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorDto(Response.Status.NOT_FOUND.getStatusCode(), e.getMessage())).build();
            case BadRequestException e -> Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDto(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage())).build();
            case IllegalArgumentException e -> Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDto(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage())).build();
            default -> Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorDto(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), buildMessage(exception)))
                    .build();
        };

    }

    private String buildMessage(Exception e) {
        var msg = "Unhandled exception thrown : " + e.getClass().getName();
        if (e.getMessage() != null) {
            msg += " Message: " + e.getMessage() + ".";
        }
        msg += " See logs for complete stack";
        return msg;
    }
}
