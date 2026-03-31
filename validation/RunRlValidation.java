import org.tcdrm.adaptive.api.TcdrmAdapter;

/**
 * RL validation runner (Phase 2).
 * Requires the Python client from tcdrm_gym to connect to the Py4J gateway.
 *
 * Start Python first in another terminal:
 *   cd ../tcdrm_gym
 *   uv run python connect_to_java.py --port 25333
 *
 * Then run this Java program (from validation/):
 *   javac -cp "<PATH_TO_JAR>" RunRlValidation.java
 *   java  -cp ".:<PATH_TO_JAR>" RunRlValidation
 */
public class RunRlValidation {
    public static void main(String[] args) {
        // Configure execution region and an optional popularity strategy
        TcdrmAdapter.setExecRegion("EU");
        TcdrmAdapter.setPopularityStrategy("EMA_TINYLFU", 2048, 4, 200, 0.6, 0.3);

        int timeoutSec = 120; // wait up to 2 minutes for Python
        TcdrmAdapter.runRlFigures(timeoutSec);
        System.out.println("\n[Validation] RL Phase complete → see images/ and metrics/\n");
    }
}
