package org.kookies.mirai.commen.utils;

import java.util.Random;

public class ProbabilityTrigger {

    private static final Random random = new Random();

    /**
     * 判断是否触发操作
     * @param probability 触发操作的概率，范围从 0.0 到 1.0
     * @return 是否触发操作
     */
    public static boolean shouldTrigger(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }
        // 生成一个 0.0 到 1.0 之间的随机数
        double randomValue = random.nextDouble();
        // 判断随机数是否小于等于指定的概率
        return randomValue <= probability;
    }
}
