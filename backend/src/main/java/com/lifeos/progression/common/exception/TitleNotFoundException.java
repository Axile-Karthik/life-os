package com.lifeos.progression.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a Title is not found.
 */
public class TitleNotFoundException extends ProgressionException {

    public TitleNotFoundException(Long id) {
        super("Title not found with ID: " + id, HttpStatus.NOT_FOUND);
    }
}
