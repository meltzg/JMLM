import java.io.IOException;
import java.util.List;

import org.meltzg.jmtp.JMTP;
import org.meltzg.jmtp.models.MTPDevice;
import org.meltzg.jmtp.models.MTPObjectTree;

public class Main {
	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println("Press Enter to continue");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JMTP j = new JMTP();
		List<MTPDevice> devices = j.getDevices();
		System.out.println(devices);
		System.out.println("Connected? " + j.selectDevice(devices.get(0).getDeviceId()));
		MTPObjectTree oTree = j.getDeviceContent();
		System.out.println(oTree.toPrettyString());
		
		try {
			j.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
