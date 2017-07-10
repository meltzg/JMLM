import java.io.IOException;

import org.meltzg.jmtp.JMTP;

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
		
		j.getDevices();
		try {
			j.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
