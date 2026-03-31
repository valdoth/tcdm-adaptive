import org.tcdrm.adaptive.api.TcdrmAdapter;

/**
 * Unified Q-Learning validation runner (EMA by default).
 * - Runs simple then complex workloads in a single execution
 * - Exports CSVs and images into validation/{metrics,images}
 */
public class QLearningEvaluation {
    private static final int TIMEOUT_SEC = 120;

    public static void main(String[] args) {
        System.out.println("Begin simulation (Q-Learning: simple + complex, EMA)...");
        try {
            // Reset config (defaults to EMA); no createArchitecture() needed
            TcdrmAdapter.initSimulation();
            // Keep 3000 queries unless overridden externally
            TcdrmAdapter.setMaxQueries(3000);

            // Simple
            TcdrmAdapter.runQlearningSimple(TIMEOUT_SEC);
            // Complex
            TcdrmAdapter.runQlearningComplex(TIMEOUT_SEC);

            System.out.println("\n✅ Q-Learning simple+complex complete → images/ & metrics/\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unwanted errors happen during Q-Learning run");
        }
    }
}
