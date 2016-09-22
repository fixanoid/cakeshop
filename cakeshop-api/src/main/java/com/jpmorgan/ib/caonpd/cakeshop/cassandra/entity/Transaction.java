package com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity;


import com.jpmorgan.ib.caonpd.cakeshop.cassandra.model.Input;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Function;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bouncycastle.util.encoders.Hex;
//import org.springframework.cassandra.core.Ordering;
//import org.springframework.cassandra.core.PrimaryKeyType;
//import org.springframework.data.annotation.Transient;
//import org.springframework.data.cassandra.mapping.Column;
//import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
//import org.springframework.data.cassandra.mapping.Table;


//@Table(value = "transaction")
public class Transaction {

    public static final String API_DATA_TYPE = "transaction";

	public static enum Status {
		pending,
		committed
	}

    //primary keys
	
//    @PrimaryKeyColumn(
//            name = "blockNumber",
//            ordinal = 2,
//            type = PrimaryKeyType.CLUSTERED,
//            ordering = Ordering.DESCENDING)
	private BigInteger blockNumber;
//    @PrimaryKeyColumn(
//            name = "contractAddress", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String contractAddress;
//    @PrimaryKeyColumn(
//            name = "to_address", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	private String to;
//    @PrimaryKeyColumn(
//            name = "id", ordinal = 3)
	private String id;
    //rest of the columns
//    @Column
	private String blockId;    
//    @Column
	private String status;
//    @Column
	private String nonce;    
//    @Column
	private BigInteger transactionIndex;
//    @Column(value = "from_address")
	private String from;    
//    @Column
	private BigInteger value;
//    @Column
	private BigInteger gas;
//    @Column
	private BigInteger gasPrice;
//	@Column
	private String input;
//    @Column
	private BigInteger cumulativeGasUsed;
//    @Column
	private BigInteger gasUsed;    
//    @Transient
    private Input decodedInput;
//    @Transient
    private List<Event> logs;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public BigInteger getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(BigInteger blockNumber) {
		this.blockNumber = blockNumber;
	}

	public BigInteger getTransactionIndex() {
		return transactionIndex;
	}

	public void setTransactionIndex(BigInteger transactionIndex) {
		this.transactionIndex = transactionIndex;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public BigInteger getValue() {
		return value;
	}

	public void setValue(BigInteger value) {
		this.value = value;
	}

	public BigInteger getGas() {
		return gas;
	}

	public void setGas(BigInteger gas) {
		this.gas = gas;
	}

	public BigInteger getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(BigInteger gasPrice) {
		this.gasPrice = gasPrice;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public BigInteger getCumulativeGasUsed() {
		return cumulativeGasUsed;
	}

	public void setCumulativeGasUsed(BigInteger cumulativeGasUsed) {
		this.cumulativeGasUsed = cumulativeGasUsed;
	}

	public BigInteger getGasUsed() {
		return gasUsed;
	}

	public void setGasUsed(BigInteger gasUsed) {
		this.gasUsed = gasUsed;
	}

	public String getContractAddress() {
		return contractAddress;
	}

	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

    public void decodeContractInput(ContractABI abi) {
        if (!getContractAddress().equalsIgnoreCase("0x") || getTo() == null
                || getInput() == null || getInput().isEmpty()) {

            return;
        }

        final String inputStr = getInput();
        //System.out.println(input);

        Function func = abi.findFunction(new Predicate<ContractABI.Function>() {
            @Override
            public boolean evaluate(Function f) {
                return inputStr.startsWith("0x" + Hex.toHexString(f.encodeSignature()));
            }
        });

        if (func != null) {
            decodedInput = new Input(func.name, func.decodeHex(inputStr).toArray());
        }
    }
    
    public void decodeDirectTxnInput(String method) {
        final String directInput = getInput();
        ObjectMapper mapper = new ObjectMapper();
        Object [] data;
        try {
            data = mapper.readValue(new String(Hex.decode(directInput.replaceFirst("0x", ""))), Object [].class );
            decodedInput = new Input(method, data); 
        } catch (IOException ex) {
            Logger.getLogger(Transaction.class.getName()).log(Level.SEVERE, null, ex);
        }              
    }

    public Input getDecodedInput() {
        return decodedInput;
    }

    public void setDecodedInput(Input decodedInput) {
        this.decodedInput = decodedInput;
    }

    /**
     * @return the events
     */
    public List<Event> getLogs() {
        return logs;
    }

    /**
     * @param logs the events to set
     */
    public void setLogs(List<Event> logs) {
        this.logs = logs;
    }

}
