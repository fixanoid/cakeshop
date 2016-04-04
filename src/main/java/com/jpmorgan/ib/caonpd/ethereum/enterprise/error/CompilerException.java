package com.jpmorgan.ib.caonpd.ethereum.enterprise.error;

import java.util.List;

public class CompilerException extends APIException {

    private List<String> errors;

    public CompilerException(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

}
