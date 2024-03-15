package top.ivan.windrop.util.extern;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author Ivan
 * @since 2023/11/14 14:41
 */
public class ExternDelay<T, EX> extends ExternAfter<T, EX> {

    private final Function<T, EX> exMapping;

    public ExternDelay(Publisher<T> src, Function<T, EX> mapping) {
        super(src);
        this.exMapping = mapping;
    }

    @Override
    EX accept(T t) {
        return exMapping.apply(t);
    }

    @Override
    public <R> ExternMono<R, EX> transfer(Function<Mono<T>, Mono<R>> opt) {
        return new ExternDelay<>(opt.apply(this), r -> getExtern());
    }
}
