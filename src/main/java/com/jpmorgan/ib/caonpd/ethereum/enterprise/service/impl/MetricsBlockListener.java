package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.codahale.metrics.FastMeter;
import com.codahale.metrics.SimpleRollingMeter;
import com.codahale.metrics.TickListener;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MetricsBlockListener implements BlockListener, TickListener {

    public class Metric {
        public long timestamp;
        public double value;

        public Metric(long timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

	@Autowired(required = false)
	private SimpMessagingTemplate template;

    private FastMeter txnPerSecMeter;
    private SimpleRollingMeter txnPerMinMeter;
    private SimpleRollingMeter blockPerMinMeter;

    private CircularFifoQueue<Metric> txnPerMin;
    private CircularFifoQueue<Metric> txnPerSec;
    private CircularFifoQueue<Metric> blockPerMin;

    private MetricCollector metricCollector;

    class MetricCollector extends Thread {
        boolean running = true;
        @Override
        public void run() {
            while (running) {
                long ts = timestamp();
                blockPerMin.add(new Metric(ts, blockPerMinMeter.getRate()));
                txnPerMin.add(new Metric(ts, txnPerMinMeter.getRate()));
                txnPerSec.add(new Metric(ts, txnPerSecMeter.getRate()));

                // always tick all metrics each sec
                blockPerMinMeter.mark(0);
                txnPerSecMeter.mark(0);
                txnPerMinMeter.mark(0);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public MetricsBlockListener() {
        txnPerSecMeter = new FastMeter(1, this);
        txnPerMinMeter = new SimpleRollingMeter();
        blockPerMinMeter = new SimpleRollingMeter();

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
        blockPerMinMeter.mark();
        if (block.getTransactions() != null && block.getTransactions().size() > 0)  {
            txnPerSecMeter.mark(block.getTransactions().size());
            txnPerMinMeter.mark(block.getTransactions().size());
        } else {
            // always tick, even if no txns
            txnPerSecMeter.mark(0);
            txnPerMinMeter.mark(0);
        }
    }

    @Override
    public void nextTick(double val) {
        template.convertAndSend(
                "/topic/metrics/txnPerSec",
                APIResponse.newSimpleResponse(new Metric(timestamp(), val)));
    }

    public Metric getTxnPerSec() {
        return new Metric(timestamp(), txnPerSecMeter.getRate());
    }

    public Metric getTxnPerMin() {
        return new Metric(timestamp(), txnPerMinMeter.getRate());
    }

    public Metric getBlockPerMin() {
        return new Metric(timestamp(), blockPerMinMeter.getRate());
    }

    private long timestamp() {
        return System.currentTimeMillis() / 1000;
    }

}
