package org.tcdrm.adaptive.api;

import org.tcdrm.adaptive.TcdrmMain;
import org.tcdrm.adaptive.benchmark.BenchmarkData;
import org.tcdrm.adaptive.benchmark.BenchmarkExporter;
import org.tcdrm.adaptive.benchmark.BenchmarkRunner;
import org.tcdrm.adaptive.benchmark.ChartGenerator;
import org.tcdrm.adaptive.core.RuntimeConfig;
import org.tcdrm.adaptive.gateway.Py4JGateway;
import org.tcdrm.adaptive.rl.PythonRLBridge;

import java.io.File;
import java.io.IOException;

/**
 * Public adapter API for invoking TCDRM-ADAPTIVE from external projects
 * (e.g., cloudsim-multicloud-implementation) using the shaded JAR.
 *
 * Outputs are written to the host process working directory:
 * - images/*.png
 * - metrics/*.csv
 */
public final class TcdrmAdapter {

    private TcdrmAdapter() {}

    // =========================
    // Configuration
    // =========================

    /** Sets the execution region for the compute/join site (e.g., "EU", "US", "AS"). */
    public static void setExecRegion(String region) { RuntimeConfig.setExecRegion(region); }

    /**
     * Select popularity strategy: "EMA", "TINYLFU" or "EMA_TINYLFU".
     * Optional TinyLFU params can be null to keep defaults.
     */
    public static void setPopularityStrategy(String strategy, Integer width, Integer depth,
                                             Integer agingPeriod, Double tauHi, Double tauLo) {
        RuntimeConfig.setPopularityStrategy(strategy);
        RuntimeConfig.setTinyParams(width, depth, agingPeriod, tauHi, tauLo);
    }

    /** Reset all runtime configuration to defaults. */
    public static void resetConfig() { RuntimeConfig.reset(); }
    /** Override number of queries for the next runs. */
    public static void setMaxQueries(int queries) { RuntimeConfig.setMaxQueries(queries); }

    // =========================
    // High-level runners
    // =========================

    // Compatibility helpers (example-style API)
    /** Initialize/Reset simulation runtime configuration (compat helper). */
    public static void initSimulation() { RuntimeConfig.reset(); }
    /** Configure architecture strategy (compat helper; RL may override internally). */
    public static void createArchitecture(String strategy) { RuntimeConfig.setPopularityStrategy(strategy); }

    /**
     * Generates paper-style figures (Phase 1): NoRepLc vs. TCDRM, and writes metrics CSVs.
     */
    public static void runPaperFigures() {
        String[] args = new String[] { "--headless" , "--phase1-only" };
        try { TcdrmMain.main(args); }
        catch (Exception e) { throw new RuntimeException("TCDRM-ADAPTIVE Phase 1 failed", e); }
    }

    /**
     * Runs the RL extension (Phase 2): waits for Python client to connect to Py4J.
     * The Python client can be started from tcdrm_gym via connect_to_java.py.
     */
    public static void runRlFigures(int gatewayTimeoutSec) {
        String[] args = new String[] { "--headless", "--rl-only", "--py-timeout", Integer.toString(Math.max(10, gatewayTimeoutSec)) };
        try { TcdrmMain.main(args); }
        catch (Exception e) { throw new RuntimeException("TCDRM-ADAPTIVE Phase 2 failed", e); }
    }

    // =========================
    // RL single-model runners (separated Q-Learning / DQN)
    // =========================

    /** Run Q-Learning on simple workload only. Outputs go to images/ and metrics/. */
    public static void runQlearningSimple(int gatewayTimeoutSec) { runSingleRL("qlearning", false, 1000L, gatewayTimeoutSec); }
    /** Run Q-Learning on complex workload only. */
    public static void runQlearningComplex(int gatewayTimeoutSec) { runSingleRL("qlearning", true, 3000L, gatewayTimeoutSec); }
    /** Run DQN on simple workload only. */
    public static void runDqnSimple(int gatewayTimeoutSec) { runSingleRL("dqn", false, 2000L, gatewayTimeoutSec); }
    /** Run DQN on complex workload only. */
    public static void runDqnComplex(int gatewayTimeoutSec) { runSingleRL("dqn", true, 4000L, gatewayTimeoutSec); }

    private static void runSingleRL(String model, boolean complex, long seed, int timeoutSec) {
        System.setProperty("java.awt.headless", "true");
        new File("images").mkdirs();
        new File("metrics").mkdirs();

        Py4JGateway gateway = new Py4JGateway();
        int gwPort;
        try {
            gwPort = Integer.parseInt(System.getenv().getOrDefault("TCDRM_PY4J_PORT", "25333"));
        } catch (NumberFormatException nfe) {
            gwPort = 25333;
        }
        gateway.start(gwPort);

        try {
            PythonRLBridge bridge = waitForPython(gateway, Math.max(5, timeoutSec));
            if (bridge == null) throw new IllegalStateException("Python client not connected within timeout");

            bridge.resetCounters();
            if ("qlearning".equals(model) && !bridge.isQLearningReady()) {
                throw new IllegalStateException("Python Q-Learning model not loaded. Aborting run.");
            }
            if ("dqn".equals(model) && !bridge.isDQNReady()) {
                throw new IllegalStateException("Python DQN model not loaded. Aborting run.");
            }
            String name = ("qlearning".equals(model) ? "QLearning_" : "DQN_") + (complex ? "Complex" : "Simple");
            BenchmarkData data = BenchmarkRunner.runRL(bridge, model, name, complex, seed);

            // Export CSV metrics
            String csvName = "metrics/rl_" + ("qlearning".equals(model) ? "qlearning" : "dqn") + (complex ? "_complex.csv" : "_simple.csv");
            try {
                BenchmarkExporter.exportPerQueryCsv(data, csvName);
                BenchmarkExporter.exportOvertimeAverages(data, "metrics/log_overtime.csv", 100);
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to export CSV metrics", ioe);
            }

            // Export per-model figures only (no combined charts)
            String metricsPng = "images/metrics_" + ("qlearning".equals(model) ? "qlearning" : "dqn") + (complex ? "_complex.png" : "_simple.png");
            String popPng = "images/popularity_" + ("qlearning".equals(model) ? "qlearning" : "dqn") + (complex ? "_complex.png" : "_simple.png");
            try {
                ChartGenerator.generateModelMetrics(data, metricsPng, complex);
                ChartGenerator.generatePopularityAnalysis(data, popPng, complex);
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to generate charts", ioe);
            }

            System.out.println("\n[RL] " + name + " complete → see images/ and metrics/\n");
        } finally {
            gateway.stop();
        }
    }

