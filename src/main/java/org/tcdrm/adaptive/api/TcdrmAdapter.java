package org.tcdrm.adaptive.api;

import org.tcdrm.adaptive.TcdrmMain;
import org.tcdrm.adaptive.core.RuntimeConfig;

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

    // =========================
    // High-level runners
    // =========================

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

    // Convenience aliases exposing intent
    public static void generateCsvAndChartsPhase1() { runPaperFigures(); }
    public static void generateCsvAndChartsRl(int timeoutSec) { runRlFigures(timeoutSec); }
}
