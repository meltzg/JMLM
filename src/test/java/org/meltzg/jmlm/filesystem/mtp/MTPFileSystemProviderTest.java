package org.meltzg.jmlm.filesystem.mtp;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
}