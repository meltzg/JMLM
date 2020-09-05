package org.meltzg.jmlm.filesystem.mtp;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.meltzg.jmlm.device.storage.StorageDevice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Collectors;

import static org.meltzg.jmlm.filesystem.mtp.MTPFileSystemProvider.MountProperties.*;


/**
 * MTP FileSystem Schema
 * mtp:VID:PID:SERIAL!/path/on/device
 */
public class MTPFileSystemProvider extends FileSystemProvider {
    static {
        System.loadLibrary("jmtp");
        MTPFileSystemProvider.initMTP();
    }

    final Map<DeviceIdentifier, MTPFileSystem> fileSystems = new HashMap<>();

    private static native void initMTP();

    private static native List<MTPDeviceInfo> getDevicesInfo();

    private static native MTPDeviceInfo getDeviceInfo(String id);

    private static native String getFileStoreId(String path, String deviceId);

    private static native byte[] getFileContent(String path, String deviceId);

    private static native int writeFileContent(String path, String deviceId, byte[] bytes, long offset, int length);

    private static native boolean deleteFile(String path, String deviceId);

    private static native List<String> getPathChildren(String path, String deviceId);

    private static native boolean isDirectory(String path, String deviceId);

    private static native long size(String path, String deviceId) throws IOException;

    static native StorageDevice getFileStoreProperties(String storageId, String deviceId);

    @Override
    public String getScheme() {
        return "mtp";
    }

