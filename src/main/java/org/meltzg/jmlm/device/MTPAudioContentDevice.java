package org.meltzg.jmlm.device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.meltzg.jmlm.device.storage.StorageDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.utilities.CommandRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.meltzg.jmlm.device.MTPAudioContentDevice.MountProperties.*;

@Slf4j
public class MTPAudioContentDevice extends FileSystemAudioContentDevice implements ListableDevice {
    @Getter
    @Setter
    private Map<String, String> mountProperties = initializeProperties();

    private static final String JMTP_CMD = "jmtpfs";
    private static final String DEVICE_LOC = "-device=%s,%s";
    private static final String FUSERMOUNT = "fusermount";

    static {
        System.loadLibrary("jmtp");
//        MTPContentDevice.initMTP();
    }

    private static Map<String, String> initializeProperties() {
        var properties = new HashMap<String, String>();
        for (var prop : MountProperties.values()) {
            properties.put(prop.toString(), null);
        }
        return properties;
    }

    @Override
    public MTPAudioContentDevice mount() throws IOException {
        setRootPath(Paths.get(mountDir, getId()).toString());
        var mountPath = Paths.get(getRootPath());
        if (!mountPath.toFile().exists() && !Paths.get(getRootPath()).toFile().mkdirs()) {
            throw new IOException("Could not create intermediate directories for " + getRootPath());
        }
        var props = getDeviceInfo(mountProperties.get(DEVICE_ID.toString())).toMap();
        if (props == null) {
            log.error("Could not mount device with properties {}", mountProperties);
            throw new IOException("Could not mount device");
        }
        var busLocation = props.get(BUS_LOCATION.toString());
        var devNum = props.get(DEV_NUM.toString());

        var mountResult = CommandRunner.runCommand(Arrays.asList(
                JMTP_CMD, String.format(DEVICE_LOC, busLocation, devNum), getRootPath()));

        if (mountResult.getExitValue() != 0) {
            throw new IOException("Could not mount device");
        }
        log.info("Device mounted: {}", mountProperties);
        return this;
    }

    @Getter
    @Setter
    private String mountDir = Paths.get(System.getProperty("user.home"), "mnt").toString();

    public MTPAudioContentDevice(AudioContentRepository contentRepo) {
        super(contentRepo);
    }

    public MTPAudioContentDevice(String name, AudioContentRepository contentRepo) {
        super(name, contentRepo);
    }

    public MTPAudioContentDevice(String name, AudioContentRepository contentRepo, Map<String, String> mountProperties) {
        super(name, contentRepo);
        this.mountProperties = mountProperties;
    }

    public MTPAudioContentDevice(String name, AudioContentRepository contentRepo, Map<String, String> mountProperties, String mountDir) {
        super(name, contentRepo);
        this.mountProperties = mountProperties;
        this.mountDir = mountDir;
    }

    @Override
    public List<Map<String, String>> getAllDeviceMountProperties() throws IOException {
        var devices = getDevicesInfo();
        return devices.stream()
                .map(MTPDeviceInfo::toMap)
                .collect(Collectors.toList());
    }

    @Override
    protected StorageDevice getStorageDevice(Path path) throws IOException, URISyntaxException {
        var storage = getStorageDevice(path.toString(), mountProperties.get(DEVICE_ID.toString()));
        if (storage == null) {
            log.error("Could not get storage device for path {}", path);
            throw new IOException("Could not get storage device");
        }
        return storage;
    }

    @Override
    public void unmount() throws IOException {
        var unmountResult = CommandRunner.runCommand(Arrays.asList(FUSERMOUNT, "-u", getRootPath()));
        if (unmountResult.getExitValue() != 0) {
            throw new IOException("Could not unmount device");
        }
        log.info("Device unmounted: {}", mountProperties);
    }

    @AllArgsConstructor
    public enum MountProperties {
        DEVICE_ID("deviceId"),
        FRIENDLY_NAME("friendlyName"),
        DESCRIPTION("description"),
        MANUFACTURER("manufacturer"),
        SERIAL("serial"),
        DEV_NUM("devNum"),
        BUS_LOCATION("busLocation");

        private final String value;
    }

    private native StorageDevice getStorageDevice(String path, String deviceId);

    private static native List<MTPDeviceInfo> getDevicesInfo();

    private static native MTPDeviceInfo getDeviceInfo(String id);

    @Value
    public class MTPDeviceInfo {
        private final String deviceId;
        private final String friendlyName;
        private final String description;
        private final String manufacturer;
        private final String serial;

        long busLocation;
        long devNum;

        private Map<String, String> toMap() {
            var map = new HashMap<String, String>();

            map.put(DEVICE_ID.toString(), deviceId);
            map.put(FRIENDLY_NAME.toString(), friendlyName);
            map.put(DESCRIPTION.toString(), description);
            map.put(MANUFACTURER.toString(), manufacturer);
            map.put(SERIAL.toString(), serial);
            map.put(BUS_LOCATION.toString(), Long.toString(busLocation));
            map.put(DEV_NUM.toString(), Long.toString(devNum));

            return map;
        }
    }
}
