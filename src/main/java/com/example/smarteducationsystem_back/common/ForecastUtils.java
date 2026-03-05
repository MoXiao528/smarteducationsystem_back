package com.example.smarteducationsystem_back.common;

import java.util.List;

/**
 * 预测算法工具类（移动平均 + 线性回归）。
 */
public class ForecastUtils {

    /**
     * 简单移动平均预测。
     * 取末尾 windowSize 个值的均值作为下一步预测。
     *
     * @param values     历史序列
     * @param horizon    预测步数
     * @param windowSize 移动窗口大小（默认取 min(3, values.size())）
     * @return 预测值数组
     */
    public static double[] movingAverage(List<Double> values, int horizon, int windowSize) {
        if (values == null || values.isEmpty()) {
            return new double[horizon];
        }
        int ws = Math.min(windowSize, values.size());
        double[] result = new double[horizon];

        // 构建工作序列（历史 + 逐步追加预测值）
        double[] working = new double[values.size() + horizon];
        for (int i = 0; i < values.size(); i++) {
            working[i] = values.get(i);
        }

        for (int step = 0; step < horizon; step++) {
            int end = values.size() + step;
            double sum = 0;
            for (int j = end - ws; j < end; j++) {
                sum += working[j];
            }
            double predicted = sum / ws;
            working[end] = predicted;
            result[step] = predicted;
        }
        return result;
    }

    /**
     * 最小二乘线性回归预测。
     * x = 0, 1, 2, ... (时间步索引)  y = values
     *
     * @param values  历史序列
     * @param horizon 预测步数
     * @return 预测值数组
     */
    public static double[] linearRegression(List<Double> values, int horizon) {
        if (values == null || values.isEmpty()) {
            return new double[horizon];
        }
        int n = values.size();
        if (n == 1) {
            double[] result = new double[horizon];
            java.util.Arrays.fill(result, values.get(0));
            return result;
        }

        // 计算回归系数
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += (double) i * i;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        double[] result = new double[horizon];
        for (int step = 0; step < horizon; step++) {
            result[step] = intercept + slope * (n + step);
        }
        return result;
    }

    /**
     * 计算 MAE（留一交叉：用前 n-1 个点预测第 n 个点）。
     */
    public static double calcMAE(List<Double> values, String model) {
        if (values == null || values.size() < 3) {
            return 0.0;
        }
        double totalError = 0;
        int count = 0;

        for (int i = 2; i < values.size(); i++) {
            List<Double> sub = values.subList(0, i);
            double[] pred;
            if ("LR".equals(model)) {
                pred = linearRegression(sub, 1);
            } else {
                pred = movingAverage(sub, 1, Math.min(3, sub.size()));
            }
            totalError += Math.abs(pred[0] - values.get(i));
            count++;
        }
        return count > 0 ? totalError / count : 0.0;
    }

    /**
     * 估算预测置信区间（基于历史 MAE 的简单估算）。
     */
    public static double[] confidenceInterval(double predicted, double mae, int step) {
        // 随预测步数增大，不确定性线性增长
        double margin = mae * (1.0 + 0.3 * (step - 1));
        return new double[] { predicted - margin, predicted + margin };
    }
}
