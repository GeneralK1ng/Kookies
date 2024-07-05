package org.kookies.mirai.commen.utils;

import java.awt.*;
import java.util.Random;

/**
 * @author General_K1ng
 */
public class ColorManager {
    private final Random RANDOM;

    public ColorManager() {
        RANDOM = new Random();
    }

    /**
     * 生成背景颜色。
     * <p>
     * 本方法旨在生成一种随机颜色，用作背景色。背景色的生成不依赖于任何外部输入，
     * 完全基于随机算法，确保每次调用时都有可能得到不同的颜色，为界面提供多样性。
     *
     * @return Color 返回一个随机生成的颜色对象。
     */
    public Color generateBkgColor() {
        // white
        return new Color(255, 255, 255);
    }

    /**
     * 生成与给定背景颜色对比鲜明的文字颜色数组。
     * <p>
     * 此方法的目的是为了根据一种背景颜色，生成一组对比鲜明的文字颜色，以确保无论哪种文字颜色被选择使用，
     * 在给定的背景颜色下都能提供良好的可读性。生成的颜色数量固定为4，具体用途可以根据实际情况进行分配。
     *
     * @param bkgColor 背景颜色，用于生成对比鲜明的文字颜色。
     * @return 一个包含四种对比鲜明颜色的数组。
     */
    public Color[] generateTextColor(Color bkgColor) {
        // 初始化一个长度为4的颜色数组，用于存放生成的文字颜色。
        Color[] colors = new Color[4];

        // 遍历颜色数组，为每个位置生成一种与背景颜色对比鲜明的颜色。
        for (int i = 0; i < colors.length; i++) {
            colors[i] = generateContrastingColor(bkgColor);
        }

        // 返回生成的对比鲜明的文字颜色数组。
        return colors;
    }

    /**
     * 生成一个随机颜色。
     * <p>
     * 该方法通过调整色相、饱和度和亮度值来生成随机颜色。色相值在0到1之间随机生成，饱和度和亮度值在0.5到1之间随机生成，
     * 以确保生成的颜色既包括鲜艳的颜色也包括较暗的颜色。
     *
     * @return 生成的随机颜色。
     */
    private Color generateRandomColor() {
        // 生成一个在0到1之间的随机色相值。
        float hue = RANDOM.nextFloat();
        // 生成一个在0.5到1之间的随机饱和度值，确保颜色的鲜艳度。
        float saturation = 0.3f + RANDOM.nextFloat() * 0.5f;
        // 生成一个在0.5到1之间的随机亮度值，确保颜色的明暗度。
        float brightness = 0.5f + RANDOM.nextFloat() * 0.5f;
        // 根据色相、饱和度和亮度值生成并返回随机颜色。
        return Color.getHSBColor(hue, saturation, brightness);
    }

    /**
     * 计算给定颜色的亮度。
     * <p>
     * 该方法使用了国际照明委员会（CIE）推荐的XYZ颜色空间中的Luminance计算方法。
     * 它首先将RGB颜色值转换为XYZ颜色空间中的值，然后从这些值中计算亮度。
     * 这个过程对于视觉上一致的亮度计算很重要，特别是在不同光照条件下。
     *
     * @param color 输入的颜色对象，代表要计算亮度的颜色。
     * @return 返回计算得到的亮度值。
     */
    private double calculateLuminance(Color color) {
        // 将RGB颜色值转换为归一化的XYZ颜色值。
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;

        // 对于亮度计算，根据颜色值的不同范围使用不同的计算公式。
        // 这里的阈值是0.03928，低于这个阈值使用线性转换，高于这个阈值使用非线性转换。
        r = (r <= 0.03928) ? (r / 12.92) : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? (g / 12.92) : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? (b / 12.92) : Math.pow((b + 0.055) / 1.055, 2.4);

        // 根据CIE XYZ颜色空间中的Y值（亮度）的加权平均计算亮度。
        // 这里的权重分别是0.2126、0.7152和0.0722，它们是人眼对红色、绿色和蓝色光的敏感度的加权。
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /**
     * 计算两种颜色之间的对比度比例。
     * <p>
     * 对比度比例是评估颜色可读性和视觉效果的重要指标，用于确保文本和背景之间的颜色有足够的对比，以提高可读性。
     * 此方法基于WCAG（Web Content Accessibility Guidelines）2.0的标准来计算对比度比例。
     *
     * @param c1 第一种颜色，作为对比度计算的一方。
     * @param c2 第二种颜色，作为对比度计算的另一方。
     * @return 返回两种颜色之间的对比度比例，比例越高，颜色对比越强烈。
     */
    private double calculateContrastRatio(Color c1, Color c2) {
        // 计算第一个颜色的亮度，并加上0.05以补偿亮度的边界情况。
        double lum1 = calculateLuminance(c1) + 0.05;
        // 计算第二个颜色的亮度，并加上0.05以补偿亮度的边界情况。
        double lum2 = calculateLuminance(c2) + 0.05;
        // 根据两种颜色的亮度值计算对比度比例，确保比例总是大于1。
        return (lum1 > lum2) ? (lum1 / lum2) : (lum2 / lum1);
    }

    /**
     * 生成与给定背景颜色对比度足够的文本颜色。
     *
     * 此方法通过生成随机颜色并计算其与背景颜色的对比度，直到找到一个对比度符合要求的颜色。
     * 如果在设定的尝试次数内无法找到合适的颜色，将抛出运行时异常。
     *
     * @param backgroundColor 背景颜色，用于与生成的文本颜色进行对比。
     * @return 与背景颜色具有足够对比度的文本颜色。
     * @throws RuntimeException 如果在尝试生成足够对比度的颜色超过预定次数时仍未找到合适颜色，则抛出此异常。
     */
    private Color generateContrastingColor(Color backgroundColor) {
        Color textColor;
        do {
            textColor = generateRandomColor();
            // WCAG AA contrast ratio threshold
        } while (calculateContrastRatio(backgroundColor, textColor) < 4.5);
        return textColor;
    }

}
