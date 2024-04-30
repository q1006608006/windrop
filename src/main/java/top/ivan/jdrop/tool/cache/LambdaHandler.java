package top.ivan.jdrop.tool.cache;

import lombok.Data;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2024/03/19 17:15
 */
@Data
public class LambdaHandler<T> {
    private T value;

    public boolean hasValue() {
        return Objects.isNull(value);
    }
}
