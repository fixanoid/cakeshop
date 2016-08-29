package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Block;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.LatestBlockNumber;
import java.math.BigInteger;

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
public class BlockRepository {
    
    @Autowired(required = false)
    private CassandraOperations cassandraTemplate;
    
    private CachedPreparedStatementCreator creatorBlock;
    private CachedPreparedStatementCreator creatorLatest;
    
    public void save(Block block) {
        cassandraTemplate.insert(block);
    }
    
    public void save(LatestBlockNumber blockNumber) { 
        cassandraTemplate.update(blockNumber);
    }
    
    public Block getByNumber(final BigInteger number) {

        Select select = QueryBuilder.select().all().from("block").
                where(QueryBuilder.eq("blockNumber", number)).limit(1).allowFiltering();
        
        if (null == creatorBlock) {
            creatorBlock = new CachedPreparedStatementCreator(select.getQueryString());
        }
        Block block = cassandraTemplate.query(creatorBlock, new PreparedStatementBinder() {
            @Override
            public BoundStatement bindValues(PreparedStatement ps) throws DriverException {
                BoundStatement bound = new BoundStatement(ps);
                bound.setVarint("blockNumber", number);
                return bound;
            }
        }, new ResultSetExtractor<Block>() {
            @Override
            public Block extractData(ResultSet resultSet) throws DriverException, DataAccessException {
                return getBlock(resultSet.one());
            }
        });

        return block;
    }
    
    public Block getLatest() {
        Select select = QueryBuilder.select().all().from("latest_block").limit(1);  
        
        if(null == creatorLatest) {
            creatorLatest = new CachedPreparedStatementCreator(select.getQueryString());
        }
        
        BigInteger latest = cassandraTemplate.query(creatorLatest,  new ResultSetExtractor<BigInteger>() {
            @Override
            public BigInteger extractData(ResultSet resultSet) throws DriverException, DataAccessException {
                if (resultSet.iterator().hasNext())
                    return resultSet.one().getVarint("blocknumber");
                else
                    return null;
            }
        });
        
        return latest != null ? getByNumber(latest) : null;
    }
    
    public void reset() {
        cassandraTemplate.deleteAll(Block.class);
    }
    
	private Block getBlock(Row row) {
		if (null != row) {
			Block block = new Block();
			block.withNunmer(row.getVarint("blockNumber")).withId(row.getString("id"))
					.withExtraData(row.getString("extraData")).withGasLimit(row.getVarint("gasLimit"))
					.withGasUsed(row.getVarint("gasUsed")).withLogsBloom(row.getString("logsBloom"))
					.withMiner(row.getString("miner")).withNonce(row.getString("nonce"))
					.withParentId(row.getString("parentId")).withSha3Uncles(row.getString("sha3Uncles"))
					.withStateRoot(row.getString("stateRoot")).withTimestamp(row.getVarint("timestamp"))
					.withTotalDifficulty(row.getVarint("totalDifficulty"))
					.withTransactions(row.getList("transactions", String.class))
					.withTransactionsRoot(row.getString("transactionsRoot"))
					.withUncles(row.getList("uncles", String.class));

			return block;
		}
		return null;
	}
    
}
