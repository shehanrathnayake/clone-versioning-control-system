import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFileVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String path = file.toAbsolutePath().toString();
        Pattern pattern = Pattern.compile(Clone.mainRepoPath);
        Matcher matcher = pattern.matcher(path);

        if (!matcher.find()) {
            if (Clone.isDistroy) {
                Files.delete(file);
            } else {
                byte[] buffer = getBytes(file);
                FileDetails fileDetails = new FileDetails(path, buffer);
                Clone.contents.add(fileDetails);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    private byte[] getBytes(Path file) throws IOException {
        FileInputStream fis = new FileInputStream(file.toAbsolutePath().toString());
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] buffer = null;
        try{
            buffer = bis.readAllBytes();
        } finally {
            bis.close();
        }

        return buffer;
    }
}
