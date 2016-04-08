package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.codahale.metrics.FastMeter;
import com.codahale.metrics.MetricRegistry;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.stereotype.Service;

@Service
public class MetricsBlockListener implements BlockListener {

    private final MetricRegistry metrics;
    private final FastMeter blockMeter;
    private final FastMeter txnMeter;

    private CircularFifoQueue<Double> txnPerMin;
    private CircularFifoQueue<Double> txnPerSec;
    private CircularFifoQueue<Double> blockPerMin;

    private MetricCollector metricCollector;

    class MetricCollector extends Thread {
        boolean running = true;
        @Override
        public void run() {
            int i = 0;
            while (running) {
                System.out.println("Collecting stats");
                if (i == 0) {
                    blockPerMin.add(getBlockPerMin());
                    txnPerMin.add(getTxnPerMin());
                }
                txnPerSec.add(getTxnPerSec());
                i++;
                if (i >= 60) {
                    i = 0;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public MetricsBlockListener() {
        metrics = new MetricRegistry();
        blockMeter = metrics.register("blockMeter", new FastMeter());
        txnMeter = metrics.register("txnMeter", new FastMeter());

        txnPerMin = new CircularFifoQueue<>(1000);
        txnPerSec = new CircularFifoQueue<>(1000);
        blockPerMin = new CircularFifoQueue<>(1000);

        this.metricCollector = new MetricCollector();
        this.metricCollector.start();
    }

    public void shutdown() {
        this.metricCollector.running = false;
    }

    @Override
    public void blockCreated(Block block) {
        blockMeter.mark();
        if (block.getTransactions() != null && block.getTransactions().size() > 0)  {
            txnMeter.mark(block.getTransactions().size());
        }
    }

    public double getTxnPerSec() {
        return txnMeter.getFiveSecondRate();
    }

    public double getTxnPerMin() {
        return txnMeter.getOneMinuteRate();
    }

    public double getBlockPerMin() {
        return blockMeter.getOneMinuteRate();
    }

}
