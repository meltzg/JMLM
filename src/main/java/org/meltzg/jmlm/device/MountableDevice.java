package org.meltzg.jmlm.device;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MountableDevice extends AutoCloseable {
    Map<String, String> getMountProperties();
    void setMountProperties(Map<String, String> mountProperties);

    void mount() throws IOException;

    void unmount() throws IOException;

    default void close() throws IOException {
        unmount();
    }

    default Map<String, String> toMap(String[][] props) {
        return Stream.of(props).collect(Collectors.toMap(prop -> prop[0], prop -> prop[1]));
    }
}
