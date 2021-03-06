package org.meltzg.jmlm.device;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.meltzg.jmlm.CommonUtil.RESOURCEDIR;
import static org.meltzg.jmlm.CommonUtil.TMPDIR;

@RunWith(SpringRunner.class)
@DataJpaTest
public class FileSystemAudioContentDeviceTest {

    @Autowired
    AudioContentRepository contentRepo;

    FileSystemAudioContentDevice device;

    @Before
    public void setUp() throws Exception {
        device = new FileSystemAudioContentDevice("Device Name", contentRepo);
        FileUtils.forceMkdir(Paths.get(TMPDIR).toFile());
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(Paths.get(TMPDIR).toFile());
    }

    @Test
    public void testAddLibraryRoot() throws IOException, URISyntaxException {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        device.addLibraryRoot(libraryRootPath);

        assertEquals(1, device.getStorageDevices().size());

        var storageDevice = new ArrayList<>(device.getStorageDevices().values()).get(0);
        var libraryRootFile = new File(libraryRootPath);
        assertTrue(storageDevice.getCapacity() >= libraryRootFile.getFreeSpace());
        assertTrue(storageDevice.getCapacity() < libraryRootFile.getTotalSpace());
        assertEquals(11, device.getContent().size());
    }

    @Test
    public void testAddLibraryRootMultiple() throws IOException, URISyntaxException {
        String[] libraryRoots = {
                RESOURCEDIR + "/audio/jst2018-12-09",
                RESOURCEDIR + "/audio/kwgg2016-10-29"
        };

        for (var root : libraryRoots) {
            device.addLibraryRoot(root, false);
        }
        device.scanDeviceContent();

        assertEquals(libraryRoots.length, device.getLibraryRoots().size());
        for (var root : device.getLibraryRoots().values()) {
            assertTrue(Arrays.stream(libraryRoots).anyMatch(root::endsWith));
        }
        assertEquals(1, device.getStorageDevices().size());
        assertEquals(2, device.getLibraryRootCapacities().size());

        var storageDevice = new ArrayList<>(device.getStorageDevices().values()).get(0);
        assertTrue(Math.abs(storageDevice.getCapacity() - device.getLibraryRootCapacities().values()
                .stream().mapToLong(Long::longValue).sum()) <= 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLibraryRootParent() throws IOException, URISyntaxException {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        var parentLibraryPath = libraryRootPath.substring(0, libraryRootPath.lastIndexOf('/'));
        device.addLibraryRoot(libraryRootPath);
        device.addLibraryRoot(parentLibraryPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLibraryRootChild() throws IOException, URISyntaxException {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        var parentLibraryPath = libraryRootPath.substring(0, libraryRootPath.lastIndexOf('/'));
        device.addLibraryRoot(parentLibraryPath);
        device.addLibraryRoot(libraryRootPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLibraryRootNotDirectory() throws IOException, URISyntaxException {
        device.addLibraryRoot(RESOURCEDIR + "/audio/jst2018-12-09.json");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLibraryRootNotExist() throws IOException, URISyntaxException {
        device.addLibraryRoot(RESOURCEDIR + "/audio/NotFound");
    }

    @Test
    public void testAddContent() throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException, URISyntaxException {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        var content = device.addContent(isfs, testSubLibPath,
                device.getLibraryRoots().keySet().iterator().next());
        assertTrue(device.getContent().containsKey(content.getId()));
    }

    @Test(expected = CannotReadException.class)
    public void testAddContentInvalidType() throws IOException, ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException, URISyntaxException {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09.json";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        var content = device.addContent(isfs, testSubLibPath,
                device.getLibraryRoots().keySet().iterator().next());
        assertNull(content);
        assertFalse(Paths.get(TMPDIR, testSubLibPath).toFile().exists());
    }

    @Test
    public void testAddContentDirectory() throws ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException, IOException, URISyntaxException {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        try {
            device.addContent(isfs, testSubLibPath,
                    device.getLibraryRoots().keySet().iterator().next());
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Is a directory"));
        }

        assertFalse(Paths.get(TMPDIR, testSubLibPath).toFile().exists());
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void testAddContentDuplicate() throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException, URISyntaxException {
        var tmpRoot1 = Paths.get(TMPDIR, "1");
        var tmpRoot2 = Paths.get(TMPDIR, "2");

        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs1 = Files.newInputStream(Paths.get(testFile));
        var isfs2 = Files.newInputStream(Paths.get(testFile));
        FileUtils.forceMkdir(tmpRoot1.toFile());
        FileUtils.forceMkdir(tmpRoot2.toFile());

        device.addLibraryRoot(tmpRoot1.toString());
        device.addLibraryRoot(tmpRoot2.toString());

        var libIds = new ArrayList<>(device.getLibraryRoots().keySet());

        device.addContent(isfs1, testSubLibPath, libIds.get(0));
        device.addContent(isfs2, testSubLibPath, libIds.get(1));
    }

    @Test
    public void testDeleteContent() throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException, URISyntaxException {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        var content = device.addContent(isfs, testSubLibPath,
                device.getLibraryRoots().keySet().iterator().next());
        device.deleteContent(content.getId());
        assertFalse(device.getContent().containsKey(content.getId()));
        assertFalse(Files.exists(Paths.get(TMPDIR, testSubLibPath)));
    }

    @Test(expected = FileNotFoundException.class)
    public void testDeleteContentNotFound() throws IOException, URISyntaxException {
        device.addLibraryRoot(TMPDIR);
        device.deleteContent(9001L);
    }

    @Test
    public void testMoveContent() throws IOException, ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException, URISyntaxException {
        var tmpRoot1 = Paths.get(TMPDIR, "1");
        var tmpRoot2 = Paths.get(TMPDIR, "2");

        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        FileUtils.forceMkdir(tmpRoot1.toFile());
        FileUtils.forceMkdir(tmpRoot2.toFile());

        device.addLibraryRoot(tmpRoot1.toString());
        device.addLibraryRoot(tmpRoot2.toString());

        var libIds = new ArrayList<>(device.getLibraryRoots().keySet());

        var content = device.addContent(isfs, testSubLibPath, libIds.get(0));
        device.moveContent(content.getId(), libIds.get(1));

        content = device.getContent(content.getId());
        var contentLocation = device.getContentLocations().get(content.getId());

        assertEquals(contentLocation.getLibraryId(), libIds.get(1));
        assertTrue(Files.isRegularFile(Paths.get(device.getLibraryRoots().get(libIds.get(1)),
                contentLocation.getLibrarySubPath())));
    }

    @Test(expected = FileNotFoundException.class)
    public void testMoveContentNotFound() throws IOException, URISyntaxException {
        device.addLibraryRoot(TMPDIR);
        device.moveContent(9001L, device.getLibraryRoots().keySet().iterator().next());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveContentLibraryNotFound() throws IOException, ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException, URISyntaxException {
        var tmpRoot1 = Paths.get(TMPDIR, "1");
        var tmpRoot2 = Paths.get(TMPDIR, "2");

        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        FileUtils.forceMkdir(tmpRoot1.toFile());
        FileUtils.forceMkdir(tmpRoot2.toFile());

        device.addLibraryRoot(tmpRoot1.toString());
        device.addLibraryRoot(tmpRoot2.toString());

        var libIds = new ArrayList<>(device.getLibraryRoots().keySet());

        var content = device.addContent(isfs, testSubLibPath, libIds.get(0));
        device.moveContent(content.getId(), UUID.randomUUID());

    }

    @Test
    public void testGetContentStream() throws IOException, ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException, URISyntaxException {
        var tmpRoot1 = Paths.get(TMPDIR, "1");
        var tmpRoot2 = Paths.get(TMPDIR, "2");

        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        FileUtils.forceMkdir(tmpRoot1.toFile());
        FileUtils.forceMkdir(tmpRoot2.toFile());

        var device2 = new FileSystemAudioContentDevice("Device 2 Name", contentRepo);
        device.addLibraryRoot(tmpRoot1.toString());
        device2.addLibraryRoot(tmpRoot2.toString());

        var content = device.addContent(isfs, testSubLibPath, device.getLibraryRoots().keySet().iterator().next());
        var content2 = device2.addContent(device.getContentStream(content.getId()), testSubLibPath,
                device2.getLibraryRoots().keySet().iterator().next());
        assertEquals(content, content2);
        assertTrue(Files.exists(Paths.get(tmpRoot2.toString(), testSubLibPath)));
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetContentStreamNotFound() throws IOException, URISyntaxException {
        device.addLibraryRoot(TMPDIR);
        device.getContentStream(9001L);
    }

    @Test
    public void testGetParentDir() {
        var parent = device.getParentDir(Paths.get(RESOURCEDIR, "audio"));
        assertEquals(Paths.get(RESOURCEDIR), parent);
    }

    @Test
    public void testGetParentDirAboveRoot() {
        device.setRootPath(RESOURCEDIR);
        var parent = device.getParentDir(Paths.get(RESOURCEDIR).getParent());
        assertEquals(Paths.get(RESOURCEDIR), parent);
    }

    @Test
    public void testGetChildrenDirs() throws IllegalAccessException, IOException {
        var children = device.getChildrenDirs(Paths.get(RESOURCEDIR, "audio"));
        assertEquals(2, children.size());
        assertTrue(children.containsAll(Arrays.asList(Paths.get(RESOURCEDIR, "/audio/jst2018-12-09"),
                Paths.get(RESOURCEDIR, "/audio/kwgg2016-10-29"))));
    }

    @Test(expected = IllegalAccessException.class)
    public void testGetChildrenDirsIllegalAccess() throws IllegalAccessException, IOException {
        device.setRootPath(RESOURCEDIR);
        device.getChildrenDirs(Paths.get(RESOURCEDIR).getParent());
    }
}