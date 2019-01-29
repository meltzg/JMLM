package org.meltzg.jmlm.sync.strategies;

import org.junit.Test;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;

public class LazySyncStrategyTest extends AbstractSyncStrategyTest {

    @Override
    protected AbstractSyncStrategy createStrategy() {
        return new LazySyncStrategy();
    }

    @Override
    @Test(expected = InsufficientSpaceException.class)
    public void createPlanNeedsShuffle() throws InsufficientSpaceException {
        super.createPlanNeedsShuffle();
    }
}