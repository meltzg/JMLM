package org.meltzg.jmlm.device;

import lombok.Getter;
import lombok.Setter;
import org.meltzg.jmlm.repositories.AudioContentRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MTPAudioContentDevice extends FileSystemAudioContentDevice implements ListableDevice {
    private static final String JMTP_CMD = "jmtpfs";
    private static final String LIST_FLG = "--listDevices";
    @Getter
    @Setter
    private Map<String, String> mountProperties = toMap(new String[][]{
            {"productId", "null"},
            {"vendorId", "null"},
            {"product", "null"},
            {"vendor", "null"}
    });

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

    @Override
    public List<Map<String, String>> getAllDeviceMountProperties() throws IOException {
        var processBuilder = new ProcessBuilder(Arrays.asList(JMTP_CMD, LIST_FLG));
        var process = processBuilder.start();
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        var allDeviceProps = new ArrayList<Map<String, String>>();

        String line = null;
        boolean startParsing = false;
        while ((line = reader.readLine()) != null) {
            if (startParsing) {
                var parts = line.split(",");
                allDeviceProps.add(toMap(new String[][]{
                        {"busLocation", parts[0].trim()},
                        {"devNum", parts[1].trim()},
                        {"productId", parts[2].trim()},
                        {"vendorId", parts[3].trim()},
                        {"product", parts[4].trim()},
                        {"vendor", parts[5].trim()}
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
    public void mount() {

    }

    @Override
    public void unmount() {

    }
}
