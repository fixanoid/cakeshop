package com.jpmorgan.ib.caonpd.cakeshop.client.model.res;

/**
 * Thin wrapper for responses which return a single JSON object like:
 *
 * {"result":true}
 *
 * @author Chetan Sarva
 *
 */
public class SimpleResult {

    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
