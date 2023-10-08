package app;

import java.io.Serializable;
import java.util.ArrayList;

public class MadeData implements Serializable {
    private ArrayList<FileMeta> newFileMetaList;
    private ArrayList<FileDetails> newMadeDetail;
    private ArrayList<String> newHashcodes;

    public MadeData() {
    }

    public MadeData(ArrayList<FileMeta> newFileMetaList, ArrayList<FileDetails> newMadeDetail, ArrayList<String> newHashcodes) {
        this.newFileMetaList = newFileMetaList;
        this.newMadeDetail = newMadeDetail;
        this.newHashcodes = newHashcodes;
    }

    public ArrayList<FileMeta> getNewFileMetaList() {
        return newFileMetaList;
    }

    public void setNewFileMetaList(ArrayList<FileMeta> newFileMetaList) {
        this.newFileMetaList = newFileMetaList;
    }

    public ArrayList<FileDetails> getNewMadeDetail() {
        return newMadeDetail;
    }

    public void setNewMadeDetail(ArrayList<FileDetails> newMadeDetail) {
        this.newMadeDetail = newMadeDetail;
    }

    public ArrayList<String> getNewHashcodes() {
        return newHashcodes;
    }

    public void setNewHashcodes(ArrayList<String> newHashcodes) {
        this.newHashcodes = newHashcodes;
    }
}
