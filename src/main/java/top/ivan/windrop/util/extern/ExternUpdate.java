package top.ivan.windrop.util.extern;

import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Ivan
 * @since 2023/11/24 11:07
 */
public class ExternUpdate<T, EX, NEW_EX> extends ExternAfter<T, NEW_EX> {

    private final BiFunction<T, EX, NEW_EX> exMapping;

    public ExternUpdate(ExternMono<T, EX> src, BiFunction<T, EX, NEW_EX> mapping) {
        super(src);
        this.exMapping = mapping;
    }

    @Override
    public <R> ExternMono<R, NEW_EX> transfer(Function<Mono<T>, Mono<R>> opt) {
        return with(opt.apply(this), r -> getExtern());
    }

    @Override
    NEW_EX accept(T t) {
        return exMapping.apply(t, ((ExternMono<T, EX>) getSource()).getExtern(t));
    }
}
