package top.ivan.windrop.util.extern;

import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author Ivan
 * @since 2023/10/30 15:27
 */
public class ExternJust<T, EX> extends ExternMono<T, EX> {


    private final Publisher<T> publisher;
    private final EX ex;

    ExternJust(Publisher<T> publisher, EX extern) {
        this.ex = extern;
        this.publisher = publisher;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        publisher.subscribe(coreSubscriber);
    }

    @Override
    public <R> ExternMono<R, EX> transfer(Function<Mono<T>, Mono<R>> opt) {
        return new ExternJust<>(opt.apply(this), ex);
    }

    @Override
    EX getExtern(T t) {
        return ex;
    }

}
