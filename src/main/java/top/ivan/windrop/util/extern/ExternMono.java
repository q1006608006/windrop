package top.ivan.windrop.util.extern;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Ivan
 * @since 2023/11/22 11:09
 */
public abstract class ExternMono<T, EX> extends Mono<T> {

    abstract EX getExtern(T t);

    public abstract <R> ExternMono<R, EX> transfer(Function<Mono<T>, Mono<R>> opt);

    public <NEW_EX> ExternMono<T, NEW_EX> update(BiFunction<T, EX, NEW_EX> opt) {
        return new ExternUpdate<>(this, opt);
    }

    public <NEW, NEW_EX> ExternMono<NEW, NEW_EX> convert(Function<ExternMono<T, EX>, ExternMono<NEW, NEW_EX>> opt) {
        return opt.apply(this);
    }

    public <R> ExternMono<R, EX> map(BiFunction<T, EX, R> opt) {
        return transfer(m -> m.map(t -> opt.apply(t, getExtern(t))));
    }

    public <R> ExternMono<R, EX> flatMap(BiFunction<T, EX, Mono<R>> opt) {
        return transfer(m -> m.flatMap(t -> opt.apply(t, getExtern(t))));
    }

    public ExternMono<T, EX> doOnNext(BiConsumer<T, EX> opt) {
        return transfer(m -> m.doOnNext(t -> opt.accept(t, getExtern(t))));
    }

    public static <T, EX> ExternMono<T, EX> just(Publisher<T> publisher, EX ex) {
        return new ExternJust<>(publisher, ex);
    }

    public static <T, EX> ExternMono<T, EX> with(Publisher<T> publisher, Function<T, EX> supplier) {
        return new ExternDelay<>(publisher, supplier);
    }
    
}
