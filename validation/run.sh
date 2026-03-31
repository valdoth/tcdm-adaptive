#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# Locate shaded JAR (prefer local validation/lib, then legacy/jars, then target)
JAR=""
if ls ./lib/*with-dependencies.jar >/dev/null 2>&1; then
  JAR=$(ls -1 ./lib/*with-dependencies.jar | head -n1)
elif ls ../target/*with-dependencies.jar >/dev/null 2>&1; then
  JAR=$(ls -1 ../target/*with-dependencies.jar | head -n1)
else
  echo "❌ Shaded JAR not found. Build it with: mvn -DskipTests package" >&2
  exit 1
fi

echo "Using JAR: $JAR"

# Ensure local folders
mkdir -p images metrics python/models

# Copy RL models locally if available (do not overwrite user-provided)
if [ -d "../tcdrm_gym/models" ]; then
  [ -f python/models/qlearning_cloudsim.pkl ] || cp -f ../tcdrm_gym/models/qlearning_cloudsim.pkl python/models/ 2>/dev/null || true
  [ -f python/models/dqn_cloudsim.pt ] || cp -f ../tcdrm_gym/models/dqn_cloudsim.pt python/models/ 2>/dev/null || true
fi

# Clean previous RL-only outputs
rm -f images/*.png 2>/dev/null || true
rm -f metrics/rl_* metrics/summary_phase2_rl.csv 2>/dev/null || true
# keep log_overtime.csv as history

# Compile only RL validation examples (self-contained)
javac -cp "$JAR" RunRlValidation.java simpleQuerySimulation/*.java

echo "\nℹ️  RL validation ready. In another terminal:"
echo "   cd validation/python && uv sync && uv run python connect_to_java.py --port 25333"
echo "\n▶ Then run (this terminal):"
echo "   java -cp .:$JAR RunRlValidation"
echo "   # or per-example"
echo "   java -cp .:$JAR simpleQuerySimulation/QLearningEvaluation3000Cloudlet"
echo "   java -cp .:$JAR simpleQuerySimulation/DqnEvaluation3000Cloudlet"

# If Py4J gateway is already up on default port, try to run automatically
if lsof -i:25333 >/dev/null 2>&1; then
  echo "\n🚀 Gateway detected on 25333 — running RunRlValidation now..."
  java -cp .:$JAR RunRlValidation || true
  echo "\n📊 Generated files:"
  echo "Images:"
  ls -1 images 2>/dev/null || echo "(none)"
  echo "\nCSVs:"
  ls -1 metrics/*.csv 2>/dev/null || echo "(none)"
else
  echo "\n(Waiting for gateway on 25333 to auto-run. Start the Python client as above.)"
fi
