package top.ivan.windrop.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Ivan
 * @since 2021/07/19 13:58
 */
@Data
@AllArgsConstructor
public class Pair<K,V> {
    private K key;
    private V value;
}
