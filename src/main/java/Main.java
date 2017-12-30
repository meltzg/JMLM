import java.util.List;

import org.meltzg.jmlm.device.MTPContentDevice;
import org.meltzg.jmlm.device.MTPDeviceInfo;

public class Main {
    public static void main(String[] args) {
    	try {
    		List<MTPDeviceInfo> deviceInfos = MTPContentDevice.getDevicesInfo();
            System.out.println();
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
        
    }
}