package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event;

import java.util.ArrayList;
import java.util.List;

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
public class EventsRepository {
    
    @Autowired(required = false)
    private CassandraOperations cassandraTemplate;
    
    private CachedPreparedStatementCreator creator;
    
    
    public void save (Event event) {
        cassandraTemplate.insert(event);
    }
    
    public void save (List <Event> events) {
        cassandraTemplate.insert(events);
    }
    
    public void reset() {
        cassandraTemplate.deleteAll(Event.class);
    }
    
    public List<Event> getEvents(final String transactionId) {

        Select.Where select = QueryBuilder.select().all().from("events")
                .where(QueryBuilder.eq("transactionId", transactionId));
        if (null == creator) {
            creator = new CachedPreparedStatementCreator(select.getQueryString());
        }
        List<Event> events = cassandraTemplate.query(creator, new PreparedStatementBinder() {
            
            @Override
            public BoundStatement bindValues(PreparedStatement ps) throws DriverException {
                BoundStatement bound = new BoundStatement(ps);
                bound.setString("transactionId", transactionId);
                return bound;
            }
        }, new ResultSetExtractor<List<Event>>() {
            
            List<Event> list = new ArrayList();
            @Override
            public List<Event> extractData(ResultSet resultSet) throws DriverException, DataAccessException {
                for (Row row : resultSet) {
                    Event event = new Event();
                    event.setId(row.getVarint("id"));
                    event.setBlockId(row.getString("blockId"));
                    event.setBlockNumber(row.getVarint("blockNumber"));
                    event.setContractId(row.getString("contractId"));
                    event.setData(row.getList("data", String.class));
                    event.setLogIndex(row.getVarint("logIndex"));
                    event.setName(row.getString("name"));
                    event.setTransactionId(row.getString("transactionId"));
                    list.add(event);
                }
                return list;
            }
        });
        return events;
    }
    
}
