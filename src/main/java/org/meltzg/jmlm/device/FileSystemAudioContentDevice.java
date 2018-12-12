package org.meltzg.jmlm.device;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.device.storage.StorageDevice;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileSystemAudioContentDevice
        implements JsonSerializer<FileSystemAudioContentDevice>, JsonDeserializer<FileSystemAudioContentDevice> {
    private Set<String> libraryRoots;
    private BiMap<String, StorageDevice> storageDevices;
    private Map<String, AudioContent> content;

    public FileSystemAudioContentDevice() {
        this.libraryRoots = new HashSet<>();
        this.storageDevices = HashBiMap.create();
        this.content = new HashMap<>();
    }

    public Set<String> getLibraryRoots() {
        return libraryRoots;
    }

    public Map<String, AudioContent> getContent() {
        return content;
    }

    public Set<StorageDevice> getStorageDevices() {
        return storageDevices.values();
    }

    public void addLibraryRoot(String libraryPath) {
        var libPath = Paths.get(libraryPath);
        if (!Files.isDirectory(libPath)) {
            throw new IllegalArgumentException("Library root must be a valid directory (" +
                    libPath.toAbsolutePath() + ")");
        }
        libPath = libPath.toAbsolutePath();

        if (!this.libraryRoots.contains(libPath.toString())) {
            for (var existingLib : this.libraryRoots) {
                var existingPath = Paths.get(existingLib).toAbsolutePath();
                if (existingPath.startsWith(libPath) || libPath.startsWith(existingPath)) {
                    throw new IllegalArgumentException("Library root cannot be a child of another library root");
                }
            }

            this.libraryRoots.add(libPath.toString());
            var libStorage = this.getStorageDevice(libPath);
            this.storageDevices.put(libStorage.getId(),
                    this.storageDevices.getOrDefault(libStorage.getId(), libStorage));
            libStorage.setPartitions(libStorage.getPartitions() + 1);
        }
    }

    protected StorageDevice getStorageDevice(Path path) {
        String deviceId = null;
        var idFile = new File(path.toString());
        var capacity = idFile.getTotalSpace();

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            deviceId = path.getRoot().toString();
        } else {
            try {
                deviceId = Files.getAttribute(path, "unix:dev").toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        var device = new StorageDevice(deviceId, capacity, 0);
        return device;
    }

    @Override
    public FileSystemAudioContentDevice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var jsonObject = json.getAsJsonObject();
        var device = new FileSystemAudioContentDevice();

        device.libraryRoots = context.deserialize(jsonObject.get("libraryRoots"), Set.class);
        device.content = context.deserialize(jsonObject.get("content"), Map.class);
        device.storageDevices = HashBiMap.create();

        for (var storageJson : jsonObject.getAsJsonArray("storageDevices")) {
            StorageDevice storage = context.deserialize(storageJson, StorageDevice.class);
            device.storageDevices.put(storage.getId(), storage);
        }

        return device;
    }

    @Override
    public JsonElement serialize(FileSystemAudioContentDevice src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonMap = new JsonObject();
        jsonMap.add("libraryRoots", context.serialize(src.libraryRoots));
        jsonMap.add("content", context.serialize(src.content));
        jsonMap.add("storageDevices", context.serialize(src.storageDevices.values()));
        return jsonMap;
    }
}
