package org.meltzg.jmlm.device;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.meltzg.jmlm.repositories.AudioContentRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class MTPAudioContentDevice extends FileSystemAudioContentDevice implements ListableDevice {
    private static final String DEV_NUM = "devNum";
    private static final String BUS_LOCATION = "busLocation";
    private static final String PRODUCT_ID = "productId";
    private static final String VENDOR_ID = "vendorId";
    private static final String PRODUCT = "product";
    private static final String VENDOR = "vendor";

    private static final String JMTP_CMD = "jmtpfs";
    private static final String LIST_FLG = "--listDevices";
    private static final String DEVICE_LOC = "-device=%s,%s";
    private static final String FUSERMOUNT = "fusermount";

    @Getter
    @Setter
    private Map<String, String> mountProperties = toMap(new String[][]{
            {PRODUCT_ID, "null"},
            {VENDOR_ID, "null"},
            {PRODUCT, "null"},
            {VENDOR, "null"}
    });

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
        var processBuilder = new ProcessBuilder(Arrays.asList(JMTP_CMD, LIST_FLG));
        var process = processBuilder.start();
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        var allDeviceProps = new ArrayList<Map<String, String>>();

        String line;
        boolean startParsing = false;
        while ((line = reader.readLine()) != null) {
            log.info(line);
            if (startParsing) {
                var parts = line.split(",");
                allDeviceProps.add(toMap(new String[][]{
                        {BUS_LOCATION, parts[0].trim()},
                        {DEV_NUM, parts[1].trim()},
                        {PRODUCT_ID, parts[2].trim()},
                        {VENDOR_ID, parts[3].trim()},
                        {PRODUCT, parts[4].trim()},
                        {VENDOR, parts[5].trim()}
                }));
            } else {
                if (line.contains("Available devices")) {
                    startParsing = true;
                }
            }
        }
        return allDeviceProps;
    }

    @Override
    public void mount() throws IOException {
        setRootPath(Paths.get(mountDir, getId()).toString());
        var mountPath = Paths.get(getRootPath());
        if (!mountPath.toFile().exists() && !Paths.get(getRootPath()).toFile().mkdirs()) {
            throw new IOException("Could not create intermediate directories for " + getRootPath());
        }
        var allDevices = getAllDeviceMountProperties();
        for (var props : allDevices) {
            var match = true;
            for (var key : Arrays.asList(PRODUCT_ID, VENDOR_ID, PRODUCT, VENDOR)) {
                if (!props.get(key).equals(mountProperties.get(key))) {
                    match = false;
                }
            }
            if (match) {
                var busLocation = props.get(BUS_LOCATION);
                var devNum = props.get(DEV_NUM);

                var processBuilder = new ProcessBuilder(Arrays.asList(
                        JMTP_CMD, String.format(DEVICE_LOC, busLocation, devNum), getRootPath()));
                processBuilder.inheritIO();
                var process = processBuilder.start();
                var exitVal = 0;
                try {
                    exitVal = process.waitFor();
                } catch (InterruptedException e) {
                    log.error("Device mounting was interrupted", e);
                    unmount();
                    exitVal = 1;
                }

                if (exitVal != 0) {
                    var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.error(line);
                    }
                    throw new IOException("Could not mount device");
                }
                break;
            }
        }
        log.info("Device mounted: {}", mountProperties);
    }

    @Override
    public void unmount() throws IOException {
        var processBuilder = new ProcessBuilder(Arrays.asList(FUSERMOUNT, "-u", getRootPath()));
        processBuilder.inheritIO();
        var process = processBuilder.start();
        var exitVal = 0;
        try {
            exitVal = process.waitFor();
        } catch (InterruptedException e) {
            log.error("Device unmounting was interrupted", e);
            unmount();
            exitVal = 1;
        }

        if (exitVal != 0) {
            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.error(line);
            }
            throw new IOException("Could not unmount device");
        }
        log.info("Device unmounted: {}", mountProperties);
    }
}
