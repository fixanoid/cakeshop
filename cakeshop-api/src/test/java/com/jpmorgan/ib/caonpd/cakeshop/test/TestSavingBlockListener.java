package com.jpmorgan.ib.caonpd.cakeshop.test;

import com.jpmorgan.ib.caonpd.cakeshop.db.SavingBlockListener;
import com.jpmorgan.ib.caonpd.cakeshop.model.Block;

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
