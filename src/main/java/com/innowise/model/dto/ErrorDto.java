package com.innowise.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized error response body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    private int errorCode;
    private String errorMessage;
}