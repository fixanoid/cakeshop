package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;

@Controller
public class BaseController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleException(APIException ex) {
        APIResponse res = new APIResponse();
        String rootCause = ExceptionUtils.getRootCauseMessage(ex);

        if (ex instanceof APIException) {
            // try to pass back more specific exceptions

            Throwable cause = ex.getCause();
            if (cause instanceof ResourceAccessException) {
                // server down (failed connect)
                res.addError(new APIError(null, HttpStatus.SERVICE_UNAVAILABLE.toString(), "Service unavailable", rootCause));
                return new ResponseEntity<>(res, HttpStatus.SERVICE_UNAVAILABLE);

            } else if (cause instanceof HttpClientErrorException) {
                // server returned 4xx
                HttpClientErrorException err = (HttpClientErrorException) cause;
                res.addError(new APIError(null, err.getStatusCode().toString(), err.getStatusText(), rootCause));
                return new ResponseEntity<>(res, err.getStatusCode());

            } else if (cause instanceof HttpServerErrorException) {
                // server returned 5xx
                HttpServerErrorException err = (HttpServerErrorException) cause;
                res.addError(new APIError(null, err.getStatusCode().toString(), err.getStatusText(), rootCause));
                return new ResponseEntity<>(res, err.getStatusCode());
            }
        }

        res.addError(new APIError(null, HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Internal server error", rootCause));
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
