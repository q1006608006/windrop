package test;

import lombok.SneakyThrows;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Ivan
 * @since 2023/12/25 17:20
 */
public class WeakReferenceTest {

    public static class Inner {
        private String test = UUID.randomUUID().toString();

    }



    public static void main(String[] args) throws InterruptedException {
    /*    ConcurrentSkipListMap csm = new ConcurrentSkipListMap();
        csm.put("k1", "val");
        csm.put("k2", "val");
        csm.put("k3", "val");


        new Thread(() -> {
            try {
                Thread.sleep(1000);
                csm.put("k1", "val2");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        csm.forEach((k, v) -> {
            System.out.println(k + "-: " + v);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(csm.remove(k, v));
        });
*/

    }
}
