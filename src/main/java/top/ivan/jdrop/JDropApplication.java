package top.ivan.jdrop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Ivan
 * @since 2024/01/10 16:44
 */
@Slf4j
@SpringBootApplication
public class JDropApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JDropApplication.class);
        builder.headless(false).run(args);
    }
}
