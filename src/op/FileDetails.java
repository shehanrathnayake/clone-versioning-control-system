package op;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class FileDetails implements Serializable {
    String path;
    byte[] buffer = null;


    public FileDetails(String path, byte[] buffer) {
        this.path = path;
        this.buffer = buffer;
    }
}
