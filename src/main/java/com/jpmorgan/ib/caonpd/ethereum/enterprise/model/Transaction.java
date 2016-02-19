package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI.Function;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bouncycastle.util.encoders.Hex;

@Entity
@Table(name="TRANSACTIONS", schema="PUBLIC")
public class Transaction {

    public class Input {
        private String method;
        private Object[] args;

        public Input(String method, Object[] args) {
            this.method = method;
            this.args = args;
        }

        public String getMethod() {
            return method;

        }
        public void setMethod(String method) {
            this.method = method;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] args) {
            this.args = args;
        }
    }

    public static final String API_DATA_TYPE = "transaction";

	public static enum Status {
		pending,
		committed
	}

	@Id
	private String id;

	private Status status;

	private String nonce;

	private String blockId;

	private Long blockNumber;

	private Long transactionIndex;

	private String from;
	private String to;

	private Long value;
	private Long gas;
	private Long gasPrice;

	@Lob
	@Column(length=Integer.MAX_VALUE)
	private String input;

	@Transient
	private Input decodedInput;

	private Long cumulativeGasUsed;
	private Long gasUsed;

	private String contractAddress;

	@ElementCollection(fetch=FetchType.EAGER)
	private List<String> logs;


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

	public Long getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(Long blockNumber) {
		this.blockNumber = blockNumber;
	}

	public Long getTransactionIndex() {
		return transactionIndex;
	}

	public void setTransactionIndex(Long transactionIndex) {
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

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	public Long getGas() {
		return gas;
	}

	public void setGas(Long gas) {
		this.gas = gas;
	}

	public Long getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(Long gasPrice) {
		this.gasPrice = gasPrice;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public Long getCumulativeGasUsed() {
		return cumulativeGasUsed;
	}

	public void setCumulativeGasUsed(Long cumulativeGasUsed) {
		this.cumulativeGasUsed = cumulativeGasUsed;
	}

	public Long getGasUsed() {
		return gasUsed;
	}

	public void setGasUsed(Long gasUsed) {
		this.gasUsed = gasUsed;
	}

	public String getContractAddress() {
		return contractAddress;
	}

	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}

	public List<String> getLogs() {
		return logs;
	}

	public void setLogs(List<String> logs) {
		this.logs = logs;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
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

    public void decodeInput(ContractABI abi) {
        if (getContractAddress() != null || getTo() == null
                || getInput() == null || getInput().isEmpty()) {

            return;
        }

        String input = getInput();
        System.out.println(input);

        for (Function func : abi.getFunctions()) {
            String sig = "0x" + Hex.toHexString(func.encodeSignature());
            if (input.startsWith(sig)) {
                decodedInput = new Input(func.name, func.decodeHex(input));
                return;
            }
        }
    }

    public Input getDecodedInput() {
        return decodedInput;
    }

    public void setDecodedInput(Input decodedInput) {
        this.decodedInput = decodedInput;
    }

}
