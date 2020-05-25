package org.meltzg.jmlm.filesystem.mtp;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class MTPFileSystemProviderTest {

    static final MTPFileSystemProvider.DeviceIdentifier deviceIdentifier = new MTPFileSystemProvider.DeviceIdentifier(
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
        var path = Paths.get(uri);
        assertEquals(String.format("/%s", strPath), path.toString());
        assertEquals(uri, path.toUri());
    }

    @Test
    public void getFileStore() throws URISyntaxException, IOException {
        var uri = getURI("Internal storage");
        var path = Paths.get(uri);
        var fileStore = Files.getFileStore(path);
        assertEquals("0x10001", fileStore.name());
    }

    @Test
    public void readFile() throws IOException, URISyntaxException, InterruptedException {
        var uri = getURI("Internal storage/Contents/Sample");
        var path = Paths.get(uri);
        try (var stream = Files.newDirectoryStream(path)) {
            var validated = false;
            for (var child : stream) {
                if (Files.isRegularFile(child) && child.toString().endsWith(".flac")) {
                    validated = true;
                    validateFLAC(child);
                    break;
                }
            }
            assertTrue("validated at least 1 file", validated);
        }
    }

    @Test
    public void fileCRUD() throws IOException, URISyntaxException, InterruptedException {
        var sourcePath = Paths.get(getURI("Internal storage/Contents/Sample"));
        var sinkPath = Paths.get(getURI("SD card/file.flac"));
        try (var stream = Files.newDirectoryStream(sourcePath)) {
            var validated = false;
            for (var child : stream) {
                if (Files.isRegularFile(child) && child.toString().endsWith(".flac")) {
                    validated = true;
                    Files.write(sinkPath, Files.readAllBytes(child));
                    validateFLAC(sinkPath);
                    break;
                }
            }
            assertTrue("validated at least 1 file", validated);
            Files.delete(sinkPath);
            assertFalse(Files.exists(sinkPath));
        }
    }

    @Test
    public void walkFileSystem() throws IOException, URISyntaxException {
        var rootPath = Paths.get(getURI(""));
        var storagePath = Paths.get(getURI("Internal storage"));
        var dirPath = Paths.get(getURI("Internal storage/Contents"));

        var rootSet = Files.walk(rootPath).map(Path::toUri).collect(Collectors.toSet());
        var storageSet = Files.walk(storagePath).map(Path::toUri).collect(Collectors.toSet());
        var dirSet = Files.walk(dirPath).map(Path::toUri).collect(Collectors.toSet());

        assertTrue(rootSet.size() > 1);
        assertTrue(storageSet.size() > 1);
        assertTrue(dirSet.size() > 1);

        assertTrue(storageSet.containsAll(dirSet));
        assertTrue(rootSet.containsAll(storageSet));
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

    @Test
    public void isDirectory() throws UnsupportedEncodingException, URISyntaxException {
        var dirPath = Paths.get(getURI("Internal storage/Contents/Sample"));
        var storePath = Paths.get(getURI("Internal storage"));
        var filePath = Paths.get(getURI("Internal storage/Contents/Sample/01.Spanish Harlem_Chesky Record.flac"));
        var rootPath = Paths.get(getURI(""));
        assertTrue(Files.isDirectory(dirPath));
        assertTrue(Files.isDirectory(storePath));
        assertFalse(Files.isDirectory(filePath));
        assertTrue(Files.isDirectory(rootPath));
    }

    @Test
    public void size() throws IOException, URISyntaxException {
        var dirPath = Paths.get(getURI("Internal storage/Contents/Sample"));
        var storePath = Paths.get(getURI("Internal storage"));
        var filePath = Paths.get(getURI("Internal storage/Contents/Sample/01.Spanish Harlem_Chesky Record.flac"));
        var rootPath = Paths.get(getURI(""));
        assertEquals(0, Files.size(dirPath));
        assertEquals(0, Files.size(storePath));
        assertEquals(149637081, Files.size(filePath));
        assertEquals(0, Files.size(rootPath));
    }

    private void validateFLAC(Path path) throws IOException {
        var tmpFile = File.createTempFile("temp", ".flac");
        var outputStream = new FileOutputStream(tmpFile.getAbsolutePath());
        outputStream.write(Files.readAllBytes(path));
        outputStream.close();
        tmpFile.deleteOnExit();

        var processBuilder = new ProcessBuilder()
                .command("bash", "-c", String.format("flac -t %s", tmpFile.getAbsolutePath()))
                .redirectErrorStream(true);
        var p = processBuilder.start();

        var stdIn = new BufferedReader(new InputStreamReader(p.getInputStream()));

        var outputLines = new ArrayList<String>();
        String buffer;
        while ((buffer = stdIn.readLine()) != null) {
            outputLines.add(buffer);
        }
        assertTrue(outputLines.get(outputLines.size() - 1).strip().endsWith("ok"));
    }

    static URI getURI(String path) throws URISyntaxException, UnsupportedEncodingException {
        var uriStr = String.format("mtp:%s!/%s", deviceIdentifier.toString(), URLEncoder.encode(path, StandardCharsets.UTF_8.toString()));
        return new URI(uriStr);
    }
}