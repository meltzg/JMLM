import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.meltzg.jmlm.content.models.AbstractContentTree;
import org.meltzg.jmlm.content.models.ContentRoot;
import org.meltzg.jmlm.device.access.MTPContentInterface;
import org.meltzg.jmlm.device.models.AbstractContentDevice;
import org.meltzg.jmlm.device.models.FSAudioContentDevice;

public class Main {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		String testFile = "C:/Users/vader/Desktop/test.flac";

		System.out.println("[0] MTP");
		System.out.println("[1] FSA");
		int testType = -1;
		while (testType < 0 || testType > 1) {
			System.out.println("Enter test type");
			testType = in.nextInt();
		}

		AbstractContentDevice device = null;
		String libRootId1 = null;
		String libRootId2 = null;
		MTPContentInterface j = MTPContentInterface.getInstance();

		if (testType == 0) {
			List<AbstractContentDevice> devices = j.getDevices();

			for (int i = 0; i < devices.size(); i++) {
				System.out.println("[" + i + "] " + devices.get(i).getFriendlyName());
			}

			int selection = -1;
			while (selection < 0 || selection >= devices.size()) {
				System.out.println("Enter device selection");
				selection = in.nextInt();
			}
			in.close();

			boolean connected = j.selectDevice(devices.get(selection).getDeviceId());
			// boolean connected = j.selectDevice("not_real");
			System.out.println("Connected? " + connected);

			if (connected) {
				device = devices.get(selection);
				device.buildContentRoot();
				libRootId1 = "o2";
				libRootId2 = "o9301";
			}

		} else if (testType == 1) {
			device = new FSAudioContentDevice(UUID.randomUUID().toString(), "Test Device", "test fsa", null);
			device.addContentRoot("D:\\Users\\vader\\Music");
			device.addContentRoot("D:\\Users\\vader\\Music2");
			Map<String, ContentRoot> libRoots = device.getLibraryRoots();
			String[] libRootIds = libRoots.keySet().toArray(new String[libRoots.keySet().size()]);
			libRootId1 = libRootIds[0];
			libRootId2 = libRootIds[1];
		}

		if (device != null && libRootId1 != null && libRootId2 != null) {
//			System.out.println(device.getContentRoot().toPrettyString());
			System.out.println("-------------------");

			AbstractContentTree newSubTree1 = device.transferToDevice(testFile, libRootId1, "this/is/a/test1.mp3");
			System.out.println("transfer1 to device successful: " + newSubTree1);
			AbstractContentTree newSubTree2 = device.transferToDevice(testFile, libRootId2, "this/is/a/test2.mp3");
			System.out.println("transfer2 to device successful: " + newSubTree2);
//			System.out.println(device.getContentRoot().toPrettyString());
			System.out.println("-------------------");

			if (newSubTree1 != null && newSubTree2 != null) {
				String id1 = newSubTree1.getChildren().get(0).getChildren().get(0).getChildren().get(0).getId();
				String id2 = newSubTree2.getChildren().get(0).getChildren().get(0).getChildren().get(0).getId();

				boolean transSuccess1 = device.transferFromDevice(id1, "C:/Users/vader/Desktop/test/transfer1.mp3");
				System.out.println("transfer1 from device successful: " + transSuccess1);
				boolean transSuccess2 = device.transferFromDevice(id2, "C:/Users/vader/Desktop/test/transfer2.mp3");
				System.out.println("transfer2 from device successful: " + transSuccess2);
				System.out.println("-------------------");

				String removeSuccess1 = device.removeFromDevice(id1, libRootId1);
				System.out.println("remove1 from device successful: " + removeSuccess1);
				String removeSuccess2 = device.removeFromDevice(id2, libRootId2);
				System.out.println("remove2 from device successful: " + removeSuccess2);
//				System.out.println(device.getContentRoot().toPrettyString());
				System.out.println("-------------------");
			}
		}

		try {
			j.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
