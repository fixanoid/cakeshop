package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.db.SavingBlockListener;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class TestSavingBlockListener extends SavingBlockListener {

    public TestSavingBlockListener() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void blockCreated(Block block) {
        System.out.println("SAVING BLOCK!!!!");
        saveBlock(block);
    }

}
