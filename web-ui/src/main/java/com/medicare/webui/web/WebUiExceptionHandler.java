package com.medicare.webui.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.ui.Model;

/**
 * Maps backend API failures to the friendly error page instead of an unhandled 500.
 */
@ControllerAdvice
class WebUiExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public String handleRestClient(RestClientResponseException ex, Model model) {
        model.addAttribute("status", ex.getStatusCode().value());
        model.addAttribute("error", ApiErrors.message(ex));
        return "error";
    }
}
