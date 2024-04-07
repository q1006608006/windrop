package top.ivan.jdrop.user.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.user.domain.repo.UserRepository;

/**
 * @author Ivan
 * @since 2024/03/27 17:11
 */
@Slf4j
@Service
public class UserActionService {

    @Autowired
    private UserRepository userRepository;

    public Mono<Boolean> validSign(String id, String sign, String content) {
        return Mono.fromSupplier(() -> userRepository.findUser(id))
                .map(user -> user.validContent(content, sign));
    }

    public Mono<String> sign(String userId, String content) {
        return Mono.fromSupplier(() -> userRepository.findUser(userId))
                .map(user -> user.sign(content));
    }
}
