package top.ivan.jdrop.user.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.JDropConfig;
import top.ivan.jdrop.common.cache.CacheNotAccessableException;
import top.ivan.jdrop.common.cache.LimitCache;
import top.ivan.jdrop.common.exception.IllegalClientRequestException;
import top.ivan.jdrop.common.util.NetUtils;
import top.ivan.jdrop.user.domain.model.ConnectProtocolEntity;
import top.ivan.jdrop.user.infrastructure.assembler.UserConnectAssembler;
import top.ivan.jdrop.user.domain.factory.ConnectProtocolFactory;
import top.ivan.jdrop.user.domain.factory.UserFactory;
import top.ivan.jdrop.user.domain.repo.UserRepository;
import top.ivan.jdrop.user.userinterface.model.dto.UserBindDTO;
import top.ivan.jdrop.user.userinterface.model.dto.UserConnectDTO;
import top.ivan.jdrop.user.userinterface.model.vo.UserBindVO;

/**
 * @author Ivan
 * @since 2024/03/12 10:36
 */
@Slf4j
@Service
public class UserAddService {

    private static final UserConnectAssembler USER_CONNECT_ASSEMBLER = new UserConnectAssembler();

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private ConnectProtocolFactory connectProtocolFactory;

    @Autowired
    private JDropConfig config;

    private final LimitCache<ConnectProtocolEntity> protocolCache = new LimitCache<>();

    public Mono<UserBindDTO> bindUser(UserBindVO bind) {
        return getProtocol(bind.getToken())
                .flatMap(p -> p.checkSign(bind) ? Mono.just(p) :
                        Mono.error(() -> new IllegalClientRequestException("核验失败，请检查网络安全")))
                .doOnNext(p -> userRepo.saveUser(userFactory.newUser(bind.getId(), bind.getName(), p)))
                .map(p -> new UserBindDTO(bind.getId(), p.getKeyElement()));
    }

    public Mono<UserConnectDTO> newUserConnect(int expireMillions) {
        return Mono.just(connectProtocolFactory.newConnectProtocol(expireMillions))
                .doOnNext(entity -> protocolCache.register(entity.toToken(), () -> entity, 1, expireMillions))
                .map(entity -> USER_CONNECT_ASSEMBLER.convertToUserConnect(entity,
                        NetUtils.getLocalHostName(),
                        config.getPort())
                );
    }

    public Mono<Void> removeConnectBind(UserConnectDTO uc) {
        if (null == uc) {
            return Mono.empty();
        }
        String token = ConnectProtocolEntity.toToken(uc.getKeyRoot(), uc.getTokenElement());
        return Mono.fromRunnable(() -> protocolCache.remove(token));
    }

    private Mono<ConnectProtocolEntity> getProtocol(String token) {
        return Mono.defer(() -> {
            try {
                return Mono.just(protocolCache.getData(token));
            } catch (CacheNotAccessableException e) {
                return Mono.error(e);
            }
        }).doOnError(e -> log.error("get protocol failed: {}", e.getMessage()));
    }

}
