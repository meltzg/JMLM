package org.meltzg.jmlm.filesystem.mtp;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MTPFileSystemProviderTest {

    final MTPFileSystemProvider.DeviceIdentifier deviceIdentifier = new MTPFileSystemProvider.DeviceIdentifier(
            16642, 4497, "F2000018D562F2A412B4"
    );

    MTPFileSystemProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new MTPFileSystemProvider();
    }

    @Test
    public void newFileSystem() throws URISyntaxException, IOException {
        assertNotNull(provider.newFileSystem(new URI(String.format("mtp:%s!/", deviceIdentifier.toString())), null));
    }

    @Test(expected = FileSystemAlreadyExistsException.class)
    public void newFileSystemDuplicate() throws URISyntaxException, IOException {
        var uri = new URI(String.format("mtp:%s!/", deviceIdentifier.toString()));
        provider.newFileSystem(uri, null);
        provider.newFileSystem(uri, null);
    }

    @Test
    public void getFileSystem() throws URISyntaxException, IOException {
        var uri = new URI(String.format("mtp:%s!/", deviceIdentifier.toString()));
        var fs = provider.newFileSystem(uri, null);
        var retrievedFs = provider.getFileSystem(uri);
        assertEquals(fs, retrievedFs);
    }

    @Test(expected = FileSystemNotFoundException.class)
    public void getFileSystemNotFound() throws URISyntaxException, IOException {
        var uri = new URI(String.format("mtp:%s!/", deviceIdentifier.toString()));
        provider.getFileSystem(uri);
    }

    @Test
    public void getFileSystemCreated() throws URISyntaxException, IOException {
        var uri = new URI(String.format("mtp:%s!/", deviceIdentifier.toString()));
        assertNotNull(provider.getFileSystem(uri, true));
    }

    @Test
    public void getPath() throws UnsupportedEncodingException, URISyntaxException {
        var strPath = "Internal Storage";
        var uri = getURI(strPath);
        var path = provider.getPath(uri);
        assertEquals(String.format("/%s", strPath), path.toString());
        assertEquals(uri, path.toUri());
    }

    @Test
    public void getFileStore() throws URISyntaxException, IOException {
        var uri = getURI("Internal storage");
        var path = provider.getPath(uri);
        var fileStore = provider.getFileStore(path);
        assertEquals("0x10001", fileStore.name());
    }

    @Test
    public void validateUri() throws URISyntaxException {
        var uri = new URI(String.format("mtp:%s!/", deviceIdentifier.toString()));
        provider.validateURI(uri);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateUriNoSchema() throws URISyntaxException {
        var uri = new URI(String.format("/%s!/", deviceIdentifier.toString()));
        provider.validateURI(uri);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateUriBadDeviceIdentifier() throws URISyntaxException {
        var uri = new URI(String.format("/%s!/", "not:3:parts:aah"));
        provider.validateURI(uri);
    }

    @Test(expected = FileSystemNotFoundException.class)
    public void validateUriDeviceNotFound() throws URISyntaxException {
        var uri = new URI(String.format("mtp:%s!/", "1:2:serial"));
        provider.validateURI(uri);
    }

    URI getURI(String path) throws URISyntaxException, UnsupportedEncodingException {
        var uriStr = String.format("mtp:%s!/%s", deviceIdentifier.toString(), URLEncoder.encode(path, StandardCharsets.UTF_8.toString()));
        return new URI(uriStr);
    }
}