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
    public static ArrayList<String> fileHashCodes = new ArrayList<>();
//    public static ArrayList<FileDetails> fileContentList = new ArrayList<>();
//    public static ArrayList<FileMeta> fileMetaList = new ArrayList<>();
    private static ArrayList<CloneUnit> cloneList = new ArrayList<>();
    public static ArrayList<FileMeta> newFiles = new ArrayList<>();
    public static ArrayList<FileMeta> currentFileList = new ArrayList<>();
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
            takeClones();
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
                    if (cloneList.size() == 0) takeClones();
                    if (getHeadClone().equals(cloneList.get(cloneList.size() -1))) {
                        make(targetFolder);
                    }
                    else System.out.println("Cannot make clones while HEAD detached from main");
                }
                else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                break;

            case "status":
                if (folderBase.exists()) {

                }
                else System.out.println("Not a repository. Use \" + RED_COLOR + \"clone start\" + RESET + \" to start cloning");

            case "save":
                if (folderBase.exists()) {
                    if (cloneList.size() == 0) takeClones();
                    if (getHeadClone().equals(cloneList.get(cloneList.size() -1))) {
                        save();
                    } else System.out.println("Cannot save clones while HEAD detached from main");
                }
                else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                break;

            case "log":
                if (folderBase.exists()) {
                    if (cloneList.size() == 0) takeClones();
                    showClones();
                }
                else System.out.println("Not a repository. Use " + RED_COLOR + "clone start" + RESET + " to start cloning");
                break;

            case "show":


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
        String[] ignorePaths = {"", "clones", "content-hashcodes", "contents", "madedata", ".ignoreclone"};
        for (String ignorePath : ignorePaths) {
            File fileRef = new File(mainRepoPath + ignorePath);
            fileRef.mkdir();
        }

        String[] repoFiles = {"clones/cloneList.clone", "clones/tailcloneunit.clone", "clones/clonehascodelist.clone", "clones/filedata/", "clones/headhash.clone", "uniqueclone.clone", "madedata/currentfilelist.clone",
                                "content-hashcodes/contenthashcodes.clone"};
        for (String repoFile : repoFiles) {
            File fileHash = new File(mainRepoPath + repoFile);
            fileHash.createNewFile();
        }
    }

    private static void make(Path targetFolder) throws IOException {
        addToUniqueFile();
        Files.walkFileTree(targetFolder, new MyFileVisitor());
        saveFileList();
    }

    private static void saveFileList() throws IOException {
        String filePath = mainRepoPath + "madedata/currentfilelist.clone";
        writeFileContent(filePath, currentFileList);
    }

    private static void save() throws IOException, NoSuchAlgorithmException {
        getCurrentFileList();
        if (currentFileList.size() == 0) {
            System.out.println("Need to make a clone before save. use " + RED_COLOR + "clone make" + RESET);
            return;
        }
        if(!findChanges()) {
            System.out.println(("Nothing has been changed from the previous clone."));
            return;
        }

        saveNewFiles();
        String hashCode = generateHashCode();

        CloneUnit newCloneUnit = new CloneUnit(currentFileList, hashCode);
        cloneList.add(newCloneUnit);

        String cloneListFilePath = mainRepoPath + "clones/cloneList.clone";
        writeFileContent(cloneListFilePath, cloneList);

        setHeadClone(hashCode);
        System.out.println("Clone " + YELLOW_COLOR + hashCode.substring(0,7) + RESET + " has been saved successfully.");
    }

    private static void saveNewFiles() {
        String filePath = mainRepoPath + "content-hashcodes/contenthashcodes.clone";
        String folderPathOfContent = mainRepoPath + "clones/filedata/";
        try {
            fileHashCodes = (ArrayList<String>) readFileContent(filePath);
            for (FileMeta currentFile : currentFileList) {
                if (!fileHashCodes.contains(currentFile.getHashcode())) {
                    byte[] buffer = (byte[]) readFileContent(currentFile.getFilePath());
                    writeFileContent(folderPathOfContent + currentFile.getHashcode() + ".clone", buffer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean findChanges() {
        ArrayList<FileMeta> lastCloneFileList = cloneList.get(cloneList.size() -1).getFileList();
        if (lastCloneFileList.size() != currentFileList.size()) return true;
        for (int i = 0; i < lastCloneFileList.size(); i++) {
            if (!(lastCloneFileList.get(i).getFilePath().equals(currentFileList.get(i).getFilePath()) && lastCloneFileList.get(i).getFilePath().equals(currentFileList.get(i).getFilePath()))) {
                return true;
            }
        }
        return false;
    }

    private static void getCurrentFileList()  {
        String filePath = mainRepoPath + "madedata/currentfilelist.clone";
        try{
            currentFileList = (ArrayList<FileMeta>) readFileContent(filePath);
        } catch (EOFException e) {
            System.out.println("Need to make a clone before save. Execute " + RED_COLOR + "clone make" + RESET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateHashCode() throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(currentFileList);
        oos.flush();
        byte[] byteArray =  baos.toByteArray();
        return calculateHashCode(byteArray);
    }

    static String calculateHashCode(byte[] byteArray) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashArray = digest.digest(byteArray);

        StringBuilder hexStringCode = new StringBuilder();
        for (byte b : hashArray) {
            String hex = String.format("%02X", b);
            hexStringCode.append(hex);
        }
        return hexStringCode.toString();
    }

    private static void takeClones() throws IOException {
        String filePath = mainRepoPath + "clones/cloneList.clone";
        try{
            cloneList = (ArrayList<CloneUnit>) readFileContent(filePath);
        } catch (EOFException e) {}
    }

//    private static void logHashCodes() throws IOException {
//        String filePath = mainRepoPath + "clone-hash/clonehash.clone";
//        writeFileContent(filePath, hashCodes);
//    }

    private static void setHeadClone(String headCloneCode) throws IOException {
        String filePath = mainRepoPath + "clone-hash/headhash.clone";
        writeFileContent(filePath, headCloneCode.getBytes());
        if (!headCloneCode.equals(cloneList.get(cloneList.size() -1).getCloneHashcode())) {
            System.out.println("HEAD detached from the present. You can see the files, go through the past clones but cannot save changes.");
        }
    }

    private static String getHeadClone() throws IOException {
        String filePath = mainRepoPath + "clone-hash/headhash.clone";
        byte[]hashcodeBuffer = (byte[]) readFileContent(filePath);
        String headHashCode = new String(hashcodeBuffer);
        return headHashCode;
    }

    private static void showClones() throws IOException {
        String headHashCode = getHeadClone();
        boolean done = false;
        for (int i = cloneList.size() -1; i >= 0; i--) {
            System.out.print(YELLOW_COLOR + cloneList.get(i).getCloneHashcode().substring(0,7) + RESET);
            if (i == cloneList.size() -1 && cloneList.get(i).getCloneHashcode().equals(headHashCode)) {
                System.out.print(" " + RED_COLOR + "(HEAD -> main)" + RESET);
                done = true;
            }
            else if (!done) {
                if (i == cloneList.size() -1 && !cloneList.get(i).getCloneHashcode().equals(headHashCode)) System.out.print(" " + RED_COLOR + "(main)" + RESET);
                else if (cloneList.get(i).getCloneHashcode().equals(headHashCode)) System.out.print(" " + RED_COLOR + "(HEAD)" + RESET);
            }
            System.out.println();
        }
    }

    /* Activating clone */
    private static void selectClone(String hashCode) throws IOException {
        if (cloneList.size() == 0) takeClones();

        for (CloneUnit cloneUnit : cloneList) {
            if (hashCode.equals(cloneUnit.getCloneHashcode().substring(0,7))) {
                destroyPresent(new File(targetFolderPath));
                activateClone(cloneUnit);
                return;
            }
        }
        System.out.println("Wrong code");
    }

    private static void activateClone(CloneUnit clone) throws IOException {
//        String filePath = mainRepoPath + "clones/" + hashCode + ".clone";
//        CloneUnit cloneObject = (CloneUnit) readFileContent(filePath);
//        extractClone(cloneObject);

        String folderPathOfContent = mainRepoPath + "clones/filedata/";

        for (FileMeta fileMeta : clone.getFileList()) {

            String fileName = "/[.]?[A-Za-z0-9_[-] ]+[.][A-Za-z]+$";
            Pattern pattern = Pattern.compile(fileName);
            Matcher matcher = pattern.matcher(fileMeta.getFilePath());
            matcher.find();
            String directoryPath = fileMeta.getFilePath().substring(0, matcher.start());
            File directory = new File(directoryPath);
            if (!directory.exists()) directory.mkdir();

            byte[] contentBuffer = (byte[]) readFileContent(folderPathOfContent + fileMeta.getHashcode() + ".clone");
            writeFileContent(fileMeta.getFilePath(),contentBuffer);
        }


        setHeadClone(clone.getCloneHashcode());
    }

//    private static void extractClone(CloneUnit clone) throws IOException {
//        for (FileMeta files : clone.getFileList()) {
//            String fileName = "/[.]?[A-Za-z0-9_[-] ]+[.][A-Za-z]+$";
//            Pattern pattern = Pattern.compile(fileName);
//            Matcher matcher = pattern.matcher(files.getHashcode());
//            matcher.find();
//            String directoryPath = files.getHashcode().substring(0, matcher.start());
//            File directory = new File(directoryPath);
//            if (!directory.exists()) directory.mkdir();
//
//            String filePath = files.getHashcode();
//            writeFileContent(filePath, files.getBuffer());
//        }
//    }

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

        String filePath = mainRepoPath + "uniqueclone.clone";
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
        System.out.println("\n\tAll the command list\n");
        System.out.println("\t" + RED_COLOR + "clone" + RESET + " - Welcome notice");
        System.out.println("\t" + RED_COLOR + "clone [-h | --help]" + RESET + " - To see the command list");
        System.out.println("\t" + RED_COLOR + "clone [-v | --version]" + RESET + " - To see the version");
        System.out.println();
        System.out.println("\t" + RED_COLOR + "clone start" + RESET + " - To initialize a cloning factory");
        System.out.println("\t" + RED_COLOR + "clone make" + RESET + " - To generate a new clone");
        System.out.println("\t" + RED_COLOR + "clone save" + RESET + " - To save the prepared clone permanently");
        System.out.println("\t" + RED_COLOR + "clone log" + RESET + " - To see the clone list");
        System.out.println("\t" + RED_COLOR + "clone activate <hashcode>" + RESET + " - To traverse along the saved clones");
        System.out.println();
    }
}
