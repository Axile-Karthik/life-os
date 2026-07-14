package com.lifeos.progression.common.exception;

import com.lifeos.shared.kernel.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Base exception for progression module.
 */
public class ProgressionException extends ApiException {

    public ProgressionException(String message, HttpStatus status) {
        super(message, status);
    }

    public ProgressionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
