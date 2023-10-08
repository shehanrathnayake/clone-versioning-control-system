package app;

import java.io.Serializable;
import java.util.ArrayList;

public class CloneUnit implements Serializable {
    private ArrayList<FileMeta> fileList;
    private String cloneHashcode;

    public CloneUnit() {
    }

    public CloneUnit(ArrayList<FileMeta> fileList, String cloneHashcode) {
        this.fileList = fileList;
        this.cloneHashcode = cloneHashcode;
    }

    public ArrayList<FileMeta> getFileList() {
        return fileList;
    }

    public void setFileList(ArrayList<FileMeta> fileList) {
        this.fileList = fileList;
    }

    public String getCloneHashcode() {
        return cloneHashcode;
    }

    public void setCloneHashcode(String cloneHashcode) {
        this.cloneHashcode = cloneHashcode;
    }
}
