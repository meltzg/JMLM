package org.meltzg.jmlm.sync.strategies;

public class GreedySyncStrategyTest extends AbstractSyncStrategyTest {

    @Override
    protected AbstractSyncStrategy createStrategy() {
        return new GreedySyncStrategy();
    }
}