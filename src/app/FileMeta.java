package app;

import java.io.Serializable;

public class FileMeta implements Serializable {

    private String filePath;
    private String hashcode;

    public FileMeta() {
    }

    public FileMeta(String filePath, String hashcode) {
        this.filePath = filePath;
        this.hashcode = hashcode;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getHashcode() {
        return hashcode;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }
}
