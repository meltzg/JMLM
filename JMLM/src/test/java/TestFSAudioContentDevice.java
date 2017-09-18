import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.meltzg.jmlm.content.models.ContentRoot;
import org.meltzg.jmlm.device.models.FSAudioContentDevice;

public class TestFSAudioContentDevice extends TestAbstractContentDevice {

	@BeforeClass
	public static void setUpBeforeClass() throws FileNotFoundException, IOException {
		initProps();
		
		String libRoot1 = props.getProperty("device.fsa.lib1");
		String libRoot2 = props.getProperty("device.fsa.lib2");
		device = new FSAudioContentDevice(UUID.randomUUID().toString(), "Test Device", "test fsa", null);
		device.addContentRoot(libRoot1);
		device.addContentRoot(libRoot2);
		
		Map<String, ContentRoot> libRoots = device.getLibraryRoots();
		String[] libRootIds = libRoots.keySet().toArray(new String[libRoots.keySet().size()]);
		libRootId1 = libRootIds[0];
		libRootId2 = libRootIds[1];
			
		commonDeviceInit();
	}

}
