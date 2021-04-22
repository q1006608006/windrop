package top.ivan.windrop.test;

/**
 * @author Ivan
 * @description
 * @date 2021/3/3
 */
public class YuanShenTest {
    public static void main(String[] args) throws Exception {
        int round = 5000000;
        test(round, 4092, 11610, 606);
        test(round, 4459, 12234, 641);
        test(round, 4533, 12439, 575);
        test(round, 4382, 11685, 629);
        test(round, 5007, 13172, 601);
        test(round, 5107, 13972, 630);
//        test(round, 700, 1900, 700);
    }

    private static void test(int round, long minA, long maxA, int rga) {
        long total = 0;
        rga = 1000 - rga;

        for (int i = 0; i < round; i++) {
            int rgx = (int) (Math.random() * 1000);
            if (rgx >= rga) {
                total += maxA;
            } else {
                total += minA;
            }
        }

        System.out.println(total / round);
    }

//    56683448
//    58210000
//    63609999
}