    @Override
    public MTPFileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        validateURI(uri);
        synchronized (fileSystems) {
            var deviceIdentifier = getDeviceIdentifier(uri);
            var fileSystem = fileSystems.get(deviceIdentifier);
            if (fileSystem != null) {
                throw new FileSystemAlreadyExistsException(deviceIdentifier.toString());
            }
            fileSystem = new MTPFileSystem(this, deviceIdentifier, env);
            fileSystems.put(deviceIdentifier, fileSystem);
            return fileSystem;
        }
    }

    @Override
    public MTPFileSystem getFileSystem(URI uri) {
        return getFileSystem(uri, false);
    }

    public MTPFileSystem getFileSystem(URI uri, boolean create) {
        validateURI(uri);
        synchronized (fileSystems) {
            var deviceIdentifier = getDeviceIdentifier(uri);
            var fileSystem = fileSystems.get(deviceIdentifier);
            if (fileSystem == null) {
                if (create) {
                    try {
                        fileSystem = newFileSystem(uri, null);
                    } catch (IOException e) {
                        throw (FileSystemNotFoundException) new FileSystemNotFoundException().initCause(e);
                    }
                } else {
                    throw new FileSystemNotFoundException(deviceIdentifier.toString());
                }
            }
            return fileSystem;
        }
    }

    @Override
    public Path getPath(URI uri) {
        validateURI(uri);
        var schemaSpecificPart = uri.getSchemeSpecificPart();
        var pathStart = schemaSpecificPart.indexOf("!/");
        if (pathStart == -1) {
            throw new IllegalArgumentException(String.format("URI %s does not contain path info", uri));
        }
        return getFileSystem(uri, true).getPath(schemaSpecificPart.substring(pathStart + 1));
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        validatePathProvider(path);
        var devicePath = toDevicePath(path);
        var deviceIdentifier = getDeviceIdentifier(path.toUri());
        var isWrite = options.contains(StandardOpenOption.WRITE);

        var bufferOptions = new HashSet<OpenOption>();
        bufferOptions.add(StandardOpenOption.WRITE);
        bufferOptions.add(StandardOpenOption.READ);
        var surrogateChannel = Files.newByteChannel(Files.createTempFile("write-buffer-", ".tmp"), bufferOptions, attrs);
        var content = getFileContent(devicePath, deviceIdentifier.toString());
        if (content != null) {
            surrogateChannel.write(ByteBuffer.allocate(content.length).put(content).position(0));
            surrogateChannel.position(0);
        }

        return new SeekableByteChannel() {
            long position;

            @Override
            public int read(ByteBuffer byteBuffer) throws IOException {
                surrogateChannel.position(position);
                var read = surrogateChannel.read(byteBuffer);
                position += read;
                return read;
            }

            @Override
            public int write(ByteBuffer byteBuffer) throws IOException {
                if (!isWrite) {
                    throw new IOException("Channel is not open for write");
                }
                surrogateChannel.position(position);
                var written = surrogateChannel.write(byteBuffer);
                position += written;
                return written;
            }

            @Override
            public long position() throws IOException {
                return position;
            }

            @Override
            public SeekableByteChannel position(long l) throws IOException {
                position = l;
                return this;
            }

            @Override
            public long size() throws IOException {
                var size = MTPFileSystemProvider.size(toDevicePath(path), deviceIdentifier.toString());
                if (isDirectory(devicePath, deviceIdentifier.toString())) {
                    throw new IOException(String.format("%s is a directory", path.toUri().toString()));
                }
                if (size < 0) {
                    throw new IOException(String.format("Could not retrieve size of %s", path.toUri().toString()));
                }
                return size;
            }

            @Override
            public SeekableByteChannel truncate(long l) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() throws IOException {
                if (isWrite) {
                    surrogateChannel.position(0);
                    var byteBuffer = ByteBuffer.allocate(Math.toIntExact(surrogateChannel.size()));
                    surrogateChannel.read(byteBuffer);
                    byteBuffer.position(0);
                    if (writeFileContent(devicePath, deviceIdentifier.toString(), byteBuffer.array(), 0, byteBuffer.remaining()) < 0) {
                        throw new IOException("Could not write write buffer to device on close");
                    }
                }
            }
        };
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        validatePathProvider(dir);
        if (!Files.isDirectory(dir)) {
            throw new IOException(String.format("%s is not a directory", dir.toUri()));
        }
        var deviceIdentifier = getDeviceIdentifier(dir.toUri());
        var children = getPathChildren(toDevicePath(dir), deviceIdentifier.toString());
        var childPaths = children
                .stream()
                .map(child -> {
                    try {
                        var strUri = String.format("%s/%s", dir.toUri(), URLEncoder.encode(child, StandardCharsets.UTF_8.toString()));
                        strUri = strUri.replaceAll("/+", "/");
                        return getPath(new URI(strUri));
                    } catch (URISyntaxException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
        return new DirectoryStream<Path>() {
            @Override
            public Iterator<Path> iterator() {
                return new Iterator<Path>() {
                    final Iterator<Path> delegate = childPaths.iterator();

                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public Path next() {
                        return delegate.next();
                    }
                };
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void delete(Path path) throws IOException {
        validatePathProvider(path);
        var deviceIdentifier = getDeviceIdentifier(path.toUri());
        if (Files.isDirectory(path)) {
            throw new IOException(String.format("Cannot delete directories. (%s)", path.toUri()));
        }
        if (!deleteFile(toDevicePath(path), deviceIdentifier.toString())) {
            throw new IOException(String.format("Could not delete %s", path.toUri()));
        }
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return path.toUri().equals(path2.toUri());
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public MTPFileStore getFileStore(Path path) throws IOException {
        validatePathProvider(path);
        var deviceIdentifier = getDeviceIdentifier(path.toUri());
        var fileStoreId = getFileStoreId(toDevicePath(path), deviceIdentifier.toString());
        return new MTPFileStore(this, deviceIdentifier, fileStoreId);
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        readAttributes(path, MTPFileAttribute.class);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        validatePathProvider(path);
        var deviceIdentifier = getDeviceIdentifier(path.toUri());
        String fileType;
        long size;
        if (isDirectory(toDevicePath(path), deviceIdentifier.toString())) {
            fileType = "directory";
            size = 0;
        } else {
            fileType = "file";
            size = size(toDevicePath(path), deviceIdentifier.toString());
        }
        return (A) new MTPFileAttribute(fileType, size);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return null;
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {

    }

    void validateURI(URI uri) {
        var scheme = uri.getScheme();
        if (scheme == null || !scheme.equalsIgnoreCase(getScheme())) {
            throw new IllegalArgumentException(String.format("URI scheme is not %s", getScheme()));
        }
        var deviceIdentifier = getDeviceIdentifier(uri);
        if (getDeviceInfo(deviceIdentifier.toString()) == null) {
            throw new FileSystemNotFoundException(String.format("Device %s could not be found", deviceIdentifier.toString()));
        }
    }

    void validatePathProvider(Path path) {
        if (!(path instanceof MTPPath)) {
            throw new ProviderMismatchException();
        }
    }

    private DeviceIdentifier getDeviceIdentifier(URI uri) {
        var schemeSpecificPart = uri.getSchemeSpecificPart();
        var sep = schemeSpecificPart.indexOf("!/");
        if (sep >= 0) {
            schemeSpecificPart = schemeSpecificPart.substring(0, sep);
        }
        var deviceSchema = schemeSpecificPart.split(":");
        if (deviceSchema.length != 3) {
            throw new IllegalArgumentException(String.format("Invalid device schema %s", schemeSpecificPart));
        }
        return new DeviceIdentifier(Integer.parseInt(deviceSchema[0]), Integer.parseInt(deviceSchema[1]), deviceSchema[2]);
    }

    private String toDevicePath(Path path) {
        validatePathProvider(path);
        var strPath = path.toString();
        if (strPath.length() > 1) {
            return strPath.replaceAll("(/+$)", "");  // remove trailing slashes
        }
        return strPath;
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

    @Value
    static class DeviceIdentifier {
        private final int vendor_id;
        private final int product_id;
        private final String serial;

        @Override
        public String toString() {
            return String.format("%d:%d:%s", vendor_id, product_id, serial);
        }
    }

    @Value
    public static class MTPDeviceInfo {
        private final String deviceId;
        private final String friendlyName;
        private final String description;
        private final String manufacturer;
        private final String serial;

        long busLocation;
        long devNum;

        public Map<String, String> toMap() {
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

    @Value
    private static class MTPFileAttribute implements BasicFileAttributes {
        private final String type;
        private final long size;

        @Override
        public FileTime lastModifiedTime() {
            return null;
        }

        @Override
        public FileTime lastAccessTime() {
            return null;
        }

        @Override
        public FileTime creationTime() {
            return null;
        }

        @Override
        public boolean isRegularFile() {
            return "file".equals(type);
        }

        @Override
        public boolean isDirectory() {
            return "directory".equals(type);
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return size;
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }
}
