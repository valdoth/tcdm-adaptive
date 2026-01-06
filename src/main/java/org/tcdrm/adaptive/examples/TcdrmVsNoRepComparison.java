package org.tcdrm.adaptive.examples;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.tcdrm.adaptive.benchmark.BenchmarkData;
import org.tcdrm.adaptive.benchmark.NoRepBenchmark;
import org.tcdrm.adaptive.benchmark.TcdrmBenchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TcdrmVsNoRepComparison {

    public static void main(String[] args) {
        System.out.println("================ TCDRM vs NOREP Comparison ================");
        
        List<Double> fragmentSizesR1 = Arrays.asList(2.0, 1.5, 1.0);
        List<Double> fragmentSizesR2 = Arrays.asList(3.0, 2.5, 2.0, 1.5);

        generateComparisonChart("R1", fragmentSizesR1, 3);
        generateComparisonChart("R2", fragmentSizesR2, 3);
        
        System.out.println("================ Comparison Complete ================");
    }

    private static void generateComparisonChart(String queryId, List<Double> fragmentSizes, int replicationFactor) {
        System.out.println("\nGenerating comparison for query: " + queryId);
        System.out.println("Fragment sizes: " + fragmentSizes);
        System.out.println("Replication factor: " + replicationFactor);

        TcdrmBenchmark tcdrmBenchmark = new TcdrmBenchmark(replicationFactor, 42);
        NoRepBenchmark noRepBenchmark = new NoRepBenchmark(84);

        BenchmarkData tcdrmData = tcdrmBenchmark.computeBenchmark(queryId, fragmentSizes);
        BenchmarkData noRepData = noRepBenchmark.computeBenchmark(queryId, fragmentSizes);

        List<Integer> repetitions = tcdrmData.repetitions();
        List<Double> tcdrmSeconds = convertToSeconds(tcdrmData.totalTimeMs());
        List<Double> noRepSeconds = convertToSeconds(noRepData.totalTimeMs());

        generateTimeChart(queryId, repetitions, tcdrmSeconds, noRepSeconds);
        generateCostChart(queryId, repetitions, tcdrmData, noRepData);

        printSummary(queryId, tcdrmData, noRepData);
    }

    private static void generateTimeChart(String queryId, List<Integer> repetitions, 
                                         List<Double> tcdrmSeconds, List<Double> noRepSeconds) {
        XYChart chart = new XYChartBuilder()
            .width(800)
            .height(600)
            .title("TCDRM vs NOREP - Execution Time - " + queryId)
            .xAxisTitle("Number of Repetitions")
            .yAxisTitle("Execution Time (seconds)")
            .build();

        chart.addSeries("TCDRM (With Replication)", repetitions, tcdrmSeconds);
        chart.addSeries("NOREP (No Replication)", repetitions, noRepSeconds);

        String fileName = "tcdrm_vs_norep_time_" + queryId;
        try {
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);
            System.out.println("✓ Time chart generated: " + fileName + ".png");
        } catch (IOException e) {
            System.err.println("✗ Error generating time chart: " + e.getMessage());
        }
    }

    private static void generateCostChart(String queryId, List<Integer> repetitions,
                                         BenchmarkData tcdrmData, BenchmarkData noRepData) {
        XYChart chart = new XYChartBuilder()
            .width(800)
            .height(600)
            .title("TCDRM vs NOREP - Total Cost - " + queryId)
            .xAxisTitle("Number of Repetitions")
            .yAxisTitle("Total Cost ($)")
            .build();

        chart.addSeries("TCDRM (With Replication)", repetitions, tcdrmData.getTotalCosts());
        chart.addSeries("NOREP (No Replication)", repetitions, noRepData.getTotalCosts());

        String fileName = "tcdrm_vs_norep_cost_" + queryId;
        try {
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);
            System.out.println("✓ Cost chart generated: " + fileName + ".png");
        } catch (IOException e) {
            System.err.println("✗ Error generating cost chart: " + e.getMessage());
        }
    }

    private static void printSummary(String queryId, BenchmarkData tcdrmData, BenchmarkData noRepData) {
        int lastIndex = tcdrmData.repetitions().size() - 1;
        
        double tcdrmTime = tcdrmData.totalTimeMs().get(lastIndex) / 1000.0;
        double noRepTime = noRepData.totalTimeMs().get(lastIndex) / 1000.0;
        double timeImprovement = ((noRepTime - tcdrmTime) / noRepTime) * 100;

        double tcdrmCost = tcdrmData.getTotalCost(lastIndex);
        double noRepCost = noRepData.getTotalCost(lastIndex);
        double costDifference = ((tcdrmCost - noRepCost) / noRepCost) * 100;

        System.out.println("\n--- Summary for " + queryId + " ---");
        System.out.println("At " + tcdrmData.repetitions().get(lastIndex) + " repetitions:");
        System.out.printf("  TCDRM Time: %.2f s | NOREP Time: %.2f s | Improvement: %.2f%%\n", 
            tcdrmTime, noRepTime, timeImprovement);
        System.out.printf("  TCDRM Cost: $%.4f | NOREP Cost: $%.4f | Difference: %+.2f%%\n", 
            tcdrmCost, noRepCost, costDifference);
    }

    private static List<Double> convertToSeconds(List<Double> milliseconds) {
        return milliseconds.stream()
            .map(ms -> ms / 1000.0)
            .toList();
    }
}
