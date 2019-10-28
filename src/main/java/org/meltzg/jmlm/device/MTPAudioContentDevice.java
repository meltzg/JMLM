package org.meltzg.jmlm.device;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.meltzg.jmlm.device.storage.StorageDevice;
import org.meltzg.jmlm.filesystem.mtp.MTPFileSystemProvider;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.utilities.CommandRunner;

import javax.persistence.Entity;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MTPAudioContentDevice extends FileSystemAudioContentDevice implements ListableDevice {
    @Getter
    @Setter
    private Map<String, String> mountProperties = initializeProperties();

    private static final String JMTP_CMD = "jmtpfs";
    private static final String DEVICE_LOC = "-device=%s,%s";
    private static final String FUSERMOUNT = "fusermount";

    private static Map<String, String> initializeProperties() {
        var properties = new HashMap<String, String>();
        for (var prop : MTPFileSystemProvider.MountProperties.values()) {
            properties.put(prop.toString(), null);
        }
        return properties;
    }

    @Override
    public MTPAudioContentDevice mount() throws IOException {
//        setRootPath(Paths.get(mountDir, getId()).toString());
//        var mountPath = Paths.get(getRootPath());
//        if (!mountPath.toFile().exists() && !Paths.get(getRootPath()).toFile().mkdirs()) {
//            throw new IOException("Could not create intermediate directories for " + getRootPath());
//        }
//        var props = getDeviceInfo(mountProperties.get(DEVICE_ID.toString())).toMap();
//        if (props.isEmpty()) {
//            log.error("Could not mount device with properties {}", mountProperties);
//            throw new IOException("Could not mount device");
//        }
//        var busLocation = props.get(BUS_LOCATION.toString());
//        var devNum = props.get(DEV_NUM.toString());
//
//        var mountResult = CommandRunner.runCommand(Arrays.asList(
//                JMTP_CMD, String.format(DEVICE_LOC, busLocation, devNum), getRootPath()));
//
//        if (mountResult.getExitValue() != 0) {
//            throw new IOException("Could not mount device");
//        }
//        log.info("Device mounted: {}", mountProperties);
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
//        var devices = getDevicesInfo();
//        return devices.stream()
//                .map(MTPFileSystemProvider.MTPDeviceInfo::toMap)
//                .collect(Collectors.toList());
        return null;
    }

    @Override
    protected StorageDevice getStorageDevice(Path path) throws IOException, URISyntaxException {
        unmount();
        var devicePath = path.toString();
        if (devicePath.startsWith(rootPath)) {
            devicePath = path.toString().substring(rootPath.length());
        }
        StorageDevice storage = null; //getStorageDevice(devicePath, mountProperties.get(DEVICE_ID.toString()));
        if (storage == null) {
            log.error("Could not get storage device for path {}", path);
            throw new IOException("Could not get storage device");
        }
        mount();
        return storage;
    }

    @Override
    public void unmount() throws IOException {
        var unmountResult = CommandRunner.runCommand(Arrays.asList(FUSERMOUNT, "-u", getRootPath()));
        for (int i = 0; i < 10 && unmountResult.getExitValue() != 0; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("Failed to sleep", e);
            }
            unmountResult = CommandRunner.runCommand(Arrays.asList(FUSERMOUNT, "-u", getRootPath()));
        }
        if (unmountResult.getExitValue() != 0) {
            throw new IOException("Could not unmount device: " + unmountResult.getOutput());
        }
        log.info("Device unmounted: {}", mountProperties);
    }
}
