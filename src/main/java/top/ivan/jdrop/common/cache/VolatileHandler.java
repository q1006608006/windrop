package top.ivan.jdrop.common.cache;

import lombok.Data;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2024/03/19 17:15
 */
@Data
public class VolatileHandler<T> {
    private volatile T value;

    public boolean hasValue() {
        return !Objects.isNull(value);
    }
}
