package top.ivan.windrop.util.extern;

import lombok.AccessLevel;
import lombok.Getter;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Ivan
 * @since 2023/11/27 14:20
 */
public abstract class ExternAfter<T, EX> extends ExternMono<T, EX> implements CoreSubscriber<T> {

    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<ExternAfter> STATE = AtomicIntegerFieldUpdater.newUpdater(ExternAfter.class, "state");
    public static final int PREPARE = 0;
    public static final int PROCESSING = 1;
    public static final int READY = 2;

    @Getter(AccessLevel.PACKAGE)
    private final Publisher<T> source;
    private CoreSubscriber<? super T> subscribe;

    private volatile int state = PREPARE;
    private EX extern;

    public ExternAfter(Publisher<T> src) {
        this.source = src;
    }

    abstract EX accept(T t);

    EX getExtern(T t) {
        int s;
        do {
            s = this.state;
            if (s == READY) {
                return this.extern;
            }
        } while (s == PROCESSING || !STATE.compareAndSet(this, s, PROCESSING));

        try {
            extern = accept(t);
        } catch (Exception e) {
            state = READY;
            onError(e);
        } finally {
            state = READY;
        }
        return extern;
    }

    EX getExtern() {
        int s;
        do {
            s = this.state;
            if (s == READY) {
                return extern;
            }
            if (s == PREPARE) {
                throw new RuntimeException("Extern not initial");
            }
        } while (true);
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        this.subscribe = coreSubscriber;
        source.subscribe(this);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Integer.MAX_VALUE);
    }


    @Override
    public void onNext(T t) {
        subscribe.onNext(t);
        subscribe.onComplete();
    }

    @Override
    public void onError(Throwable throwable) {
        throw new RuntimeException(throwable);
    }

    @Override
    public void onComplete() {
    }

}
