package org.meltzg.jmlm.sync.strategies;

import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RankedSyncStrategy extends AbstractSyncStrategy {
    private List<Class<? extends AbstractSyncStrategy>> rankedStrategies;

    public RankedSyncStrategy(String[] rankedStrategyClassNames) throws ClassNotFoundException {
        this.rankedStrategies = new ArrayList<>();
        for (var className : rankedStrategyClassNames) {
            var clazz = Class.forName(className);
            if (!AbstractSyncStrategy.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Strategies must extend " + AbstractSyncStrategy.class.getCanonicalName());
            }
            rankedStrategies.add((Class<? extends AbstractSyncStrategy>) clazz);
        }
    }

    @Override
    protected void planDeviceTransfers(List<AudioContent> desiredContentInfo, Map<Long, ContentSyncStatus> syncStatuses, Map<UUID, Long> destinationLibCapacities, Map<UUID, Long> destinationLibFreeSpace) throws InsufficientSpaceException {
        for (var strategyClass : rankedStrategies) {
            try {
                var strategy = strategyClass.getDeclaredConstructor().newInstance();
                plan = strategy.createPlan(desiredContentInfo, syncStatuses, destinationLibCapacities, destinationLibFreeSpace);
                return;
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new IllegalStateException("Could not instantiate strategy " + strategyClass.getCanonicalName());
            } catch (InsufficientSpaceException e) {
                System.err.println("Could not use " + strategyClass.getCanonicalName());
            }
        }
        throw new InsufficientSpaceException("Could not sync device using any strategy");
    }
}
