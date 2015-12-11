package com.jpmorgan.ib.caonpd.ethereum.enterprise.controller;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import org.springframework.stereotype.Controller;

@Controller
public class BaseController {

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> handleException(APIException ex) {

        APIResponse res = new APIResponse();
        res.addError(new APIError("", HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Internal server error"));
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleAllException(Exception ex) {

        APIResponse res = new APIResponse();

        if (ex instanceof ResourceAccessException) {
            res.addError(new APIError(null, HttpStatus.SERVICE_UNAVAILABLE.toString(), ex.getMessage()));
            return new ResponseEntity<>(res, HttpStatus.SERVICE_UNAVAILABLE);
        }

        res.addError(new APIError("", HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Internal server error"));
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
