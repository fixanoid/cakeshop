package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;


public class Contract  {

  private String code = null;
  private String address = null;


  /**
   * Binary contract code
   **/
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }


  /**
   * Ethereum address
   **/
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }



  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Contract {\n");

    sb.append("  code: ").append(code).append("\n");
    sb.append("  address: ").append(address).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
