# Validation (RL‑only) with the Shaded JAR

This folder provides a self‑contained validation for TCDRM‑ADAPTIVE focused on the RL models (Q‑Learning, DQN). No NoRep/TCDRM baselines are included here.

## What you get (RL only)
- RL metrics and popularity figures for Q‑Learning and DQN
- CSV exports per requête + résumé RL
- Outputs written in the current directory:
  - `images/*.png` (RL metrics & popularity)
  - `metrics/*.csv` (`rl_qlearning_*`, `rl_dqn_*`, `summary_phase2_rl.csv`, `log_overtime.csv`)

## Prerequisites
- Java 17+
- Maven 3.9+ (to build the shaded JAR if needed)
- Python 3.11+ with `uv`

## Quick Run

```bash
# 1) Prepare (detect jar, compile RL examples)
bash run.sh

# 2) Terminal A — start Python RL client (standalone here)
cd python
uv sync
uv run python connect_to_java.py --port 25333 \
  --qlearning-model models/qlearning_cloudsim.pkl \
  --dqn-model       models/dqn_cloudsim.pt

# 3) Terminal B — run Java RL validation
cd ..
java -cp .:lib/*with-dependencies.jar RunRlValidation
# or per example
java -cp .:lib/*with-dependencies.jar simpleQuerySimulation/QLearningEvaluation3000Cloudlet
java -cp .:lib/*with-dependencies.jar simpleQuerySimulation/DqnEvaluation3000Cloudlet
```

## Programmatic Use in another project
```java
import org.tcdrm.adaptive.api.TcdrmAdapter;

public class MyValidation {
    public static void main(String[] args) {
        // RL only (requires a Python client connected on the same port)
        TcdrmAdapter.runRlFigures(120); // wait up to 120s for Py4J client
    }
}
```

## Notes
- Default Py4J port is `25333` (`TCDRM_PY4J_PORT` to override).
- Workloads are legacy‑style (Simple/Complex) but results exported/graphés concernent uniquement Q‑Learning et DQN.
