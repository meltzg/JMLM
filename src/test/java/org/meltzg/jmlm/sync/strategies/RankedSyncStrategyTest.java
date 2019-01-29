package org.meltzg.jmlm.sync.strategies;

public class RankedSyncStrategyTest extends AbstractSyncStrategyTest {

    @Override
    protected AbstractSyncStrategy createStrategy() throws ClassNotFoundException {
        return new RankedSyncStrategy(new String[]{
                LazySyncStrategy.class.getCanonicalName(),
                GreedySyncStrategy.class.getCanonicalName()
        });
    }
}