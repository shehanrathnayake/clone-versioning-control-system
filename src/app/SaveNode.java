package app;

import java.io.Serializable;
import java.util.ArrayList;

public class SaveNode implements Serializable {
    private String hashCode;
    private ArrayList<FileDetails> contents;
    private String prevHashCode;

    public SaveNode() {
    }

    public SaveNode(String hashCode, ArrayList<FileDetails> contents) {
        this.hashCode = hashCode;
        this.contents = contents;
    }

    public SaveNode(String hashCode, ArrayList<FileDetails> contents, String prevHashCode) {
        this.hashCode = hashCode;
        this.contents = contents;
        this.prevHashCode = prevHashCode;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public ArrayList<FileDetails> getContents() {
        return contents;
    }

    public void setContents(ArrayList<FileDetails> contents) {
        this.contents = contents;
    }

    public String getPrevHashCode() {
        return prevHashCode;
    }

    public void setPrevHashCode(String prevHashCode) {
        this.prevHashCode = prevHashCode;
    }
}
