package org.meltzg.jmlm.filesystem.mtp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

@RequiredArgsConstructor
public class MTPFileStore extends FileStore {

    private final MTPFileSystemProvider fileSystemProvider;
    @Getter
    private final MTPFileSystemProvider.DeviceIdentifier deviceIdentifier;
    private final String storageId;

    @Override
    public String name() {
        return storageId;
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long getTotalSpace() throws IOException {
        var storageProperties = fileSystemProvider.getFileStoreProperties(storageId, deviceIdentifier.toString());
        return storageProperties.getCapacity();
    }

    @Override
    public long getUsableSpace() throws IOException {
        var storageProperties = fileSystemProvider.getFileStoreProperties(storageId, deviceIdentifier.toString());
        return storageProperties.getCapacity();
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        var storageProperties = fileSystemProvider.getFileStoreProperties(storageId, deviceIdentifier.toString());
        return storageProperties.getFreeSpace();
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return false;
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        return false;
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return null;
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        return null;
    }
}
