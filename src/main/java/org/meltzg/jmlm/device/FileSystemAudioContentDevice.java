package org.meltzg.jmlm.device;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.device.storage.StorageDevice;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileSystemAudioContentDevice
        implements JsonSerializer<FileSystemAudioContentDevice>, JsonDeserializer<FileSystemAudioContentDevice> {
    private Set<String> libraryRoots;
    private BiMap<String, StorageDevice> storageDevices;
    private Map<String, AudioContent> content;
    private Map<String, String> libraryRootToStorage;

    public FileSystemAudioContentDevice() {
        this.libraryRoots = new HashSet<>();
        this.storageDevices = HashBiMap.create();
        this.content = new HashMap<>();
        this.libraryRootToStorage = new HashMap<>();
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
        this.addLibraryRoot(libraryPath, true);
    }

    public void addLibraryRoot(String libraryPath, boolean scanContent) {
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
            this.libraryRootToStorage.put(libPath.toString(), libStorage.getId());

            if (!this.storageDevices.containsKey(libStorage.getId())) {
                this.storageDevices.put(libStorage.getId(), libStorage);
            } else {
                libStorage = this.storageDevices.get(libStorage.getId());
            }

            libStorage.setPartitions(libStorage.getPartitions() + 1);
        }

        if (scanContent) {
            this.scanDeviceContent();
        }
    }

    public Map<String, Long> getLibraryRootCapacities() {
        var libCapacities = new HashMap<String, Long>();
        for (var entry : this.libraryRootToStorage.entrySet()) {
            var storage = this.storageDevices.get(entry.getValue());
            libCapacities.put(entry.getKey(), storage.getCapacity() / storage.getPartitions());
        }

        return libCapacities;
    }

    protected StorageDevice getStorageDevice(Path path) {
        String deviceId = null;
        var idFile = new File(path.toString());
        var capacity = idFile.getFreeSpace();

        for (var file : this.content.values()) {
            capacity += file.getSize();
        }

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

    public void scanDeviceContent() {
        for (var libRoot : this.libraryRoots) {
            Path libPath = Paths.get(libRoot);
            var stack = new Stack<Path>();
            stack.push(libPath);

            while (!stack.empty()) {
                var path = stack.pop();
                if (Files.isDirectory(path)) {
                    try (var ds = Files.newDirectoryStream(path)) {
                        for (var child : ds) {
                            stack.push(child);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (var isfs = Files.newInputStream(path)) {
                        var af = AudioFileIO.read(path.toFile());
                        var tag = af.getTag();

                        var contentId = DigestUtils.md2Hex(isfs);
                        var libSubPath = path.toAbsolutePath().toString().substring(
                                libPath.toAbsolutePath().toString().length());
                        var size = Files.size(path);

                        var genre = tag.getFirst(FieldKey.GENRE);
                        var artist = tag.getFirst(FieldKey.ARTIST);
                        var album = tag.getFirst(FieldKey.ALBUM);
                        var title = tag.getFirst(FieldKey.TITLE);
                        var trackNum = Integer.parseInt(tag.getFirst(FieldKey.TRACK));

                        String strDiscNum = tag.getFirst(FieldKey.DISC_NO);
                        var discNum = strDiscNum.length() > 0 ? Integer.parseInt(strDiscNum) : 1;

                        var content = new AudioContent(contentId, libSubPath, size, genre, artist,
                                album, title, discNum, trackNum);
                        this.content.put(content.getId(), content);
                    } catch (IOException | CannotReadException | ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public FileSystemAudioContentDevice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var jsonObject = json.getAsJsonObject();
        var device = new FileSystemAudioContentDevice();

        device.libraryRoots = context.deserialize(jsonObject.get("libraryRoots"), Set.class);
        device.content = new HashMap<>();
        device.storageDevices = HashBiMap.create();
        device.libraryRootToStorage = context.deserialize(jsonObject.get("libraryRootToStorage"), Map.class);

        for (var contentJson : jsonObject.getAsJsonArray("content")) {
            AudioContent content = context.deserialize(contentJson, AudioContent.class);
            device.content.put(content.getId(), content);
        }

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
        jsonMap.add("content", context.serialize(src.content.values()));
        jsonMap.add("storageDevices", context.serialize(src.storageDevices.values()));
        jsonMap.add("libraryRootToStorage", context.serialize(src.libraryRootToStorage));
        return jsonMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSystemAudioContentDevice that = (FileSystemAudioContentDevice) o;
        return libraryRoots.equals(that.libraryRoots) &&
                storageDevices.equals(that.storageDevices) &&
                content.equals(that.content) &&
                libraryRootToStorage.equals(that.libraryRootToStorage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryRoots, storageDevices, content, libraryRootToStorage);
    }
}
