import org.meltzg.jmlm.device.AbstractContentDevice;
import org.meltzg.jmlm.device.FSAudioContentDevice;

public class Main {
    public static void main(String[] args) {
        AbstractContentDevice device = new FSAudioContentDevice();
        device.addLibraryRoot("D:\\Users\\vader\\Music");
        System.out.println();
    }
}