//package top.ivan.windrop.svc;
//
//import org.springframework.stereotype.Service;
//import top.ivan.windrop.util.RandomAccessKey;
//
//import java.util.function.Predicate;
//
///**
// * @author Ivan
// * @description
// * @date 2021/3/12
// */
//@Service
//public class ApplyKeyService {
//
//    private final RandomAccessKey randomAccessKey = new RandomAccessKey(30);
//
//    public boolean valid(Predicate<String> valid) {
//        return valid.test(randomAccessKey.getAccessKey());
//    }
//
//    public boolean updateIfValidated(Predicate<String> valid) {
//        boolean result = valid.test(randomAccessKey.getAccessKey());
//        if (result) {
//            update();
//        }
//        return result;
//    }
//
//    public boolean validOnce(Predicate<String> valid) {
//        boolean result = valid.test(randomAccessKey.getAccessKey());
//        update();
//        return result;
//    }
//
//    public String getKey() {
//        return randomAccessKey.getAccessKey();
//    }
//
//    public void update() {
//        randomAccessKey.update();
//    }
//
//}
