package org.meltzg.jmlm.filesystem.mtp;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

@RequiredArgsConstructor
public class MTPFileStore extends FileStore {

    private final MTPFileSystemProvider fileSystemProvider;
    private final MTPFileSystemProvider.DeviceIdentifier deviceIdentifier;
    private final String storageId;

    @Override
    public String name() {
        return null;
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return 0;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return 0;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return 0;
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
