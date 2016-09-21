package com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository;

//import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Account;
import java.util.ArrayList;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author I629630
 */
//@Repository
public class WalletRepository extends BaseRepository{

    public void save(Account account) {
//        if (null != getCassandraTemplate()) {
//            getCassandraTemplate().insert(account);
//        }
    }

    public void reset() {
//        if (null != getCassandraTemplate()) {
//            getCassandraTemplate().deleteAll(Account.class);
//        }
    }

    public List<Account> list() {
//        if (null != getCassandraTemplate()) {
//            return getCassandraTemplate().select(
//                    QueryBuilder.select().all().from("account"),
//                    Account.class);
//        } else {
//            return new ArrayList<>();
//        }
     return null;
    }

}
