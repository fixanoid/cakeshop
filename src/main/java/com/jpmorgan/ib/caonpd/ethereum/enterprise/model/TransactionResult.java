package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TransactionResult {

    public static final String API_DATA_TYPE = "transaction_result";

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(getId());
        data.setType(API_DATA_TYPE);
        data.setAttributes(this);
        return data;
    }

}
