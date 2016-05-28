package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.cakeshop.client.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Contract.CodeTypeEnum;

import java.util.List;

import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;

public class ContractApiTest extends BaseApiTest {

    @Test
    public void testList() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);

        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":[{\"id\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"type\":\"contract\",\"attributes\":{\"id\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"SimpleStorage\",\"owner\":null,\"code\":\"\\ncontract SimpleStorage {\\n\\n    uint public storedData;\\n\\n    function SimpleStorage(uint initVal) {\\n        storedData = initVal;\\n    }\\n\\n    function set(uint x) {\\n        storedData = x;\\n    }\\n\\n    function get() constant returns (uint retVal) {\\n        return storedData;\\n    }\\n    \\n}\\n\",\"codeType\":\"solidity\",\"binary\":null,\"abi\":\"[{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"storedData\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"x\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"set\\\",\\\"outputs\\\":[],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"get\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"retVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"initVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"constructor\\\"}]\\n\",\"createdDate\":1464365329,\"gasEstimates\":null,\"solidityInterface\":null,\"functionHashes\":null}}],\"meta\":{\"version\":\"1.0\"}}"));
        List<Contract> contracts = contractApi.list().getDataAsList();
        assertNotNull(contracts);
        assertEquals(contracts.size(), 1);

        Contract contract = contracts.get(0);
        commonTests(contract);

    }

    @Test
    public void testGet() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"type\":\"contract\",\"attributes\":{\"id\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"SimpleStorage\",\"owner\":null,\"code\":\"\\ncontract SimpleStorage {\\n\\n    uint public storedData;\\n\\n    function SimpleStorage(uint initVal) {\\n        storedData = initVal;\\n    }\\n\\n    function set(uint x) {\\n        storedData = x;\\n    }\\n\\n    function get() constant returns (uint retVal) {\\n        return storedData;\\n    }\\n    \\n}\\n\",\"codeType\":\"solidity\",\"binary\":\"0x60606040526000357c0100000000000000000000000000000000000000000000000000000000900480632a1afcd914604b57806360fe47b114606c5780636d4ce63c146082576049565b005b6056600480505060c2565b6040518082815260200191505060405180910390f35b6080600480803590602001909190505060a3565b005b608d600480505060b1565b6040518082815260200191505060405180910390f35b806000600050819055505b50565b6000600060005054905060bf565b90565b6000600050548156\",\"abi\":\"[{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"storedData\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"x\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"set\\\",\\\"outputs\\\":[],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"get\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"retVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"initVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"constructor\\\"}]\\n\",\"createdDate\":1464365329,\"gasEstimates\":null,\"solidityInterface\":null,\"functionHashes\":null}},\"meta\":{\"version\":\"1.0\"}}"));
        Contract c = contractApi.get("0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2").getData();
        commonTests(c);
    }

    private void commonTests(Contract contract) {
        assertNotNull(contract);
        assertEquals(contract.getCodeType(), CodeTypeEnum.SOLIDITY);
        assertEquals(contract.getCreatedDate(), Long.valueOf(1464365329));
    }

}
