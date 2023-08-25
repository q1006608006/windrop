package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ivan
 * @since 2023/08/25 20:27
 */
@Service
@Slf4j
public class SecurityManager {

    @Autowired
    private UserService userService;

    @Autowired
    private ValidService validService;



}
