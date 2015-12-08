package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class APIResponse {

    private APIData data;
    private List<APIError> errors;
    private Map<String, String> meta;

    public APIResponse() {
        this.errors = new ArrayList<APIError>();
    }

    public void addError(APIError error) {
        getErrors().add(error);
    }

    public APIData getData() {
        return data;
    }
    public void setData(APIData data) {
        this.data = data;
    }

    public List<APIError> getErrors() {
        return errors;
    }
    public void setErrors(List<APIError> errors) {
        this.errors = errors;
    }

    public Map<String, String> getMeta() {
        return meta;
    }
    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }
}
