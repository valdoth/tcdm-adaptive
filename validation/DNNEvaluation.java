import org.tcdrm.adaptive.api.TcdrmAdapter;

/**
 * DQN validation runner (EMA by default).
 * - Runs workload queries in a single execution
 * - Exports CSVs and images into validation/{metrics,images}
 */
public class DNNEvaluation {
    private static final int TIMEOUT_SEC = 120;

    public static void main(String[] args) {
        System.out.println("Begin simulation (DQN: EMA)...");
        try {
            // Reset config (defaults to EMA); no createArchitecture() needed
            TcdrmAdapter.initSimulation();
            // Keep 3000 queries unless overridden externally
            TcdrmAdapter.setMaxQueries(3000);

            // Run workload
            TcdrmAdapter.runDqn(TIMEOUT_SEC);

            System.out.println("\n✅ DQN complete → images/ & metrics/\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unwanted errors happen during DQN run");
        }
    }
}
