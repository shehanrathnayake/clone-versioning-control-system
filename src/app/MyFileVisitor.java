package app;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFileVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String path = file.toAbsolutePath().toString();
        Pattern pattern = Pattern.compile(Clone.mainRepoPath);
        Matcher matcher = pattern.matcher(path);

        if (file.toAbsolutePath().toString().equals(Clone.mainRepoPath + "uniqueclone.txt") || !matcher.find()) {
            byte[] buffer = getBytes(file);
            try {
                String hashcode = Clone.calculateHashCode(buffer);
                FileMeta fileMeta = new FileMeta(path, hashcode);
                Clone.currentFileList.add(fileMeta);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    public static byte[] getBytes(Path file) throws IOException {
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

    public static void saveBytes(Path file, byte[] buffer) throws IOException {
        FileOutputStream fos = new FileOutputStream(file.toAbsolutePath().toString());
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        try{
            bos.write(buffer);
        } finally {
            bos.close();
        }
    }
}
