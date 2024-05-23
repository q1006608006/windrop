package top.ivan.jdrop.framework.util.cache;

import lombok.Data;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2024/03/19 17:15
 */
@Data
public class VolatileHandler<T> {
    private volatile T value;

    public VolatileHandler() {
    }

    public VolatileHandler(T value) {
        this.value = value;
    }

    public boolean hasValue() {
        return !Objects.isNull(value);
    }
}
