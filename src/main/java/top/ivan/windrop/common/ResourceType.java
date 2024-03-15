package top.ivan.windrop.common;

/**
 * @author Ivan
 * @since 2023/09/08 14:30
 */
public enum ResourceType {

    FILE("文件"), TEXT("文本"), IMAGE("图片"), UNKNOWN("未知");

    private final String name;

    ResourceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ResourceType judge(String resourceType) {
        switch (resourceType.toUpperCase()) {
            case "FILE":
                return FILE;
            case "TEXT":
                return TEXT;
            case "IMAGE":
                return IMAGE;
            default:
                return UNKNOWN;
        }
    }

}
