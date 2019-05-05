package org.meltzg.jmlm;

import java.nio.file.Paths;

public class CommonUtil {
    public static final String RESOURCEDIR = Paths.get("src", "test", "resources").toAbsolutePath().toString();
    public static final String TMPDIR = RESOURCEDIR + "/temp";
}
