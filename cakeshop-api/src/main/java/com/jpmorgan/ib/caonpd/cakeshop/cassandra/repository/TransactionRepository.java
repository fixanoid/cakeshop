package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.CachedPreparedStatementCreator;
import org.springframework.cassandra.core.PreparedStatementBinder;
import org.springframework.cassandra.core.ResultSetExtractor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;

/**
 *
 * @author I629630
 */
@Repository
public class TransactionRepository {

    @Autowired
    private CassandraOperations cassandraTemplate;

    private CachedPreparedStatementCreator creatorTrans;
    private CachedPreparedStatementCreator creatorContract;
    private Select selectContractTransactions;
    private Select selectContractCreation;

    public void save(Transaction txn) {
        cassandraTemplate.insert(txn);
    }

    public void save(List<Transaction> txns) {
        cassandraTemplate.insert(txns);
    }

    public void reset() {
        cassandraTemplate.deleteAll(Transaction.class);
    }

    public List<Transaction> listForContractId(String id) {
        List<Transaction> allTx = new ArrayList<>();
        
        if (StringUtils.isNotBlank(id)) {
            if (null == selectContractTransactions) {
                selectContractTransactions = QueryBuilder.select().all().from("transaction")
                        .where(QueryBuilder.eq("contractAddress", "0x")).and(QueryBuilder.eq("to_address", id)).orderBy(QueryBuilder.asc("blockNumber"));
            }
            if (null == selectContractCreation) {
                selectContractCreation = QueryBuilder.select().all().from("transaction")
                        .where(QueryBuilder.eq("contractAddress", id)).and(QueryBuilder.eq("to_address",  "0x")).orderBy(QueryBuilder.asc("blockNumber"));
            }
            if (null == creatorTrans) {
                creatorTrans = new CachedPreparedStatementCreator(selectContractTransactions.getQueryString());
            }
            if (null == creatorContract) {
                creatorContract = new CachedPreparedStatementCreator(selectContractCreation.getQueryString());
            }
            List<Transaction> creationList = getResult(id, "to_address", creatorTrans);
            List<Transaction> txList = getResult(id, "contractAddress", creatorContract);

            // merge lists
            if (creationList != null && !creationList.isEmpty()) {
                allTx.addAll(creationList);
            }

            if (txList != null && !txList.isEmpty()) {
                allTx.addAll(txList);
            }
        }

        return allTx;
    }

    private List<Transaction> getResult(final String id, final String paramName, CachedPreparedStatementCreator creator) {

        List<Transaction> txns = cassandraTemplate.query(creator, new PreparedStatementBinder() {

            @Override
            public BoundStatement bindValues(PreparedStatement ps) throws DriverException {
                BoundStatement bound = new BoundStatement(ps);
                if (paramName.equals("to_address")) {
                    bound.setString("contractAddress", "0x");
                } else {
                    bound.setString("to_address", "0x");
                }
                bound.setString(paramName, id);
                return bound;
            }
        }, new ResultSetExtractor<List<Transaction>>() {

            List<Transaction> list = new ArrayList<>();

            @Override
            public List<Transaction> extractData(ResultSet resultSet) throws DriverException, DataAccessException {
                for (Row row : resultSet) {
                    Transaction txn = new Transaction();
                    txn.setId(row.getString("id"));
                    txn.setBlockId(row.getString("blockId"));
                    txn.setBlockNumber(row.getVarint("blockNumber"));
                    txn.setContractAddress(row.getString("contractAddress"));
                    txn.setCumulativeGasUsed(row.getVarint("cumulativeGasUsed"));
                    txn.setFrom(row.getString("from_address"));
                    txn.setGas(row.getVarint("gas"));
                    txn.setGasPrice(row.getVarint("gasPrice"));
                    txn.setGasUsed(row.getVarint("gasUsed"));
                    txn.setInput(row.getString("input"));
                    txn.setNonce(row.getString("nonce"));
                    txn.setStatus(row.getString("status"));
                    txn.setTo(row.getString("to_address"));
                    txn.setTransactionIndex(row.getVarint("transactionIndex"));
                    txn.setValue(row.getVarint("value"));
                    list.add(txn);
                }
                return list;
            }
        });

        return txns;
    }

}
