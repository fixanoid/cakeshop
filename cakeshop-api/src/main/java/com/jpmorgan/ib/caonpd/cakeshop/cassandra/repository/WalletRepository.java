package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Account;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Repository;

/**
 *
 * @author I629630
 */
@Repository
public class WalletRepository {
    
    @Autowired(required = false)
    private CassandraOperations cassandraTemplate;
        
    public void save (Account account) {
        cassandraTemplate.insert(account);
    }
    
    public void reset () {
        cassandraTemplate.deleteAll(Account.class);
    }
    
    public List<Account> list() {
        return cassandraTemplate.select(
                QueryBuilder.select().all().from("account"), 
                Account.class);
    }
    
}
