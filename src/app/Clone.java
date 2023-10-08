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
    public static final String RESET = "\033[0m";
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
/*
        targetFolderPath = "/home/shehan/Documents/dep-11/myfolder/clone-test2/";
        mainRepoPath = targetFolderPath + ".clone/";
        File folderBase = new File(mainRepoPath);

*/


        targetFolderPath = args[0];
        mainRepoPath = targetFolderPath + "/.clone/";
        File folderBase = new File(mainRepoPath);
        String command = "";

        if (args.length > 1) command = args[1];
        else {
            cloneIntroduction();
            return;
        }

        if (folderBase.exists()) {
            takeHashCodes();
        }
        Path targetFolder = Paths.get(targetFolderPath);

        switch (command) {
            case "-h":
            case "--help":
                cloneHelpCenter();
                break;

            case "-v":
            case "--version":
                System.out.println("\tclone version 0.8.0");
                break;

            case "start":
                if (!folderBase.exists()) {
                    start();
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

        /*
        while(true) {
            System.out.print("Command: ");
            String command = scanner.nextLine();

        }
*/

//        System.out.println();



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
        String filePath = mainRepoPath + "temp-clone/tempclone.txt";
        writeFileContent(filePath, contents);
    }

    private static void save() throws IOException, NoSuchAlgorithmException {
        getMadeContents();
        if (contents.size() == 0) {
            System.out.println("Need to make a clone before save. use " + RED_COLOR + "clone make" + RESET);
            return;
        }
        String hashCode = generateHashCode();
        SaveNode newSaveNode = new SaveNode(hashCode, contents);
        String filePath = mainRepoPath + "clones/" + hashCode + ".db";
        writeFileContent(filePath, newSaveNode);

        takeHashCodes();
        hashCodes.add(hashCode);
        logHashCodes();
        setHeadClone(hashCode);
        System.out.println("Clone " + YELLOW_COLOR + hashCodes.get(hashCodes.size() -1).substring(0,7) + RESET + " has been saved successfully.");
    }

    private static void getMadeContents()  {
        String filePath = mainRepoPath + "temp-clone/tempclone.txt";
        try{
            contents = (ArrayList<FileDetails>) readFileContent(filePath);
        } catch (EOFException e) {
            System.out.println("Need to make a clone before save. Execute " + RED_COLOR + "clone make" + RESET);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        String filePath = mainRepoPath + "clone-hash/clonehash.txt";
        try{
            hashCodes = (ArrayList<String>) readFileContent(filePath);
        } catch (EOFException e) {}
    }

    private static void logHashCodes() throws IOException {
        String filePath = mainRepoPath + "clone-hash/clonehash.txt";
        writeFileContent(filePath, hashCodes);
    }

    private static void setHeadClone(String headCloneCode) throws IOException {
        String filePath = mainRepoPath + "clone-hash/headhash.txt";
        writeFileContent(filePath, headCloneCode.getBytes());
        if (!headCloneCode.equals(hashCodes.get(hashCodes.size() -1))) {
            System.out.println("HEAD detached from the present. You can see the files, go through the past clones but cannot save changes.");
        }
    }

    private static String getHeadClone() throws IOException {
        String filePath = mainRepoPath + "clone-hash/headhash.txt";
        byte[]hashcodeBuffer = (byte[]) readFileContent(filePath);
        String headHashCode = new String(hashcodeBuffer);
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
        String filePath = mainRepoPath + "clones/" + hashCode + ".db";
        SaveNode cloneObject = (SaveNode) readFileContent(filePath);
        extractClone(cloneObject);
        setHeadClone(hashCode);
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

            String filePath = files.getPath();
            writeFileContent(filePath, files.getBuffer());
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

        String filePath = mainRepoPath + "uniqueclone.txt";
        writeFileContent(filePath,timeStamp.getBytes());
    }

    private static Object readFileContent(String filePath) throws IOException {
        File dbFile = new File(filePath);
        FileInputStream fis = new FileInputStream(dbFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object cloneObject;
        try {
            cloneObject = ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ois.close();
        }
        return cloneObject;
    }

    private static void writeFileContent(String filePath, Object contentList ) throws IOException {
        File tempFile = new File(filePath);
        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(contentList);
        } finally {
            oos.close();
        }
    }

    private static void cloneIntroduction() {
        System.out.println("  _   _      _ _\n" +
                " | | | | ___| | | ___\n" +
                " | |_| |/ _ \\ | |/ _ \\\n" +
                " |  _  |  __/ | | (_) |\n" +
                " |_| |_|\\___|_|_|\\___/\n\n" +
                "I am " + YELLOW_COLOR +"Clone" + RESET + ". A Version Control System for your projects.\n\n" +
                "\tExecute " + RED_COLOR + "clone [-h | --help]" + RESET + " To see the command list.\n");
    }

    private static void cloneHelpCenter() {
        System.out.println("\nAll the command list");
        System.out.println("\t" + RED_COLOR + "clone" + RESET + " - Welcome notice");
        System.out.println("\t" + RED_COLOR + "clone [-h | --help]" + RESET + " - To see the command list");
        System.out.println("\t" + RED_COLOR + "clone [-v | --version]" + RESET + " - To see the version");
        System.out.println();
        System.out.println("\t" + RED_COLOR + "clone start" + RESET + " - To start cloning");
        System.out.println("\t" + RED_COLOR + "clone make" + RESET + " - To make a new clone");
        System.out.println("\t" + RED_COLOR + "clone save" + RESET + " - To save the prepared clone permanently");
        System.out.println("\t" + RED_COLOR + "clone log" + RESET + " - To see the clone list");
        System.out.println("\t" + RED_COLOR + "clone activate <hashcode>" + RESET + " - To traverse along the saved clones");
        System.out.println();
    }
}
