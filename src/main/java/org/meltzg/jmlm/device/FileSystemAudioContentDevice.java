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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileSystemAudioContentDevice
        implements JsonSerializer<FileSystemAudioContentDevice>, JsonDeserializer<FileSystemAudioContentDevice> {
    private Map<UUID, String> libraryRoots;
    private BiMap<String, StorageDevice> storageDevices;
    private Map<String, AudioContent> content;
    private Map<UUID, String> libraryRootToStorage;
    private Map<UUID, Set<String>> libraryContent;

    public FileSystemAudioContentDevice() {
        this.libraryRoots = new HashMap<>();
        this.storageDevices = HashBiMap.create();
        this.content = new HashMap<>();
        this.libraryRootToStorage = new HashMap<>();
        this.libraryContent = new HashMap<>();
    }

    public Map<UUID, String> getLibraryRoots() {
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

        if (!this.libraryRoots.values().contains(libPath.toString())) {
            for (var existingLib : this.libraryRoots.values()) {
                var existingPath = Paths.get(existingLib).toAbsolutePath();
                if (existingPath.startsWith(libPath) || libPath.startsWith(existingPath)) {
                    throw new IllegalArgumentException("Library root cannot be a child of another library root");
                }
            }

            var libId = UUID.randomUUID();
            this.libraryRoots.put(libId, libPath.toString());
            var libStorage = this.getStorageDevice(libPath);
            this.libraryRootToStorage.put(libId, libStorage.getId());

            if (!this.libraryContent.containsKey(libId)) {
                this.libraryContent.put(libId, new HashSet<>());
            }

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

    public Map<UUID, Long> getLibraryRootCapacities() {
        var libCapacities = new HashMap<UUID, Long>();
        for (var entry : this.libraryRootToStorage.entrySet()) {
            var storage = this.storageDevices.get(entry.getValue());
            libCapacities.put(entry.getKey(), storage.getCapacity() / storage.getPartitions());
        }

        return libCapacities;
    }

    public void scanDeviceContent() {
        for (var libRoot : this.libraryRoots.entrySet()) {
            UUID libId = libRoot.getKey();
            Path libPath = Paths.get(libRoot.getValue());
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
                    AudioContent contentData = null;
                    try {
                        contentData = makeAudioContent(path.toString(), libId);
                        registerContent(contentData, libId);
                    } catch (TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public AudioContent addContentToDevice(InputStream bytes, AudioContent metadata, UUID libraryId) throws IOException {
        var destination = Paths.get(libraryRoots.get(libraryId), metadata.getLibraryPath());
        if (!destination.getParent().toFile().mkdirs()) {
            throw new IOException("Could not create intermediate directories for " + destination);
        }
        Files.copy(bytes, destination);
        try {
            var contentData = makeAudioContent(destination.toString(), libraryId);
            registerContent(contentData, libraryId);
            return contentData;

        } catch (TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException e) {
            e.printStackTrace();
            Files.delete(destination);
        }

        return null;
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

        return new StorageDevice(deviceId, capacity, 0);
    }

    protected AudioContent makeAudioContent(String path, UUID libId) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        var fsPath = Paths.get(path).toAbsolutePath();
        var isfs = Files.newInputStream(fsPath);

        var libPath = Paths.get(libraryRoots.get(libId));

        var af = AudioFileIO.read(fsPath.toFile());
        var tag = af.getTag();

        var contentId = DigestUtils.md2Hex(isfs);
        var libSubPath = fsPath.toAbsolutePath().toString().substring(
                libPath.toAbsolutePath().toString().length());
        var size = Files.size(fsPath);

        var genre = tag.getFirst(FieldKey.GENRE);
        var artist = tag.getFirst(FieldKey.ARTIST);
        var album = tag.getFirst(FieldKey.ALBUM);
        var title = tag.getFirst(FieldKey.TITLE);
        var trackNum = Integer.parseInt(tag.getFirst(FieldKey.TRACK));

        String strDiscNum = tag.getFirst(FieldKey.DISC_NO);
        var discNum = strDiscNum.length() > 0 ? Integer.parseInt(strDiscNum) : 1;

        return new AudioContent(contentId, libSubPath, size, genre, artist,
                album, title, discNum, trackNum);
    }

    private void registerContent(AudioContent contentData, UUID libId) {
        this.content.put(contentData.getId(), contentData);
        this.libraryContent.get(libId).add(contentData.getId());
    }

    @Override
    public FileSystemAudioContentDevice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var jsonObject = json.getAsJsonObject();
        var device = new FileSystemAudioContentDevice();

        device.libraryRoots = new HashMap<>();
        device.content = new HashMap<>();
        device.libraryContent = new HashMap<>();
        device.storageDevices = HashBiMap.create();
        device.libraryRootToStorage = new HashMap<>();

        for (var entry : jsonObject.getAsJsonObject("libraryRoots").entrySet()) {
            device.libraryRoots.put(UUID.fromString(entry.getKey()), entry.getValue().getAsString());
        }

        for (var contentJson : jsonObject.getAsJsonArray("content")) {
            AudioContent content = context.deserialize(contentJson, AudioContent.class);
            device.content.put(content.getId(), content);
        }

        for (var entry : jsonObject.getAsJsonObject("libraryContent").entrySet()) {
            device.libraryContent.put(UUID.fromString(entry.getKey()), context.deserialize(entry.getValue(), Set.class));
        }

        for (var storageJson : jsonObject.getAsJsonArray("storageDevices")) {
            StorageDevice storage = context.deserialize(storageJson, StorageDevice.class);
            device.storageDevices.put(storage.getId(), storage);
        }

        for (var entry : jsonObject.getAsJsonObject("libraryRootToStorage").entrySet()) {
            device.libraryRootToStorage.put(UUID.fromString(entry.getKey()), entry.getValue().getAsString());
        }

        return device;
    }

    @Override
    public JsonElement serialize(FileSystemAudioContentDevice src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonMap = new JsonObject();
        jsonMap.add("libraryRoots", context.serialize(src.libraryRoots));
        jsonMap.add("content", context.serialize(src.content.values()));
        jsonMap.add("libraryContent", context.serialize(src.libraryContent));
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
                libraryRootToStorage.equals(that.libraryRootToStorage) &&
                libraryContent.equals(that.libraryContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryRoots, storageDevices, content, libraryRootToStorage, libraryContent);
    }
}
