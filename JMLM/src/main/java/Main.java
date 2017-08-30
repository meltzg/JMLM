import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.meltzg.jmlm.content.models.MTPContentTree;
import org.meltzg.jmlm.device.access.MTPContentInterface;
import org.meltzg.jmlm.device.models.AbstractContentDevice;

public class Main {
	public static void main(String[] args) {		
		MTPContentInterface j = MTPContentInterface.getInstance();
		List<AbstractContentDevice> devices = j.getDevices();
		
		for (int i = 0; i < devices.size(); i++) {
			System.out.println("[" + i + "] " + devices.get(i).getFriendlyName());
		}
		
		int selection = -1;
		Scanner in = new Scanner(System.in);
		while (selection < 0 || selection >= devices.size()) {
			System.out.println("Enter device selection");
			selection = in.nextInt();
		}
		in.close();
		
		boolean connected = j.selectDevice(devices.get(selection).getDeviceId());
//		boolean connected = j.selectDevice("not_real");
		System.out.println("Connected? " + connected);
		
		if (connected) {
			AbstractContentDevice device = devices.get(selection);
//			device.buildContentRoot();
//			System.out.println(device.getContentRoot().toPrettyString());
			
			MTPContentTree newSubTree1 = j.transferToDevice("D:/Users/vader/Desktop/test space.mp3", "o2", "this/is/a/test1.mp3");
			System.out.println("transfer1 to device successful: " + newSubTree1);
			MTPContentTree newSubTree2 = j.transferToDevice("D:/Users/vader/Desktop/test space.mp3", "o2", "this/is/a/test2.mp3");
			System.out.println("transfer2 to device successful: " + newSubTree2);
			
			if (newSubTree1 != null && newSubTree2 != null) {
				String id1 = newSubTree1.getChildren().get(0).getChildren().get(0).getChildren().get(0).getId();
				String id2 = newSubTree2.getId();

				boolean transSuccess1 = j.transferFromDevice(id1, "D:/Users/vader/Desktop/test/transfer1.mp3");
				System.out.println("transfer1 from device successful: " + transSuccess1);
				boolean transSuccess2 = j.transferFromDevice(id2, "D:/Users/vader/Desktop/test/transfer2.mp3");
				System.out.println("transfer2 from device successful: " + transSuccess2);
				
				String removeSuccess1 = j.removeFromDevice(id1, "o2");
				System.out.println("remove1 from device successful: " + removeSuccess1);
				String removeSuccess2 = j.removeFromDevice(id2, "o2");
				System.out.println("remove2 from device successful: " + removeSuccess2);	
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
