import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meltzg.jmlm.device.access.MTPContentInterface;
import org.meltzg.jmlm.device.models.AbstractContentDevice;

public class TestMTPContentDevice extends TestAbstractContentDevice {

	private static String deviceId = null;
	private static boolean connected = false;
	private static MTPContentInterface mtp = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws FileNotFoundException, IOException {
		initProps();
		
		mtp = MTPContentInterface.getInstance();
		deviceId = props.getProperty("device.mtp.id");
		libRootId1 = props.getProperty("device.mtp.lib1");
		libRootId2 = props.getProperty("device.mtp.lib2");
		
		List<AbstractContentDevice>devices = mtp.getDevices();
		for (AbstractContentDevice d : devices) {
			if (d.getDeviceId().equals(deviceId)) {
				device = d;
				connected = mtp.selectDevice(deviceId);
				break;
			}
		}
			
		commonDeviceInit();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws IOException {
		if (mtp != null) {
			mtp.close();
		}
	}
	
	@Test
	public void testDeviceConnection() {
		assertTrue("Should be able to connect to device", connected);
	}
}
