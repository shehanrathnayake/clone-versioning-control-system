import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

public class CloneMain {
    public static String targetFolderPath;
    public static String mainRepoPath;
    public static ArrayList<FileDetails> contents = new ArrayList<>();
    public static ArrayList<String> hashCodes = new ArrayList<>();
    public static boolean initialized = false;
    public static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length == 0) {
            targetFolderPath = "";
        } else {
            targetFolderPath = args[0];
        }
        File folderBase = new File(targetFolderPath + ".clone");
        if (folderBase.exists()) takeHashCodes();
//        if (initialized) takeHashCodes();

        while(true) {
            System.out.print("\nTo start a repository => clone start\nMake files => clone make\nSave Clone => clone save\n\nEnter the command: ");
            String command = scanner.nextLine();
            Path targetFolder = Paths.get(targetFolderPath);

            switch (command) {
                case "clone start":
                    start(targetFolder);
                    takeHashCodes();
//                    initialized = true;
                    break;
                case "clone make":
                    Files.walkFileTree(targetFolder, new MyFileVisitor());
                    break;
                case "clone save":
                    save(targetFolder);
                    contents = new ArrayList<>();
                    break;
                default:
            }
        }
    }
    private static void start(Path targetFolder) throws IOException {
        String ignoreFilePath = targetFolder.toAbsolutePath().toString() + "/.clone/";
        String[] ignorePaths = {"", "clones", "clone-hash", ".ignoreclone"};
        for (String ignorePath : ignorePaths) {
            File fileRef = new File(ignoreFilePath + ignorePath);
            fileRef.mkdir();
        }

        File fileHash = new File(ignoreFilePath + "clone-hash/clonehash.txt");
        fileHash.createNewFile();

        CloneMain.mainRepoPath = ignoreFilePath;
    }

    private static void save(Path targetFolder) throws IOException, NoSuchAlgorithmException {
        boolean successfull = true;
        String hashCode = generateHashCode();
        SaveNode newSaveNode = new SaveNode(hashCode, contents);

        File file = new File(targetFolder.toAbsolutePath().toString()+"/.clone/clones/" + hashCode + ".db");
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(newSaveNode);
        } catch (IOException e) {
            successfull = false;
            throw new RuntimeException(e);
        } finally {
            oos.close();
            if (successfull) {
                hashCodes.add(hashCode);
                logHashCodes();
            }
        }
    }

    private static String generateHashCode() throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(contents);
        oos.flush();
        byte[] byteArray =  baos.toByteArray();

        return calculateHashCode(byteArray);
    }

    private static String calculateHashCode(byte[] byteArray) throws NoSuchAlgorithmException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashArray = digest.digest(byteArray);

        StringBuilder hexStringCode = new StringBuilder();
        for (byte b : hashArray) {
            String hex = String.format("%02X", b);
            hexStringCode.append(hex);
        }

        return hexStringCode.toString();
    }

    private static void takeHashCodes() throws IOException {
        Path pathToHashCodes = Paths.get(targetFolderPath + ".clone/clone-hash/clonehash.txt");
        FileInputStream fis = new FileInputStream(pathToHashCodes.toAbsolutePath().toString());
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
            hashCodes = (ArrayList<String>) ois.readObject();
            ois.close();
        } catch (NullPointerException e) {

        } catch (EOFException e) {

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void logHashCodes() throws IOException {
        Path pathToHashCodes = Paths.get(targetFolderPath + ".clone/clone-hash/clonehash.txt");
        FileOutputStream fos = new FileOutputStream(pathToHashCodes.toAbsolutePath().toString());
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(hashCodes);
        } finally {
            oos.close();
        }
    }
}
