import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.meltzg.jmlm.content.models.AbstractContentTree;
import org.meltzg.jmlm.device.models.AbstractContentDevice;

/**
 * @author vader
 * 
 * Test cases that extend TestAbstractDevice must implement @BeforeClass function with the pattern:
 * setUpBeforeClass() {
 * 		initProps()
 * 		// custom device setup
 * 		commonDeviceInit()
 * }
 */
public abstract class TestAbstractContentDevice {

	protected static Properties props = null;
	
	protected static AbstractContentDevice device = null;
	protected static String testFile = null;
	protected static String libRootId1 = null;
	protected static String libRootId2 = null;
	protected static String transToDest1 = null;
	protected static String transToDest2 = null;
	protected static String transFromDest1 = null;
	protected static String transFromDest2 = null;
	
	
	private static AbstractContentTree newSubTree1;
	private static AbstractContentTree newSubTree2;
	
	protected static void initProps() throws FileNotFoundException, IOException {
		props = new Properties();
		props.load(new FileReader("./src/test/resources/test-props.properties"));
		testFile = props.getProperty("test.file");
		transToDest1 = props.getProperty("test.file.trans.to.1");
		transToDest2 = props.getProperty("test.file.trans.to.2");
		transFromDest1 = props.getProperty("test.file.trans.from.1");
		transFromDest2 = props.getProperty("test.file.trans.from.2");
	}
	
	protected static void commonDeviceInit() {
		newSubTree1 = device.transferToDevice(testFile, libRootId1, transToDest1);
		newSubTree2 = device.transferToDevice(testFile, libRootId2, transToDest2);
	}

	@Test
	public void testDeviceContent() {
		assertNotNull("Device should have a content root", device.getContentRoot());
		assertTrue("Device should have more than 0 nodes", device.getContentRoot().getIdToNodes().size() > 1);
	}

	@Test
	public void testTransferContentTo() {
		assertNotNull("Device should be able to transfer a file to the device", newSubTree1);
		assertNotNull("Device should be able to transfer another file to the device", newSubTree2);
	}
	
	@Test
	public void testTransferFrom() {
		boolean transSuccess1 = false;
		boolean transSuccess2 = false;

		if (newSubTree1 != null && newSubTree2 != null) {
			String id1 = newSubTree1.getChildren().get(0).getChildren().get(0).getChildren().get(0).getId();
			String id2 = newSubTree2.getChildren().get(0).getChildren().get(0).getChildren().get(0).getId();

			transSuccess1 = device.transferFromDevice(id1, transFromDest1);
			transSuccess2 = device.transferFromDevice(id2, transFromDest2);
		}
		
		assertTrue("Device should be able to transfer a file from the device", transSuccess1);
		assertTrue("Device should be able to transfer aanother file from the device", transSuccess2);
	}
	
	@Test
	public void testMoveOnDevice() {
		AbstractContentTree moveTree1 = null;
		AbstractContentTree moveTree2 = null;
		
		if (newSubTree1 != null && newSubTree2 != null) {
			String id1 = newSubTree1.getChildren().get(0).getChildren().get(0).getChildren().get(0).getId();
			String id2 = newSubTree2.getChildren().get(0).getChildren().get(0).getChildren().get(0).getId();
			
			moveTree1 = device.moveOnDevice(id1, libRootId2, "this/is/a/move", ".");
			moveTree2 = device.moveOnDevice(id2, libRootId1, "this/is/a/move", ".");
		}
		
		assertNotNull("Device should be able to move a file on the device", moveTree1);
		assertNotNull("Device should be able to move another file on the device", moveTree2);
	}
	
	@Test
	public void testRemoveFrom() {
		String removeSuccess1 = null;
		String removeSuccess2 = null;

		if (newSubTree1 != null && newSubTree2 != null) {
			String id1 = newSubTree1.getChildren().get(0).getChildren().get(0).getId();
			String id2 = newSubTree2.getChildren().get(0).getChildren().get(0).getId();

			removeSuccess1 = device.removeFromDevice(id1, libRootId1);
			removeSuccess2 = device.removeFromDevice(id2, libRootId2);
		}
		
		assertNotNull("Device should be able to remove a file from the device", removeSuccess1);
		assertNotNull("Device should be able to remove aanother file from the device", removeSuccess2);
	}

}
