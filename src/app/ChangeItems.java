package app;

import java.io.Serializable;
import java.util.ArrayList;

public class ChangeItems implements Serializable {
    private ArrayList<FileMeta> newFilesList = null;
    private ArrayList<String> deleteFilesList = null;

    public ChangeItems() {
    }

    public ChangeItems(ArrayList<FileMeta> newFilesList, ArrayList<String> deleteFilesList) {
        this.newFilesList = newFilesList;
        this.deleteFilesList = deleteFilesList;
    }

    public ArrayList<FileMeta> getNewFilesList() {
        return newFilesList;
    }

    public void setNewFilesList(ArrayList<FileMeta> newFilesList) {
        this.newFilesList = newFilesList;
    }

    public ArrayList<String> getDeleteFilesList() {
        return deleteFilesList;
    }

    public void setDeleteFilesList(ArrayList<String> deleteFilesList) {
        this.deleteFilesList = deleteFilesList;
    }
}
