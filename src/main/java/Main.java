import org.meltzg.jmlm.device.MTPContentDevice;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            List<MTPContentDevice.MTPDeviceInfo> deviceInfos = MTPContentDevice.getDevicesInfo();
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}