package org.meltzg.jmlm.filesystem.mtp;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@RequiredArgsConstructor
public class MTPPath implements Path {
    private final MTPFileSystem fileSystem;
    private final byte[] path;

    @Override
    public MTPFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return (path.length > 0) && (path[0] == '/');
    }

    @Override
    public Path getRoot() {
        return null;
    }

    @Override
    public Path getFileName() {
        return null;
    }

    @Override
    public Path getParent() {
        return null;
    }

    @Override
    public int getNameCount() {
        return 0;
    }

    @Override
    public Path getName(int index) {
        return null;
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return null;
    }

    @Override
    public boolean startsWith(Path other) {
        return false;
    }

    @Override
    public boolean endsWith(Path other) {
        return false;
    }

    @Override
    public Path normalize() {
        return null;
    }

    @Override
    public Path resolve(Path other) {
        return null;
    }

    @Override
    public Path relativize(Path other) {
        return null;
    }

    @Override
    public URI toUri() {
        try {
            var absolutePath = toAbsolutePath().toString(false);
            return new URI(String.format("%s:%s!%s", fileSystem.provider().getScheme(), fileSystem.getDeviceIdentifier(), absolutePath));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public MTPPath toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }
        var absoluteBytes = new byte[path.length + 1];
        System.arraycopy(path, 0, absoluteBytes, 1, path.length);
        return new MTPPath(fileSystem, absoluteBytes);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return null;
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }

    @Override
    public int compareTo(Path other) {
        return 0;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean decode) {
        if (!decode) {
            return new String(path);
        }
        try {
            return URLDecoder.decode(new String(path), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return new String(path);
        }
    }
}
