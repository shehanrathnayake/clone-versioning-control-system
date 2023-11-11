package app;

import java.io.Serializable;

public class FileDetails implements Serializable {
    private String hashcode;
    private byte[] buffer = null;

    public FileDetails() {
    }
    public FileDetails(String hashcode, byte[] buffer) {
        this.hashcode = hashcode;
        this.buffer = buffer;
    }

    public String getHashcode() {
        return hashcode;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
