package com.lifeos.progression.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a Character is not found.
 */
public class CharacterNotFoundException extends ProgressionException {

    public CharacterNotFoundException(Long id) {
        super("Character not found with ID: " + id, HttpStatus.NOT_FOUND);
    }

    public CharacterNotFoundException(String userId) {
        super("Character not found for User ID: " + userId, HttpStatus.NOT_FOUND);
    }
}
