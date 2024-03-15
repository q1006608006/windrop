package test;

import java.math.BigDecimal;

/**
 * @author Ivan
 * @since 2023/11/30 14:25
 */
public class RandomSecret {

    public static void main(String[] args) {
        float f1 = 1.0F;
        float f2 = 0.9F;
        float intern = 0.1F;

        BigDecimal b1 = new BigDecimal(f1);
        BigDecimal b2 = new BigDecimal(f2);
        BigDecimal bintern = new BigDecimal(Float.toString(intern));

        //比较1.0F - 0.9F == 0.1F，预期结果为true
        System.out.println(f1 - f2 == intern); //结果可能为false
        System.out.println(b1.subtract(b2).compareTo(bintern) == 0); //结果为true
    }

}
