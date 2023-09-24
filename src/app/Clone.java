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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Clone {
    public static String targetFolderPath;
    public static String mainRepoPath;
    public static ArrayList<FileDetails> contents = new ArrayList<>();
    public static ArrayList<String> hashCodes = new ArrayList<>();
    public static final String YELLOW_COLOR = "\033[33;1m";
    public static final String RED_COLOR = "\033[31;1m";
    public static final String RESET = "\033[0m";
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        targetFolderPath = args[0];
        mainRepoPath = targetFolderPath + "/.clone/";
        File folderBase = new File(mainRepoPath);
        String command = "";
        if (args.length > 1) command = args[1];
        else {
            System.out.println("Hi, I am there. Use commands to work with me...");
            return;
        }

        if (folderBase.exists()) {
            takeHashCodes();
        }
        Path targetFolder = Paths.get(targetFolderPath);

        switch (command) {
            case "start":
                if (!folderBase.exists()) {
                    start();
                    takeHashCodes();
                    System.out.println("New repository is created...");

                }else System.out.println("Already a repository");
                break;

            case "make":
                if (folderBase.exists()) {
                    if (hashCodes.size() == 0) takeHashCodes();
                    if (hashCodes.size() == 0 || getHeadClone().equals(hashCodes.get(hashCodes.size() -1))) {
                        make(targetFolder);
                    }
                    else System.out.println("Cannot make clones while HEAD detached from main");
                }
                else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                break;

            case "save":
                if (folderBase.exists()) {
                    if (hashCodes.size() == 0) takeHashCodes();
                    if (hashCodes.size() == 0 || getHeadClone().equals(hashCodes.get(hashCodes.size() -1))) {
                        save();
                        contents = new ArrayList<>();
                    } else System.out.println("Cannot save clones while HEAD detached from main");
                }
                else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                break;

            case "log":
                if (folderBase.exists()) {
                    if (hashCodes.size() == 0) takeHashCodes();
                    showClones();
                }
                else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                break;

            case "activate":
                if (folderBase.exists()) {
                    if (args.length == 3) {
                        selectClone(args[2]);
                    } else System.out.println("Clone hashcode should be provided...");

                } else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                break;

            default:
                System.out.println("Wrong command");
        }
        System.out.println();

    }
    private static void start() throws IOException {
        String[] ignorePaths = {"", "clones", "temp-clone", "clone-hash", ".ignoreclone"};
        for (String ignorePath : ignorePaths) {
            File fileRef = new File(mainRepoPath + ignorePath);
            fileRef.mkdir();
        }

        String[] repoFiles = {"clone-hash/clonehash.txt", "clone-hash/headhash.txt", "uniqueclone.txt", "temp-clone/tempclone.txt"};
        for (String repoFile : repoFiles) {
            File fileHash = new File(mainRepoPath + repoFile);
            fileHash.createNewFile();
        }
    }

    private static void make(Path targetFolder) throws IOException {
        addToUniqueFile();
        Files.walkFileTree(targetFolder, new MyFileVisitor());
        createContentFile();
    }

    private static void createContentFile() throws IOException {
        File tempFile = new File(mainRepoPath + "temp-clone/tempclone.txt");
        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(contents);
        } finally {
            oos.close();
        }
    }

    private static void save() throws IOException, NoSuchAlgorithmException {
        boolean successfull = true;
        getMadeContents();
        if (contents.size() == 0) {
            System.out.println("Need to make a clone before save. use " + RED_COLOR + "clone make" + RESET);
            return;
        }
        String hashCode = generateHashCode();
        SaveNode newSaveNode = new SaveNode(hashCode, contents);

        File file = new File(mainRepoPath + "clones/" + hashCode + ".db");
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
                takeHashCodes();
                hashCodes.add(hashCode);
                logHashCodes();
                setHeadClone(hashCode);
                System.out.println("Clone " + YELLOW_COLOR + hashCodes.get(hashCodes.size() -1).substring(0,7) + RESET + " has been saved successfully.");
            }
        }
    }

    private static void getMadeContents() throws IOException {
        File tempFile = new File(mainRepoPath + "temp-clone/tempclone.txt");
        FileInputStream fis = new FileInputStream(tempFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);

        try {
            contents = (ArrayList<FileDetails>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ois.close();
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
        Path pathToHashCodes = Paths.get(mainRepoPath + "clone-hash/clonehash.txt");
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
        Path pathToHashCodes = Paths.get(mainRepoPath + "clone-hash/clonehash.txt");
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
        Path pathToHeadCloneCode = Paths.get(mainRepoPath + "clone-hash/headhash.txt");
        FileOutputStream fos = new FileOutputStream(pathToHeadCloneCode.toAbsolutePath().toString());
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        try {
            bos.write(headCloneCode.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            bos.close();
            if (!headCloneCode.equals(hashCodes.get(hashCodes.size() -1))) {
                System.out.println("HEAD detached from the present. You can see the files, go through the past clones but cannot save changes.");
            }
        }
    }

    private static String getHeadClone() throws IOException {
        String headHashCode = "";
        Path pathToHeadCloneCode = Paths.get(mainRepoPath + "clone-hash/headhash.txt");
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
