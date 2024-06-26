package com.pub.core.utils;

import java.text.DecimalFormat;
import java.util.Random;

public class RandomUtilSendMsg {
    private static final Random random = new Random();

    private static final DecimalFormat fourdf = new DecimalFormat("0000");

    private static final DecimalFormat sixdf = new DecimalFormat("000000");

    //生成4位随机数
    public static String getFourBitRandom() {
        return fourdf.format(random.nextInt(10000));
    }
    //生成6位随机数
    public static String getSixBitRandom() {
        return sixdf.format(random.nextInt(1000000));
    }
}
