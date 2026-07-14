package com.lifeos.progression.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a Skill or Skill Category is not found.
 */
public class SkillNotFoundException extends ProgressionException {

    public SkillNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public SkillNotFoundException(Long id) {
        super("Skill not found with ID: " + id, HttpStatus.NOT_FOUND);
    }
}
