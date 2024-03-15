package top.ivan.jardrop.common.challenge;

import org.springframework.lang.NonNull;
import top.ivan.windrop.util.QueuedConcurrentMap;

import java.util.function.Predicate;

/**
 * @author Ivan
 * @description
 * @date 2021/3/24
 */
public class ChallengeTaskManager<T> {

    private final QueuedConcurrentMap<String, ChallengeTask<T>> keyMap;

    public ChallengeTaskManager(int maxSize) {
        keyMap = new QueuedConcurrentMap<>(maxSize, (k, task) -> task.isTimeout());
    }

    public ChallengeTask<T> registerTask(String key, long expireMillion, @NonNull Predicate<T> test) {
        ChallengeTask<T> task = new ChallengeTask<>(expireMillion, test);
        keyMap.put(key, task);
        return task;
    }

    public ChallengeTask<T> registerIfAbsent(String key, long intervalMillions, @NonNull Predicate<T> test) {
        ChallengeTask<T> task = new ChallengeTask<>(intervalMillions, test);
        keyMap.computeIfAbsent(key, k -> new ChallengeTask<>(intervalMillions, test));
        return task;
    }

    public ChallengeTask.State challenge(String key, T data) {
        ChallengeTask<T> task = keyMap.get(key);
        if (null == task) {
            return ChallengeTask.State.NONE;
        }
        return task.challenge(data);
    }

    public ChallengeTask<T> remove(String key) {
        ChallengeTask<T> task = keyMap.remove(key);
        task.clean();
        return task;
    }

    public boolean remove(String key, ChallengeTask<T> task) {
        if (keyMap.remove(key, task)) {
            task.clean();
            return true;
        }
        return false;
    }

}
