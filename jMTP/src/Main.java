import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.meltzg.jmtp.JMTP;
import org.meltzg.jmtp.models.MTPDevice;
import org.meltzg.jmtp.models.MTPObjectTree;

public class Main {
	public static void main(String[] args) {		
		JMTP j = new JMTP();
		List<MTPDevice> devices = j.getDevices();
		
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
		
		System.out.println("Connected? " + j.selectDevice(devices.get(selection).getDeviceId()));
//		MTPObjectTree oTree = j.getDeviceContent();
//		System.out.println(oTree.toPrettyString());
		
		String newId = j.transferToDevice("D:/Users/vader/Desktop/test space.mp3", "o2", "this/is/a/test.mp3");
		System.out.println("transfer to device successful: " + newId);
		
		boolean transSuccess = j.transferFromDevice(newId, "D:/Users/vader/Desktop/test/transfer.mp3");
		System.out.println("transfer from device successful: " + transSuccess);
		
		boolean removeSuccess = j.removeFromDevice(newId, "o2");
		System.out.println("remove from device successful: " + removeSuccess);

		try {
			j.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
