package top.ivan.jdrop.framework.constant;

/**
 * @author Ivan
 * @since 2024/03/29 00:29
 */
public enum MediaType {
    TEXT, URL, IMAGE, FILE;

    public boolean isTextType() {
        return this == TEXT || this == URL;
    }

    public boolean isStreamType() {
        return !isTextType();
    }
}
