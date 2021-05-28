package top.ivan.windrop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.ScheduledService;
import top.ivan.windrop.verify.IPVerifier;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2021/2/5
 */
@Slf4j
@Configuration
@EnableScheduling
public class WinDropConfiguration {

    public static final String CONNECT_GROUP = "CONNECT";
    public static final String SWAP_GROUP = "SWAP";

    public static final String TEMP_FILE_PATH = "temp";
    public static final String UPLOAD_FILES_PATH = "recv";

    @Bean
    public HttpMessageConverter<BufferedImage> imageConverter(List<HttpMessageConverter<?>> converters) {
        HttpMessageConverter<BufferedImage> converter = new BufferedImageHttpMessageConverter();
        converters.add(converter);
        return converter;
    }

    @Bean
    public IPVerifier getIPVerifier() {
        return new IPVerifier("conf/ipList.txt");
    }

    @Bean
    public PersistUserService getPersistUserService() {
        return new PersistUserService("conf/u.data");
    }

    @Bean
    public ScheduledService getScheduledService() {
        return new ScheduledService(3);
    }
}
