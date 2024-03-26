import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Adler32;

public class FileSizeTest {
    public static void main(String[] args) throws IOException {
        //Path filePath = Path.of("", "messagemanager-bootstrap/scripts/readme.txt");
        Path filePath = Path.of("", "messagemanager-bootstrap/build/root/target/config/app/config.xml");
        long size =  Files.size(filePath);
        System.out.println(size);
        //IMPORTANT --> any file uploaded via FTP must use binary mode otherwise file size changes for text files!

        // even then the checksum of XMl is corrupted, not sure if this is Filezilla or FTP server issue

        long checkSUm = getChecksum(filePath);
        System.out.println(Long.toHexString(checkSUm));
    }

    public static long getChecksum(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            Adler32 checksum = new Adler32();
            byte[] buf = new byte[1024 * 8];

            int read;
            while ((read = input.read(buf, 0, buf.length)) > -1)
                checksum.update(buf, 0, read);

            return checksum.getValue();
        }
    }
}


