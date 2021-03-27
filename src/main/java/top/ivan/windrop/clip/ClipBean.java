package top.ivan.windrop.clip;

import top.ivan.windrop.util.ConvertUtil;

import java.awt.datatransfer.Transferable;
import java.io.IOException;

/**
 * @author Ivan
 * @description
 * @date 2020/12/17
 */
public interface ClipBean {

    byte[] getBytes() throws IOException;

    long getUpdateTime();

    Transferable toTransferable() throws IOException;

    boolean isOrigin(Object target) throws IOException;

}