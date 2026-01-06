package org.tcdrm.adaptive.examples;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.tcdrm.adaptive.benchmark.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Generate dual graphs (raw + smoothed) matching the TCDRM article layout
 */
public class TcdrmArticleGraphsDual {

    public static void main(String[] args) throws IOException {
        System.out.println("================ TCDRM Article Dual Graphs Generation ================\n");
        
        // R1 = F1 + F41 + F80 (3 fragments from different regions)
        // Simple query with 3 fragments
        generateDualGraphForQuery("R1", Arrays.asList(1.5, 2.0, 1.8), 3);
        
        // R2 = F2 + F21 + F32 + F45 + F71 + F80 (6 fragments from different regions)
        // Complex query with 6 fragments (2x more than R1)
        generateDualGraphForQuery("R2", Arrays.asList(1.8, 2.2, 1.5, 2.5, 1.9, 2.0), 3);
        
        System.out.println("\n================ Dual Graphs Generation Complete ================");
    }

    private static void generateDualGraphForQuery(String queryId, List<Double> fragmentSizes, int replicationFactor) throws IOException {
        System.out.println("Generating dual graph for query: " + queryId);
        System.out.println("Fragment sizes: " + fragmentSizes);
        System.out.println("Replication factor: " + replicationFactor);

        // Compute benchmarks
        TcdrmBenchmarkPerQuery tcdrmBench = new TcdrmBenchmarkPerQuery(replicationFactor, 42L);
        NoRepBenchmarkPerQuery norepBench = new NoRepBenchmarkPerQuery(42L);

        BenchmarkDataPerQuery tcdrmData = tcdrmBench.computeBenchmark(queryId, fragmentSizes);
        BenchmarkDataPerQuery norepData = norepBench.computeBenchmark(queryId, fragmentSizes);

        // Create raw graph (left)
        XYChart rawChart = createRawChart(queryId, tcdrmData, norepData);
        
        // Create smoothed graph (right)
        XYChart smoothedChart = createSmoothedChart(queryId, tcdrmData, norepData);

        // Save individual charts
        String rawFilename = "tcdrm_dual_raw_" + queryId + ".png";
        String smoothedFilename = "tcdrm_dual_smoothed_" + queryId + ".png";
        
        BitmapEncoder.saveBitmap(rawChart, rawFilename, BitmapEncoder.BitmapFormat.PNG);
        BitmapEncoder.saveBitmap(smoothedChart, smoothedFilename, BitmapEncoder.BitmapFormat.PNG);
        
        System.out.println("✓ Raw chart: " + rawFilename);
        System.out.println("✓ Smoothed chart: " + smoothedFilename);
        
        // Create combined image (side by side)
        combineTwoCharts(rawChart, smoothedChart, "tcdrm_dual_combined_" + queryId + ".png");
        System.out.println("✓ Combined chart: tcdrm_dual_combined_" + queryId + ".png\n");
    }

    private static XYChart createRawChart(String queryId, BenchmarkDataPerQuery tcdrm, BenchmarkDataPerQuery norep) {
        XYChart chart = new XYChartBuilder()
            .width(600)
            .height(400)
            .title("Impact of Replication on Response Time (Raw) - " + queryId)
            .xAxisTitle("Number of Queries")
            .yAxisTitle("Response Time (seconds)")
            .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setMarkerSize(0);
        chart.getStyler().setXAxisTickMarkSpacingHint(100);
        chart.getStyler().setXAxisMin(0.0);
        chart.getStyler().setXAxisMax(1000.0);

        // TCDRM series (raw with jitter)
        XYSeries tcdrmSeries = chart.addSeries("TCDRM", tcdrm.queryNumbers(), tcdrm.timePerQueryMs());
        tcdrmSeries.setLineColor(new Color(31, 119, 180));
        tcdrmSeries.setLineWidth(1.0f);

        // NOREP series (raw with jitter)
        XYSeries norepSeries = chart.addSeries("NOREP", norep.queryNumbers(), norep.timePerQueryMs());
        norepSeries.setLineColor(new Color(255, 127, 14));
        norepSeries.setLineWidth(1.0f);

        return chart;
    }

    private static XYChart createSmoothedChart(String queryId, BenchmarkDataPerQuery tcdrm, BenchmarkDataPerQuery norep) {
        XYChart chart = new XYChartBuilder()
            .width(600)
            .height(400)
            .title("Impact of Replication on Response Time (Smoothed) - " + queryId)
            .xAxisTitle("Number of Queries")
            .yAxisTitle("Response Time (seconds)")
            .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setMarkerSize(0);
        chart.getStyler().setXAxisTickMarkSpacingHint(100);
        chart.getStyler().setXAxisMin(0.0);
        chart.getStyler().setXAxisMax(1000.0);

        // Apply moving average smoothing
        int windowSize = 50;
        List<Double> tcdrmSmoothed = movingAverage(tcdrm.timePerQueryMs(), windowSize);
        List<Double> norepSmoothed = movingAverage(norep.timePerQueryMs(), windowSize);

        // TCDRM series (smoothed)
        XYSeries tcdrmSeries = chart.addSeries("TCDRM", tcdrm.queryNumbers(), tcdrmSmoothed);
        tcdrmSeries.setLineColor(new Color(31, 119, 180));
        tcdrmSeries.setLineWidth(2.5f);

        // NOREP series (smoothed)
        XYSeries norepSeries = chart.addSeries("NOREP", norep.queryNumbers(), norepSmoothed);
        norepSeries.setLineColor(new Color(255, 127, 14));
        norepSeries.setLineWidth(2.5f);

        return chart;
    }

    private static List<Double> movingAverage(List<Double> data, int windowSize) {
        List<Double> smoothed = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - windowSize / 2);
            int end = Math.min(data.size(), i + windowSize / 2 + 1);
            
            double sum = 0.0;
            for (int j = start; j < end; j++) {
                sum += data.get(j);
            }
            smoothed.add(sum / (end - start));
        }
        return smoothed;
    }

    private static void combineTwoCharts(XYChart leftChart, XYChart rightChart, String filename) throws IOException {
        BufferedImage leftImage = BitmapEncoder.getBufferedImage(leftChart);
        BufferedImage rightImage = BitmapEncoder.getBufferedImage(rightChart);

        int width = leftImage.getWidth() + rightImage.getWidth();
        int height = Math.max(leftImage.getHeight(), rightImage.getHeight());

        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = combined.createGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        g.drawImage(leftImage, 0, 0, null);
        g.drawImage(rightImage, leftImage.getWidth(), 0, null);
        g.dispose();

        ImageIO.write(combined, "png", new File(filename));
    }
}
