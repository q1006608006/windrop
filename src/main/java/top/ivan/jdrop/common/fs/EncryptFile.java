package top.ivan.jdrop.common.fs;

import top.ivan.jdrop.common.crypto.CryptoUtil;

import java.io.IOException;

public class EncryptFile extends WatchedFile {
    private final String password;


    public EncryptFile(String dataFile, String password) {
        super(dataFile);
        this.password = password;
    }

    @Override
    public byte[] load() throws IOException {
        byte[] data = super.load();
        if(data == null || data.length == 0) {
            return data;
        }
        return CryptoUtil.decrypt(data, password);
    }

    @Override
    public void write(byte[] data) throws IOException {
        byte[] output = CryptoUtil.encrypt(data, password);
        super.write(output);
    }
}
