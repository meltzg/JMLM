package org.meltzg.jmlm.device.content;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.meltzg.TestConfig;
import org.meltzg.jmlm.device.content.FSAudioContentNode;
import org.junit.BeforeClass;

public class FSAudioContentNodeTest extends AbstractContentNodeTest {
    
    private static String testFile;
    private static String testDir;
    private static String testInvalidFile;
    private static String testNotFound;    
    
    private static String testGenre;
    private static String testArtist;
    private static String testAlbum;
    private static String testTitle;
    private static int testDiscNum;
    private static int testTrackNum;

    @BeforeClass
    public static void setUpBeforeClass() throws FileNotFoundException, IOException {
        Properties props = TestConfig.getProps();
        
        testFile = props.getProperty("test.fsacn.file");
        testDir = props.getProperty("test.fsacn.dir");
        testInvalidFile = props.getProperty("test.fsacn.invfile");
        testNotFound = props.getProperty("test.fsacn.nonexist");
        
        testGenre = props.getProperty("test.fsacn.genre");
        testArtist = props.getProperty("test.fsacn.artist");
        testAlbum = props.getProperty("test.fsacn.album");
        testTitle = props.getProperty("test.fsacn.title");
        testDiscNum = Integer.parseInt(props.getProperty("test.fsacn.discnum"));
        testTrackNum = Integer.parseInt(props.getProperty("test.fsacn.tracknum"));
    }

    @Override
    public void testReadContentNode() {
        File file = new File(testFile);
        FSAudioContentNode node = new FSAudioContentNode(testFile);

        assertEquals("Node should have full path as ID: ", file.getAbsolutePath(), node.getId());
        assertEquals("Node should have parent dir as pID: ", file.getParentFile().getAbsolutePath(), node.getPId());
        assertEquals("Node should have file name as origName: ", file.getName(), node.getOrigName());
        assertEquals("Node should have genre set: ", testGenre, node.getGenre());
        assertEquals("Node should have artest set: ", testArtist, node.getArtist());
        assertEquals("Node should have album set: ", testAlbum, node.getAlbum());
        assertEquals("Node should have title set: ", testTitle, node.getTitle());
        assertEquals("Node should have discnum set: ", testDiscNum, node.getDiscNum());
        assertEquals("Node should have tracknum set: ", testTrackNum, node.getTrackNum());
        assertEquals("Node should have correct size: ", file.length(), node.getSize());
        assertFalse("Node should not be a directory: ", node.isDir());
        assertTrue("Node should be valid: ", node.isValid());
    }

    @Override
    public void testReadDirNode() {
        File file = new File(testDir);
        FSAudioContentNode node = new FSAudioContentNode(testDir);

        assertEquals("Node should have full path as ID: ", file.getAbsolutePath(), node.getId());
        assertEquals("Node should have parent dir as pID: ", file.getParentFile().getAbsolutePath(), node.getPId());
        assertEquals("Node should have folder name as origName: ", file.getName(), node.getOrigName());
        assertNull("Node should have genre set: ", node.getGenre());
        assertNull("Node should not have artest set: ", node.getArtist());
        assertNull("Node should not have album set: ", node.getAlbum());
        assertNull("Node should not have title set: ", node.getTitle());
        assertEquals("Node should not have discnum set: ", 0, node.getDiscNum());
        assertEquals("Node should not have tracknum set: ", 0, node.getTrackNum());
        assertEquals("Node should have correct size: ", 0, node.getSize());
        assertTrue("Node should be a directory: ", node.isDir());
        assertTrue("Node should be valid: ", node.isValid());
    }

    @Override
    public void testInvalidContentNode() {
        File file = new File(testInvalidFile);
        FSAudioContentNode node = new FSAudioContentNode(testInvalidFile);

        assertEquals("Node should have full path as ID: ", file.getAbsolutePath(), node.getId());
        assertEquals("Node should have parent dir as pID: ", file.getParentFile().getAbsolutePath(), node.getPId());
        assertEquals("Node should have folder name as origName: ", file.getName(), node.getOrigName());
        assertNull("Node should have genre set: ", node.getGenre());
        assertNull("Node should not have artest set: ", node.getArtist());
        assertNull("Node should not have album set: ", node.getAlbum());
        assertNull("Node should not have title set: ", node.getTitle());
        assertEquals("Node should not have discnum set: ", 0, node.getDiscNum());
        assertEquals("Node should not have tracknum set: ", 0, node.getTrackNum());
        assertEquals("Node should have correct size: ", file.length(), node.getSize());
        assertFalse("Node should not be a directory: ", node.isDir());
        assertFalse("Node should be invalid: ", node.isValid());
    }

    @Override
    public void testContentNotFound() {
        File file = new File(testNotFound);
        FSAudioContentNode node = new FSAudioContentNode(testNotFound);

        assertEquals("Node should have full path as ID: ", file.getAbsolutePath(), node.getId());
        assertEquals("Node should have parent dir as pID: ", file.getParentFile().getAbsolutePath(), node.getPId());
        assertEquals("Node should have folder name as origName: ", file.getName(), node.getOrigName());
        assertFalse("Node should be invalid: ", node.isValid());
    }
}