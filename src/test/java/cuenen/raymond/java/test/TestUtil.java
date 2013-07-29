package cuenen.raymond.java.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class voor het initialiseren van directory's
 * en bestanden om te gebruiken in testen.
 * 
 * @author R. Cuenen
 */
public final class TestUtil {

    public static File createDir(String name) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tmpDir, name);
        dir.mkdirs();
        while ((!dir.isDirectory() || !dir.exists()) && dir.getParentFile() != null) {
            dir = dir.getParentFile();
        }
        return dir;
    }

    public static File createFile(String resourceName) throws IOException {
        return createFile(null, resourceName);
    }

    public static File createFile(File dir, String resourceName) throws IOException {
        File tmpDir = dir == null ? new File(System.getProperty("java.io.tmpdir")) : dir;
        File file = new File(tmpDir, resourceName);
        file.deleteOnExit();
        byte[] buffer = new byte[1024];
        int read;
        InputStream input = TestUtil.class.getResourceAsStream("/" + resourceName);
        OutputStream output = new FileOutputStream(file);
        try {
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            output.flush();
            output.close();
            input.close();
        }
        return file;
    }

    private TestUtil() {
    }
}
