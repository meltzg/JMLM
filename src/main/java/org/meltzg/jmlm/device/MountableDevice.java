package org.meltzg.jmlm.device;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MountableDevice extends Closeable {
    Map<String, String> getMountProperties();

    void setMountProperties(Map<String, String> mountProperties);

    MountableDevice mount() throws IOException;

    void unmount() throws IOException;

    default void close() throws IOException {
        unmount();
    }

    default Map<String, String> toMap(String[][] props) {
        return Stream.of(props).collect(Collectors.toMap(prop -> prop[0], prop -> prop[1]));
    }
}
