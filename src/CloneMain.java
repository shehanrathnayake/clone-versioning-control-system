import op.MyFileVisitor;
import op.FileDetails;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class CloneMain {
    public static String targetFolderPath;
    public static String mainRepoPath;
    public static ArrayList<FileDetails> contents = new ArrayList<>();
    public static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            targetFolderPath = "";
        } else {
            targetFolderPath = args[0];
        }

        while(true) {
            System.out.print("\nTo start a repository => clone start\nMake files => clone make\nSave Clone => clone save\n\nEnter the command: ");
            String command = scanner.nextLine();
            Path targetFolder = Paths.get(targetFolderPath);
            System.out.println(targetFolder.toAbsolutePath().toString());

            switch (command) {
                case "clone start":
                    start(targetFolder);
                    break;
                case "clone make":
                    // emptyArrayList();
                    Files.walkFileTree(targetFolder, new MyFileVisitor());
                    break;
                case "clone save":
                    save(targetFolder);
                    contents = new ArrayList<>();
                    System.out.println(contents.size());
                    break;
                default:
            }
        }
    }



    private static void start(Path targetFolder) throws IOException {
        String ignoreFilePath = targetFolder.toAbsolutePath().toString() + "/.clone/";
        String[] ignorePaths = {"", "clones", "clone-hash", "clone-hash/clonehash.txt", ".ignoreclone"};
        for (String ignorePath : ignorePaths) {
            File fileRef = new File(ignoreFilePath + ignorePath);
            fileRef.mkdir();
        }

        File fileHash = new File(ignoreFilePath + "clone-hash/clonehash.txt");
        fileHash.createNewFile();

        CloneMain.mainRepoPath = ignoreFilePath;
    }

    private static void save(Path targetFolder) {
    }
}
