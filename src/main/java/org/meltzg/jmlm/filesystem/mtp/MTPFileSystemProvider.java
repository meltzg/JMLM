package org.meltzg.jmlm.filesystem.mtp;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return ((MTPPath) path).getFileSystem().newByteChannel(path, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        validatePathProvider(dir);
        return ((MTPPath) dir).getFileSystem().newDirectoryStream(dir, filter);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new ReadOnlyFileSystemException();
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
        return path.toAbsolutePath().equals(path2.toAbsolutePath());
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public MTPFileStore getFileStore(Path path) throws IOException {
        return null;
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {

    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return null;
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
}
