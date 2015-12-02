package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;

public class Contract  {

	/**
	 * Ethereum address of contract
	 */
  private String address;

  /**
   * Original source code (not yet supported)
   */
  private String code;

  /**
   * Original source code type (not yet supported)
   */
  private CodeType codeType;

  /**
   * Binary source code
   */
  private String binary;


  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }

  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }

	public CodeType getCodeType() {
		return codeType;
	}
	public void setCodeType(CodeType codeType) {
		this.codeType = codeType;
	}

	public String getBinary() {
		return binary;
	}
	public void setBinary(String binary) {
		this.binary = binary;
	}

  @Override
  public String toString()  {
      return ToStringBuilder.reflectionToString(this);
  }
}
