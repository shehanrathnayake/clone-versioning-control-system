import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
@Data
@NoArgsConstructor
public class SaveNode implements Serializable {
    String hashCode;
    ArrayList<FileDetails> contents;
    String prevHashCode;
    SaveNode previous;

    public SaveNode(String hashCode, ArrayList<FileDetails> contents) {
        this.hashCode = hashCode;
        this.contents = contents;
    }

    public SaveNode(String hashCode, ArrayList<FileDetails> contents, SaveNode previous) {
        this.hashCode = hashCode;
        this.contents = contents;
        this.previous = previous;
    }
}
