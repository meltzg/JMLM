package org.meltzg.jmlm.device;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.device.content.ContentLocation;
import org.meltzg.jmlm.device.storage.StorageDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;

import javax.persistence.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class FileSystemAudioContentDevice implements MountableDevice {
    @Id
    @Getter
    private String id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    protected String rootPath = "/";

    @ManyToMany(fetch = FetchType.EAGER)
    @Getter
    private Map<Long, AudioContent> content = new HashMap<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapKey(name = "storageId")
    @Getter
    private Map<String, StorageDevice> storageDevices = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Getter
    private Map<UUID, String> libraryRoots = new HashMap<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapKey(name = "contentId")
    @Getter
    private Map<Long, ContentLocation> contentLocations = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<UUID, String> libraryRootToStorage = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Getter
    @Setter
    private Map<String, String> mountProperties = new HashMap<>();

    @Transient
    @ToString.Exclude
    @Setter
    private AudioContentRepository contentRepo;

    public FileSystemAudioContentDevice(AudioContentRepository contentRepo) {
        this("Device", contentRepo);
    }

    public FileSystemAudioContentDevice(String name, AudioContentRepository contentRepo) {
        this.name = name;
        this.contentRepo = contentRepo;
        this.id = UUID.randomUUID().toString();
    }

    public AudioContent getContent(Long id) {
        return content.get(id);
    }

    public boolean containsContent(Long id) {
        return content.containsKey(id);
    }

    public void addLibraryRoot(String libraryPath) throws IOException, URISyntaxException {
        addLibraryRoot(libraryPath, true);
    }

    public void addLibraryRoot(String libraryPath, boolean scanContent) throws IOException, URISyntaxException {
        var libPath = libraryPath.startsWith(rootPath) ? Paths.get(libraryPath) : Paths.get(rootPath, libraryPath);
        if (!Files.isDirectory(libPath)) {
            throw new IllegalArgumentException("Library root must be a valid directory (" +
                    libPath.toAbsolutePath() + ")");
        }
        libPath = libPath.toAbsolutePath();

        if (!libraryRoots.containsValue(libPath.toString())) {
            for (var existingLib : libraryRoots.values()) {
                var existingPath = Paths.get(existingLib).toAbsolutePath();
                if (existingPath.startsWith(libPath) || libPath.startsWith(existingPath)) {
                    throw new IllegalArgumentException("Library root cannot be a child of another library root");
                }
            }

            var libId = UUID.randomUUID();
            libraryRoots.put(libId, libPath.toString());
            var libStorage = getStorageDevice(libPath);
            libraryRootToStorage.put(libId, libStorage.getStorageId());

            if (!storageDevices.containsKey(libStorage.getStorageId())) {
                storageDevices.put(libStorage.getStorageId(), libStorage);
            } else {
                libStorage = storageDevices.get(libStorage.getStorageId());
            }

            libStorage.setPartitions(libStorage.getPartitions() + 1);
        }

        if (scanContent) {
            scanDeviceContent();
        }
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
                    try {
                        AudioContent contentData;
                        contentData = makeAudioContent(path.toString());
                        registerContent(contentData, libId, path);
                    } catch (TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public AudioContent addContent(InputStream stream, String librarySubPath, UUID libraryId) throws ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException, IOException {
        var destination = Paths.get(libraryRoots.get(libraryId), librarySubPath);
        if (!destination.getParent().toFile().exists() && !destination.getParent().toFile().mkdirs()) {
            throw new IOException("Could not create intermediate directories for " + destination);
        }
        try {
            Files.copy(stream, destination);

            var contentData = makeAudioContent(destination.toString());
            if (content.containsKey(contentData.getId())) {
                throw new FileAlreadyExistsException("Content already exists on device");
            }

            contentData = registerContent(contentData, libraryId, destination);
            var storage = storageDevices.get(libraryRootToStorage.get(contentLocations.get(contentData.getId()).getLibraryId()));
            storage.setFreeSpace(storage.getFreeSpace() - contentData.getSize());
            return contentData;

        } catch (TagException | ReadOnlyFileException | CannotReadException | InvalidAudioFrameException | IOException e) {
            FileUtils.deleteQuietly(destination.toFile());
            throw e;
        }
    }

    public void deleteContent(Long id) throws IOException {
        var contentData = content.get(id);
        if (contentData == null) {
            throw new FileNotFoundException("Could not find content with ID " + id);
        }
        var contentLocation = contentLocations.get(contentData.getId());
        var libraryId = contentLocation.getLibraryId();
        var libraryPath = libraryRoots.get(libraryId);

        var path = Paths.get(libraryPath, contentLocation.getLibrarySubPath());

        Files.delete(path);
        unregisterContent(contentData);
        var storage = storageDevices.get(libraryRootToStorage.get(libraryId));
        storage.setFreeSpace(storage.getFreeSpace() + contentData.getSize());
    }

    public void moveContent(Long id, UUID destinationId) throws IOException {
        var destinationLibrary = libraryRoots.get(destinationId);
        var contentInfo = content.get(id);

        if (destinationLibrary == null) {
            throw new IllegalArgumentException("Invalid destination library");
        }
        if (contentInfo == null) {
            throw new FileNotFoundException("Could not find content with ID " + id);
        }
        var contentLocation = contentLocations.get(contentInfo.getId());
        if (contentLocation.getLibraryId() == destinationId) {
            return;
        }

        var sourceLibrary = libraryRoots.get(contentLocation.getLibraryId());
        var file = Paths.get(sourceLibrary, contentLocation.getLibrarySubPath());
        var destination = Paths.get(destinationLibrary, contentLocation.getLibrarySubPath());

        FileUtils.moveFile(file.toFile(), destination.toFile());
        unregisterContent(contentInfo);
        registerContent(contentInfo, destinationId, destination);
    }

    public InputStream getContentStream(Long id) throws IOException {
        var contentInfo = content.get(id);
        if (contentInfo == null) {
            throw new FileNotFoundException("Could not find content with ID " + id);
        }
        var contentLocation = contentLocations.get(contentInfo.getId());

        var libraryPath = libraryRoots.get(contentLocation.getLibraryId());
        var path = Paths.get(libraryPath, contentLocation.getLibrarySubPath());
        return Files.newInputStream(path);
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
            freeSpaces.put(entry.getKey(), storage.getFreeSpace() / storage.getPartitions());
        }
        return freeSpaces;
    }

    public Path getParentDir(Path path) {
        if (!path.toAbsolutePath().startsWith(Paths.get(rootPath))) {
            return Paths.get(rootPath);
        }
        return path.getParent();
    }

    public List<Path> getChildrenDirs(Path path) throws IllegalAccessException, IOException {
        if (!path.toAbsolutePath().startsWith(Paths.get(rootPath))) {
            throw new IllegalAccessException("Cannot access " + path);
        }

        return Files.list(path)
                .filter(p -> p.toFile().isDirectory())
                .collect(Collectors.toList());
    }

    @Override
    public FileSystemAudioContentDevice mount() throws IOException {
        log.info("Device mounted: {}", mountProperties);
        return this;
    }

    @Override
    public void unmount() throws IOException {
        log.info("Device unmounted: {}", mountProperties);
    }

    protected StorageDevice getStorageDevice(Path path) throws IOException, URISyntaxException {
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
        String deviceId = dirFileStore.name();

        return new StorageDevice(deviceId, capacity, freespace, 0);
    }

    private AudioContent makeAudioContent(String path) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        var fsPath = Paths.get(path).toAbsolutePath();

        var af = AudioFileIO.read(fsPath.toFile());
        var tag = af.getTag();

        AudioContent contentInfo;

        var size = Files.size(fsPath);

        var genre = tag.getFirst(FieldKey.GENRE);
        var artist = tag.getFirst(FieldKey.ARTIST);
        var album = tag.getFirst(FieldKey.ALBUM);
        var title = tag.getFirst(FieldKey.TITLE);
        var trackNum = Integer.parseInt(tag.getFirst(FieldKey.TRACK));

        String strDiscNum = tag.getFirst(FieldKey.DISC_NO);
        var discNum = strDiscNum.length() > 0 ? Integer.parseInt(strDiscNum) : 1;

        contentInfo = new AudioContent(size, genre, artist,
                album, title, discNum, trackNum);

        return contentInfo;
    }

    private AudioContent registerContent(AudioContent contentData, UUID libId, Path path) {
        var libPath = Paths.get(libraryRoots.get(libId));
        var libSubPath = path.toAbsolutePath().toString().substring(
                libPath.toAbsolutePath().toString().length());

        contentData = contentRepo.save(contentData);

        content.put(contentData.getId(), contentData);
        contentLocations.put(contentData.getId(), new ContentLocation(contentData.getId(), libId, libSubPath));

        return contentData;
    }

    private void unregisterContent(AudioContent contentData) {
        content.remove(contentData.getId());
        contentLocations.remove(contentData.getId());
    }
}
