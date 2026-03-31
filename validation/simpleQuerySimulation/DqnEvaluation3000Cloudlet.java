import org.tcdrm.adaptive.api.TcdrmAdapter;

/**
 * Validation for RL DQN model using the shaded JAR.
 * Note: the RL phase runs both Q-Learning and DQN sequentially.
 */
public class DqnEvaluation3000Cloudlet {
    public static void main(String[] args) {
        TcdrmAdapter.resetConfig();
        TcdrmAdapter.setExecRegion("EU");
        TcdrmAdapter.setPopularityStrategy("EMA_TINYLFU", 2048, 4, 200, 0.6, 0.3);
        TcdrmAdapter.runRlFigures(120);
    }
}
