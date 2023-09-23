import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Clone {
    public static String targetFolderPath;
    public static String mainRepoPath;
    public static ArrayList<FileDetails> contents = new ArrayList<>();
    public static ArrayList<String> hashCodes = new ArrayList<>();
    public static final String YELLOW_COLOR = "\033[33;1m";
    public static final String RED_COLOR = "\033[31;1m";
    public static final String BLUE_COLOR = "\033[34;1m";
    public static final String RESET = "\033[0m";
    public static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

//        if (args.length == 0) {
//            targetFolderPath = "";
//        } else {
//            targetFolderPath = args[0];
//        }
        targetFolderPath = "/home/shehan/Documents/dep-11/myfolder/clone-test2/";
        mainRepoPath = targetFolderPath + ".clone/";
        File folderBase = new File(mainRepoPath);
        if (folderBase.exists()) {
            takeHashCodes();

        }

        while(true) {
            System.out.print("\nStart a repository => " + RED_COLOR + "clone start" + RESET + "\nMake files => " + RED_COLOR + "clone make" + RESET +"\nSave Clone => " + RED_COLOR + "clone save" + RESET + "\nClone log => " + RED_COLOR + "clone log" + RESET + "\nActivate a clone => " + RED_COLOR + "clone activate " + BLUE_COLOR + "hashcode" + RESET + "\n\nEnter the command: ");
            String command = scanner.nextLine();
            Path targetFolder = Paths.get(targetFolderPath);

            switch (command) {
                case "clone start":
                    if (!folderBase.exists()) {
                        start(targetFolder);
                        takeHashCodes();
                        System.out.println("New repository is created...");

                    }else System.out.println("Already a repository");
                    break;

                case "clone make":
                    if (folderBase.exists()) Files.walkFileTree(targetFolder, new MyFileVisitor());
                    else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                    break;

                case "clone save":
                    if (folderBase.exists()) {
                        save(targetFolder);
                        contents = new ArrayList<>();
                    }
                    else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                    break;

                case "clone log":
                    if (folderBase.exists()) {
                        if (hashCodes.size() == 0) takeHashCodes();
                        showClones();
                    }
                    else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                    break;

                default:
                    Pattern pattern = Pattern.compile("^clone activate [A-Z0-9]{7}$");
                    Matcher matcher = pattern.matcher(command);
                    if (matcher.find()) selectClone(command.substring(command.length()-7));
                    else System.out.println("Not a command");
            }
        }
    }
    private static void start(Path targetFolder) throws IOException {
        String ignoreFilePath = targetFolder.toAbsolutePath().toString() + "/.clone/";
        String[] ignorePaths = {"", "clones", "clone-hash", ".ignoreclone"};
        for (String ignorePath : ignorePaths) {
            File fileRef = new File(mainRepoPath + ignorePath);
            fileRef.mkdir();
        }

        File fileHash = new File(ignoreFilePath + "clone-hash/clonehash.txt");
        fileHash.createNewFile();

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

    private static void showClones() {
        System.out.println();
        for (int i = hashCodes.size() -1; i >= 0; i--) {
            System.out.print(YELLOW_COLOR + hashCodes.get(i).substring(0,7) + RESET);
            if (i == hashCodes.size() -1) System.out.print(" " + RED_COLOR + "(HEAD -> main)" + RESET);

            System.out.println();
        }
    }

                        /* Activating clone */
    private static void selectClone(String hashCode) throws IOException {
        if (hashCodes.size() == 0) takeHashCodes();
        System.out.println("Entered to method");
        System.out.println(hashCode);
        for (String code : hashCodes) {
            System.out.println(code.substring(0,7));
            if (hashCode.equals(code.substring(0,7))) {
//                System.out.println(hashCode);
//                System.out.println(code.substring(0,7));
                activateClone(code);
                return;
            }
        }
        System.out.println("Wrong code");
    }

    private static void activateClone(String hashCode) throws IOException {
        File dbFile = new File(mainRepoPath + "clones/" + hashCode + ".db");
        FileInputStream fis = new FileInputStream(dbFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            SaveNode cloneObject = (SaveNode) ois.readObject();
            extractClone(cloneObject);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ois.close();
        }
    }

    private static void extractClone(SaveNode clone) throws IOException {
        for (FileDetails files : clone.getContents()) {
            File cloneFile = new File(files.getPath());
            FileOutputStream fos = new FileOutputStream(cloneFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                bos.write(files.getBuffer());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                bos.close();
            }
        }
    }
}
