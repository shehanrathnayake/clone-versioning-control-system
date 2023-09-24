package app;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

        if (args.length == 0) {
            targetFolderPath = "";
        } else {
            targetFolderPath = args[0];
        }
//        targetFolderPath = "/home/shehan/Documents/dep-11/myfolder/clone-test2/";
        mainRepoPath = targetFolderPath + ".clone/";
        File folderBase = new File(mainRepoPath);
        if (folderBase.exists()) {
            takeHashCodes();

        }

        while(true) {
            System.out.print("\nStart a repository => " + RED_COLOR + "clone start" + RESET + "\nMake files => " + RED_COLOR + "clone make" + RESET +"\nSave app.Clone => " + RED_COLOR + "clone save" + RESET + "\napp.Clone log => " + RED_COLOR + "clone log" + RESET + "\nActivate a clone => " + RED_COLOR + "clone activate " + YELLOW_COLOR + "hashcode" + RESET + "\n\nEnter the command: ");
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
                    if (folderBase.exists()) {
                        if (hashCodes.size() == 0) takeHashCodes();
                        if (hashCodes.size() == 0 || getHeadClone().equals(hashCodes.get(hashCodes.size() -1))) {
                            addToUniqueFile();
                            Files.walkFileTree(targetFolder, new MyFileVisitor());
                        }
                        else System.out.println("Not allowed");
                    }
                    else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                    break;

                case "clone save":
                    if (folderBase.exists()) {
                        if (hashCodes.size() == 0) takeHashCodes();
                        if (hashCodes.size() == 0 || getHeadClone().equals(hashCodes.get(hashCodes.size() -1))) {
                            save(targetFolder);
                            contents = new ArrayList<>();
                        } else System.out.println("Not in the present clone");
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
                    if (folderBase.exists()) {
                        Pattern pattern = Pattern.compile("^clone activate [A-Z0-9]{7}$");
                        Matcher matcher = pattern.matcher(command);
                        if (matcher.find()) selectClone(command.substring(command.length()-7));
                        else System.out.println("Not a command");

                    } else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
            }
        }
    }
    private static void start(Path targetFolder) throws IOException {
        String[] ignorePaths = {"", "clones", "clone-hash", ".ignoreclone"};
        for (String ignorePath : ignorePaths) {
            File fileRef = new File(mainRepoPath + ignorePath);
            fileRef.mkdir();
        }

        String[] repoFiles = {"clone-hash/clonehash.txt", "clone-hash/headhash.txt", "uniqueclone.txt"};
        for (String repoFile : repoFiles) {
            File fileHash = new File(Clone.mainRepoPath + repoFile);
            fileHash.createNewFile();
        }
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
                setHeadClone(hashCode);
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

    private static void setHeadClone(String headCloneCode) throws IOException {
        Path pathToHeadCloneCode = Paths.get(targetFolderPath + ".clone/clone-hash/headhash.txt");
        FileOutputStream fos = new FileOutputStream(pathToHeadCloneCode.toAbsolutePath().toString());
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        try {
            bos.write(headCloneCode.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            bos.close();
        }
    }

    private static String getHeadClone() throws IOException {
        String headHashCode = "";
        Path pathToHeadCloneCode = Paths.get(targetFolderPath + ".clone/clone-hash/headhash.txt");
        FileInputStream fis = new FileInputStream(pathToHeadCloneCode.toAbsolutePath().toString());
        BufferedInputStream bis = new BufferedInputStream(fis);
        try {
            byte[] buffer = bis.readAllBytes();
            headHashCode = new String(buffer);
        } finally {
            bis.close();
        }
        return headHashCode;
    }

    private static void showClones() throws IOException {
        System.out.println();
        String headHashCode = getHeadClone();
        boolean done = false;
        for (int i = hashCodes.size() -1; i >= 0; i--) {
            System.out.print(YELLOW_COLOR + hashCodes.get(i).substring(0,7) + RESET);
            if (i == hashCodes.size() -1 && hashCodes.get(i).equals(headHashCode)) {
                System.out.print(" " + RED_COLOR + "(HEAD -> main)" + RESET);
                done = true;
            }
            else if (!done) {
                if (i == hashCodes.size() -1 && !hashCodes.get(i).equals(headHashCode)) System.out.print(" " + RED_COLOR + "(main)" + RESET);
                else if (hashCodes.get(i).equals(headHashCode)) System.out.print(" " + RED_COLOR + "(HEAD)" + RESET);
            }
            System.out.println();
        }
    }

                        /* Activating clone */
    private static void selectClone(String hashCode) throws IOException {
        if (hashCodes.size() == 0) takeHashCodes();
        for (String code : hashCodes) {
            if (hashCode.equals(code.substring(0,7))) {
                destroyPresent(new File(targetFolderPath));
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
            setHeadClone(hashCode);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ois.close();
        }
    }

    private static void extractClone(SaveNode clone) throws IOException {
        for (FileDetails files : clone.getContents()) {

            String fileName = "/[.]?[A-Za-z0-9_[-] ]+[.][A-Za-z]+$";
            Pattern pattern = Pattern.compile(fileName);
            Matcher matcher = pattern.matcher(files.getPath());
            matcher.find();
            String directoryPath = files.getPath().substring(0, matcher.start());
            File directory = new File(directoryPath);
            if (!directory.exists()) directory.mkdir();

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

    private static void destroyPresent(File file) throws IOException {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (File contentFile : fileList) {
                if (!contentFile.getName().equals(".clone")) {
                    destroyPresent(contentFile);
                }
            }
        }
        file.delete();
    }

    private static void addToUniqueFile() throws IOException {
        Instant instant = Instant.now();
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        String timeStamp = dateTime.toString();
        File uniqueFile = new File(mainRepoPath + "uniqueclone.txt");
        FileOutputStream fos = new FileOutputStream(uniqueFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        try{
            bos.write(timeStamp.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            bos.close();
        }
    }
}
