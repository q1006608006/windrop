package top.ivan.windrop.test;

import java.math.BigDecimal;

/**
 * @author Ivan
 * @since 2021/05/19 10:13
 */
public class DLossTest {

    public static void main(String[] args) {
        System.out.println("骑士" + getScope(3.1,32.7,1.5));
        System.out.println("苍白" + getScope(3.9,13.2,15.2));
        System.out.println();

        System.out.println("骑士" + getScope(10.5,12.4,5.8));
        System.out.println("苍白" + getScope(0,19.4,5.3));
        System.out.println();

        System.out.println("骑士" + getScope(3.5,21.8,4.4));
        System.out.println("苍白" + getScope(7,12.4,0));
        System.out.println();

        System.out.println(getScope(11.7,10.9,0));
        System.out.println();

        System.out.println("苍白" +getScope(0,7,2.8));
        System.out.println();

        System.out.println("骑士花" +getScope(3.9,22.5,15.2));
        System.out.println("骑士羽" +getScope(3.9,22.5,15.2));
        System.out.println("骑士花" +getScope(3.9,22.5,15.2));
        System.out.println("骑士花" +getScope(3.9,22.5,15.2));
        System.out.println("骑士花" +getScope(3.9,22.5,15.2));
        System.out.println();
    }

    public static double getScope(double bj,double bs,double gj) {
        double rs = bj * 2 + bs + gj * 4/ 3;
        BigDecimal v = new BigDecimal(rs);
        return v.setScale(2,BigDecimal.ROUND_CEILING).doubleValue();
    }
}
