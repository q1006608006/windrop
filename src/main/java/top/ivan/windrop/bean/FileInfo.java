package top.ivan.windrop.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@AllArgsConstructor
@Data
public class FileInfo {
    private File file;
    private long stateStamp;
    private long modifiedStamp;
}