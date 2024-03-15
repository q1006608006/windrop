package top.ivan.windrop;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import top.ivan.windrop.util.extern.ExternMono;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Ivan
 * @since 2023/10/23 14:10
 */
class ExMonoTest {

    @Test
    void testGrammar() {
        String str = ExternMono.just(Mono.just("str"), new LongAdder())
                .map(ExMonoTest::toS)
                .flatMap((s, i) -> Mono.just(toS(s, i)))
                .doOnNext((s, i) -> System.out.println(toS(s, i)))
                .block();

        assert "str_1_2".equals(str);
    }

    private static String toS(String src, LongAdder cc) {
        cc.increment();
        return src + "_" + cc;
    }
}