package top.ivan.jardrop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Ivan
 * @since 2024/01/10 16:44
 */
@Slf4j
@SpringBootApplication
public class JarDrop {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JarDrop.class);
        builder.headless(false).run(args);
    }
}
