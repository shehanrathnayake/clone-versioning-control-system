package app;

import java.io.Serializable;

public class FileDetails implements Serializable {
    private String path;
    private byte[] buffer = null;

    public FileDetails() {
    }
    public FileDetails(String path, byte[] buffer) {
        this.path = path;
        this.buffer = buffer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
