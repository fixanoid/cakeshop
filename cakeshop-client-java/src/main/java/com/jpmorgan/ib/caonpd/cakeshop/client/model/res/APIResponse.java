package com.jpmorgan.ib.caonpd.cakeshop.client.model.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonInclude(Include.NON_NULL)
public class APIResponse<T> {

    /**
     * Response data. Should be an instance of either {@link APIData} or List&lt;APIData&gt;.
     */
    private T data;

    private List<APIError> errors;
    private Map<String, String> meta;

    public APIResponse() {
    }

    public void addError(APIError error) {
        if (getErrors() == null) {
            this.errors = new ArrayList<APIError>();
        }
        getErrors().add(error);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