    // === Two-model comparison helpers ===
    public static void runQlearningVsDqnSimple(int gatewayTimeoutSec) { runTwoModels(false, gatewayTimeoutSec); }
    public static void runQlearningVsDqnComplex(int gatewayTimeoutSec) { runTwoModels(true, gatewayTimeoutSec); }

    private static void runTwoModels(boolean complex, int timeoutSec) {
        System.setProperty("java.awt.headless", "true");
        new File("images").mkdirs();
        new File("metrics").mkdirs();

        Py4JGateway gateway = new Py4JGateway();
        int gwPort;
        try { gwPort = Integer.parseInt(System.getenv().getOrDefault("TCDRM_PY4J_PORT", "25333")); }
        catch (NumberFormatException nfe) { gwPort = 25333; }
        gateway.start(gwPort);

        try {
            PythonRLBridge bridge = waitForPython(gateway, Math.max(5, timeoutSec));
            if (bridge == null) throw new IllegalStateException("Python client not connected within timeout");

            bridge.resetCounters();
            if (!bridge.isQLearningReady()) {
                throw new IllegalStateException("Python Q-Learning model not loaded. Aborting two-model run.");
            }
            BenchmarkData ql = BenchmarkRunner.runRL(bridge, "qlearning", "QLearning_" + (complex?"Complex":"Simple"), complex, complex?3000L:1000L);

            bridge.resetCounters();
            if (!bridge.isDQNReady()) {
                throw new IllegalStateException("Python DQN model not loaded. Aborting two-model run.");
            }
            BenchmarkData dqn = BenchmarkRunner.runRL(bridge, "dqn", "DQN_" + (complex?"Complex":"Simple"), complex, complex?4000L:2000L);

            // Export CSVs
            try {
                BenchmarkExporter.exportPerQueryCsv(ql, "metrics/rl_qlearning_" + (complex?"complex":"simple") + ".csv");
                BenchmarkExporter.exportPerQueryCsv(dqn, "metrics/rl_dqn_" + (complex?"complex":"simple") + ".csv");
                BenchmarkExporter.exportOvertimeAverages(ql, "metrics/log_overtime.csv", 100);
                BenchmarkExporter.exportOvertimeAverages(dqn, "metrics/log_overtime.csv", 100);
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to export CSV metrics", ioe);
            }

            double tSla = complex ? org.tcdrm.adaptive.core.TcdrmConstants.TSLA_COMPLEX_MS : org.tcdrm.adaptive.core.TcdrmConstants.TSLA_SIMPLE_MS;
            // Two-model comparisons (methods handle IO internally)
            ChartGenerator.generateReplicaFactor2Models(ql, dqn, "images/rl2_replica_factor_" + (complex?"complex":"simple") + ".png");
            ChartGenerator.generateResponseTime2Models(ql, dqn, tSla, "images/rl2_response_time_" + (complex?"complex":"simple") + ".png");
            ChartGenerator.generateAvgBwPrice2Models(ql, dqn, "images/rl2_avg_bw_price_" + (complex?"complex":"simple") + ".png");
            ChartGenerator.generateCumulativeBwPrice2Models(ql, dqn, "images/rl2_cumulative_bw_price_" + (complex?"complex":"simple") + ".png");
            ChartGenerator.generateBwConsumption2Models(ql, dqn, "images/rl2_bw_consumption_" + (complex?"complex":"simple") + ".png");

            System.out.println("\n[RL] Two-model comparison complete → see images/ and metrics/\n");
        } finally {
            gateway.stop();
        }
    }

    

    private static PythonRLBridge waitForPython(Py4JGateway gateway, int maxWaitSeconds) {
        System.out.println("  Waiting for Python client (timeout: " + maxWaitSeconds + "s)...");
        int elapsed = 0;
        while (elapsed < maxWaitSeconds) {
            Object b = gateway.getPythonBridge();
            if (b != null) {
                System.out.println("  Python client connected!");
                return (PythonRLBridge) b;
            }
            try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
            elapsed += 2;
            if (elapsed % 10 == 0) {
                System.out.println("   ... waiting (" + elapsed + "s/" + maxWaitSeconds + "s)");
            }
        }
        return null;
    }

    // Convenience aliases exposing intent
    public static void generateCsvAndChartsPhase1() { runPaperFigures(); }
    public static void generateCsvAndChartsRl(int timeoutSec) { runRlFigures(timeoutSec); }
}
