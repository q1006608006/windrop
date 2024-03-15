package top.ivan.windrop.shortcut;

import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import top.ivan.windrop.bean.ShortcutApi;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.util.JSONUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;

/**
 * @author Ivan
 * @since 2021/09/06 10:14
 */
@Slf4j
@Service
public class ShortcutApiManager {
    private Supplier<ShortcutApi> apiSupplier;
    private final String apiUrl;

    public ShortcutApiManager(WindropConfig config) {
        this.apiUrl = config.getShortcutApi();
    }

    public String getShare() {
        return getApi().getShare();
    }

    public String getSync() {
        return getApi().getSync();
    }

    public String getScan() {
        return getApi().getScan();
    }

    public String getUpload() {
        return getApi().getUpload();
    }

    private ShortcutApi getApi() {
        if (null == apiSupplier) {
            init();
        }
        return apiSupplier.get();
    }

    private void init() {
        if (StringUtils.hasLength(apiUrl) && apiUrl.startsWith("http")) {
            apiSupplier = httpSuppler();
        } else {
            apiSupplier = fileSuppler();
        }
    }

    private Supplier<ShortcutApi> httpSuppler() {
        HttpClient client = HttpClient.create()
                //just access the website on browser if you have a proxy
                //.proxy(your proxy)
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .secure(SslProvider.defaultClientProvider());
        String json = client.get().uri(apiUrl).responseContent().asString().collectList().map(l -> String.join("\n", l)).block();
        try {
            ShortcutApi api = JSONUtils.read(json, ShortcutApi.class);
            return () -> api;
        } catch (Exception e) {
            log.error("can not load json", e);
        }
        throw new IllegalStateException("can not load data from: " + apiUrl);
    }

    private Supplier<ShortcutApi> fileSuppler() {
        String url = StringUtils.hasLength(apiUrl) ? apiUrl : "shortcut.json";
        ClassPathResource res = new ClassPathResource(url);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.getInputStream()))) {
            log.debug("load api from: {}", res.getURL());
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            line = builder.toString();
            ShortcutApi api = JSONUtils.read(line, ShortcutApi.class);
            return () -> api;
        } catch (IOException e) {
            log.error("load from file failed", e);
            throw new IllegalStateException("can not load data from: " + apiUrl);
        }
    }

}
