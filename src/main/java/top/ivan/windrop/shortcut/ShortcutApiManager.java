package top.ivan.windrop.shortcut;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import top.ivan.windrop.bean.ShortcutApi;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.util.JSONUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        if (apiUrl.startsWith("http")) {
            apiSupplier = httpSuppler();
        } else if (apiUrl.startsWith("file:")) {
            apiSupplier = fileSuppler();
        } else {
            throw new IllegalArgumentException("can not support protocol for: " + apiUrl);
        }
    }

    private Supplier<ShortcutApi> httpSuppler() {
        HttpClient client = HttpClient.create().secure(SslProvider.defaultClientProvider());
        String json = client.get().uri(apiUrl).responseContent().toString();
        try {
            ShortcutApi api = JSONUtil.read(json, ShortcutApi.class);
            return () -> api;
        } catch (Exception e) {
            log.error("can not load json", e);
        }
        throw new IllegalStateException("can not load data from: " + apiUrl);
    }

    private Supplier<ShortcutApi> fileSuppler() {
        URL url;
        try {
            url = new URL(apiUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("can not parse url: " + apiUrl);
        }

        try {
            byte[] data = Files.readAllBytes(Paths.get(url.getFile()));
            ShortcutApi api = JSONUtil.read(new String(data), ShortcutApi.class);
            return () -> api;
        } catch (IOException e) {
            log.error("load from file failed", e);
        }
        throw new IllegalStateException("can not load data from: " + apiUrl);
    }

}
