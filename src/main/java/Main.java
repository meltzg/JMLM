import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try (var isfs = Files.newInputStream(Paths.get("/home/meltzg/Music/01 - Seasons (Waiting on You).flac"));
             var ismtp = Files.newInputStream(Paths.get("/home/meltzg/devices/ak100ii/Internal storage/Music/Future Islands/Future Islands - Singles (2014) [FLAC]/01 - Seasons (Waiting on You).flac"))) {
            System.out.println(DigestUtils.md2Hex(isfs));
            System.out.println(DigestUtils.md2Hex(ismtp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}