package org.meltzg.jmlm.device;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.device.storage.StorageDevice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

public class FileSystemAudioContentDevice
        implements JsonSerializer<FileSystemAudioContentDevice>, JsonDeserializer<FileSystemAudioContentDevice> {
    private Map<UUID, String> libraryRoots;
    private BiMap<String, StorageDevice> storageDevices;
    private Map<String, AudioContent> content;
    private Map<UUID, String> libraryRootToStorage;
    private Map<UUID, Set<String>> libraryContent;

    private Gson gson;

    public FileSystemAudioContentDevice() {
        this.libraryRoots = new HashMap<>();
        this.storageDevices = HashBiMap.create();
        this.content = new HashMap<>();
        this.libraryRootToStorage = new HashMap<>();
        this.libraryContent = new HashMap<>();

        this.gson = new GsonBuilder()
                .registerTypeAdapter(this.getClass(),
                        this)
                .create();
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

    public Gson getGson() {
        return gson;
    }

    public void addLibraryRoot(String libraryPath) throws IOException, URISyntaxException {
        addLibraryRoot(libraryPath, true);
    }

    public void addLibraryRoot(String libraryPath, boolean scanContent) throws IOException, URISyntaxException {
        var libPath = Paths.get(libraryPath);
        if (!Files.isDirectory(libPath)) {
            throw new IllegalArgumentException("Library root must be a valid directory (" +
                    libPath.toAbsolutePath() + ")");
        }
        libPath = libPath.toAbsolutePath();

        if (!libraryRoots.values().contains(libPath.toString())) {
            for (var existingLib : libraryRoots.values()) {
                var existingPath = Paths.get(existingLib).toAbsolutePath();
                if (existingPath.startsWith(libPath) || libPath.startsWith(existingPath)) {
                    throw new IllegalArgumentException("Library root cannot be a child of another library root");
                }
            }

            var libId = UUID.randomUUID();
            libraryRoots.put(libId, libPath.toString());
            var libStorage = getStorageDevice(libPath);
            libraryRootToStorage.put(libId, libStorage.getId());

            if (!libraryContent.containsKey(libId)) {
                libraryContent.put(libId, new HashSet<>());
            }

            if (!storageDevices.containsKey(libStorage.getId())) {
                storageDevices.put(libStorage.getId(), libStorage);
            } else {
                libStorage = storageDevices.get(libStorage.getId());
            }

            libStorage.setPartitions(libStorage.getPartitions() + 1);
        }

        if (scanContent) {
            scanDeviceContent();
        }
    }

    public Map<UUID, Long> getLibraryRootCapacities() {
        var libCapacities = new HashMap<UUID, Long>();
        for (var entry : libraryRootToStorage.entrySet()) {
            var storage = storageDevices.get(entry.getValue());
            libCapacities.put(entry.getKey(), storage.getCapacity() / storage.getPartitions());
        }

        return libCapacities;
    }

    public Map<UUID, Long> getLibraryRootFreeSpace() {
        var freeSpaces = new HashMap<UUID, Long>();
        for (var entry : libraryRootToStorage.entrySet()) {
            var storage = storageDevices.get(entry.getValue());
            freeSpaces.put(entry.getKey(), storage.getFreespace() / storage.getPartitions());
        }
        return freeSpaces;
    }

    public void scanDeviceContent() {
        for (var libRoot : libraryRoots.entrySet()) {
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

    public AudioContent getContent(String id) {
        return content.get(id);
    }

    public AudioContent addContent(InputStream stream, String librarySubPath, UUID libraryId) throws ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException, IOException {
        var destination = Paths.get(libraryRoots.get(libraryId), librarySubPath);
        if (!destination.getParent().toFile().mkdirs()) {
            throw new IOException("Could not create intermediate directories for " + destination);
        }
        try {
            Files.copy(stream, destination);

            var contentData = makeAudioContent(destination.toString(), libraryId);
            if (content.containsKey(contentData.getId())) {
                throw new FileAlreadyExistsException("Content already exists on device");
            }

            registerContent(contentData, libraryId);
            var storage = storageDevices.get(libraryRootToStorage.get(contentData.getLibraryId()));
            storage.setFreespace(storage.getFreespace() - contentData.getSize());
            return contentData;

        } catch (TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException | IOException e) {
            FileUtils.deleteQuietly(destination.toFile());
            throw e;
        }
    }

    public void deleteContent(String id) throws IOException {
        var contentData = content.get(id);
        if (contentData == null) {
            throw new FileNotFoundException("Could not find content with ID " + id);
        }
        var libraryId = contentData.getLibraryId();
        var libraryPath = libraryRoots.get(libraryId);

        var path = Paths.get(libraryPath, contentData.getLibraryPath());
        try {
            Files.delete(path);
            unregisterContent(contentData);
            var storage = storageDevices.get(libraryRootToStorage.get(contentData.getLibraryId()));
            storage.setFreespace(storage.getFreespace() + contentData.getSize());
        } catch (IOException e) {
            throw e;
        }
    }

    public void moveContent(String id, UUID destinationId) throws IOException {
        var destinationLibrary = libraryRoots.get(destinationId);
        var contentInfo = content.get(id);

        if (destinationLibrary == null) {
            throw new IllegalArgumentException("Invalid destination library");
        }
        if (contentInfo == null) {
            throw new FileNotFoundException("Could not find content with ID " + id);
        }
        if (contentInfo.getLibraryId() == destinationId) {
            return;
        }

        var sourceLibrary = libraryRoots.get(contentInfo.getLibraryId());
        var file = Paths.get(sourceLibrary, contentInfo.getLibraryPath());
        var destination = Paths.get(destinationLibrary, contentInfo.getLibraryPath());

        FileUtils.moveFile(file.toFile(), destination.toFile());
        unregisterContent(contentInfo);
        registerContent(contentInfo, destinationId);
    }

    public InputStream getContentStream(String id) throws IOException {
        var contentInfo = content.get(id);
        if (contentInfo == null) {
            throw new FileNotFoundException("Could not find content with ID " + id);
        }

        var libraryPath = libraryRoots.get(contentInfo.getLibraryId());
        var path = Paths.get(libraryPath, contentInfo.getLibraryPath());
        return Files.newInputStream(path);
    }

    public boolean containsContent(String id) {
        return content.containsKey(id);
    }

    protected StorageDevice getStorageDevice(Path path) throws IOException, URISyntaxException {
        String deviceId = null;
        path = Paths.get(path.toString().replaceFirst("^~", System.getProperty("user.home")));
        var idFile = new File(path.toString());
        var freespace = idFile.getFreeSpace();
        var capacity = freespace;

        for (var file : content.values()) {
            capacity += file.getSize();
        }

        URI rootURI = new URI("file:///");
        Path rootPath = Paths.get(rootURI);
        Path dirPath = rootPath.resolve(path);
        FileStore dirFileStore = Files.getFileStore(dirPath);
        deviceId = dirFileStore.name();

        return new StorageDevice(deviceId, capacity, freespace, 0);
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

        return new AudioContent(contentId, libId, libSubPath, size, genre, artist,
                album, title, discNum, trackNum);
    }

    private void registerContent(AudioContent contentData, UUID libId) {
        content.put(contentData.getId(), contentData);
        libraryContent.get(libId).add(contentData.getId());
        contentData.setLibraryId(libId);
    }

    private void unregisterContent(AudioContent contentData) {
        content.remove(contentData.getId());
        libraryContent.get(contentData.getLibraryId()).remove(contentData.getId());
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
