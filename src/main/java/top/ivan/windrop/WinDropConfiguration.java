package top.ivan.windrop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.util.StringUtils;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.svc.IPVerifier;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.ScheduledService;
import top.ivan.windrop.util.IDUtil;

import java.awt.image.BufferedImage;
import java.net.URI;
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

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .pathMatchers("/windrop/auth/connect_code")
                .authenticated()
                .pathMatchers("/**")
                .permitAll()
                .and()
                .csrf()
                .disable()
                .httpBasic()
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler("/login?logout"))
                .and()
                .build();
    }

    public ServerLogoutSuccessHandler logoutSuccessHandler(String uri) {
        RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
        successHandler.setLogoutSuccessUrl(URI.create(uri));
        return successHandler;
    }

    @Bean
    public HttpMessageConverter<BufferedImage> imageConverter(List<HttpMessageConverter<?>> converters) {
        HttpMessageConverter<BufferedImage> converter = new BufferedImageHttpMessageConverter();
        converters.add(converter);
        return converter;
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(WindropConfig config, @Value("${spring.security.user.name:}") String username, @Value("${spring.security.user.password:}") String password) {
        if (StringUtils.isEmpty(username)) {
            username = "admin";
            config.setUsername(username);
            log.info("not found login user, using default user: '{}'", username);
        }
        if (StringUtils.isEmpty(password)) {
            password = IDUtil.get32UUID().substring(0, 16);
            config.setPassword(password);
            log.info("not set password for login, using random password: '{}', please take it careful", password);
        }
        //内存中缓存权限数据
        User.UserBuilder userBuilder = User.builder();
        UserDetails admin = userBuilder.username(username).password("{noop}" + password).roles("ADMIN").build();

        // 输出加密密码
        return new MapReactiveUserDetailsService(admin);
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
