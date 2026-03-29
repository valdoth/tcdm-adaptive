package org.tcdrm.adaptive.training;

import py4j.GatewayServer;

import java.util.HashMap;
import java.util.Map;
import ch.qos.logback.classic.Level;
import org.cloudsimplus.util.Log;

import java.net.InetAddress;

/**
 * Serveur Py4J pour l'entraînement RL.
 * 
 * Python (Gymnasium) se connecte à ce serveur pour:
 * - Créer des environnements d'entraînement
 * - Exécuter des simulations CloudSimPlus
 * - Recevoir les états et récompenses
 * 
 * Usage:
 *   java TrainingServer [port]
 */
public class TrainingServer {
    
    private TrainingEnvironment simpleEnv;
    private TrainingEnvironment complexEnv;
    private TrainingSettings settings = new TrainingSettings();
    
    /**
     * Crée un nouvel environnement d'entraînement.
     * 
     * @param seed Graine aléatoire pour reproductibilité
     * @param complex true pour requêtes complexes, false pour simples
     * @return L'environnement créé
     */
    public TrainingEnvironment createEnvironment(long seed, boolean complex) {
        if (complex) {
            complexEnv = new TrainingEnvironment(seed, true, settings);
            return complexEnv;
        } else {
            simpleEnv = new TrainingEnvironment(seed, false, settings);
            return simpleEnv;
        }
    }
    
    /**
     * Réinitialise l'environnement et retourne l'état initial.
     */
    public double[] reset(boolean complex) {
        TrainingEnvironment env = complex ? complexEnv : simpleEnv;
        if (env == null) {
            env = createEnvironment(System.currentTimeMillis(), complex);
        }
        return env.reset();
    }
    
    /**
     * Exécute une action dans l'environnement.
     * 
     * @param action 0=NOOP, 1=REPLICATE, 2=DELETE
     * @param complex true pour environnement complexe
     * @return Tableau [state..., reward, done (0 ou 1)]
     */
    public double[] step(int action, boolean complex) {
        TrainingEnvironment env = complex ? complexEnv : simpleEnv;
        if (env == null) {
            throw new IllegalStateException("Environment not created. Call reset() first.");
        }
        
        TrainingEnvironment.StepResult result = env.step(action);
        
        // Combiner état + reward + done dans un seul tableau pour Py4J
        double[] state = result.state();
        double[] output = new double[state.length + 2];
        System.arraycopy(state, 0, output, 0, state.length);
        output[state.length] = result.reward();
        output[state.length + 1] = result.done() ? 1.0 : 0.0;
        
        return output;
    }
    
    /**
     * Retourne l'état actuel de l'environnement.
     */
    public double[] getState(boolean complex) {
        TrainingEnvironment env = complex ? complexEnv : simpleEnv;
        if (env == null) {
            throw new IllegalStateException("Environment not created. Call reset() first.");
        }
        return env.getState();
    }
    
    /**
     * Retourne le nombre de requêtes exécutées.
     */
    public int getCurrentQuery(boolean complex) {
        TrainingEnvironment env = complex ? complexEnv : simpleEnv;
        return env != null ? env.getCurrentQuery() : 0;
    }
    
    /**
     * Retourne la récompense cumulative.
     */
    public double getCumulativeReward(boolean complex) {
        TrainingEnvironment env = complex ? complexEnv : simpleEnv;
        return env != null ? env.getCumulativeReward() : 0.0;
    }
    
    /**
     * Point d'entrée pour démarrer le serveur d'entraînement.
     */
    public static void main(String[] args) {
        String envPort = System.getenv("TCDRM_TRAIN_PORT");
        int defaultPort = 25335;
        int port = defaultPort;
        try {
            port = args.length > 0 ? Integer.parseInt(args[0]) : (envPort != null ? Integer.parseInt(envPort) : defaultPort);
        } catch (NumberFormatException nfe) {
            System.err.println("⚠️  Invalid TCDRM_TRAIN_PORT, falling back to " + defaultPort);
            port = defaultPort;
        }
        
        System.out.println("=".repeat(70));
        System.out.println("TCDRM TRAINING SERVER - CloudSimPlus Environment for RL");
        System.out.println("=".repeat(70));
        
        try {
            TrainingServer server = new TrainingServer();
            // Réduire la verbosité CloudSimPlus pendant l'entraînement
            Log.setLevel(Level.WARN);
            
            GatewayServer gatewayServer = new GatewayServer(server, port);
            
            System.out.println("🚀 Starting training server on port " + port + "...");
            gatewayServer.start();
            
            System.out.println("✅ Training server ready!");
            System.out.println("📡 Waiting for Python connections...");
            System.out.println();
            System.out.println("Python can connect with:");
            System.out.println("  from py4j.java_gateway import JavaGateway");
            System.out.println("  gateway = JavaGateway(gateway_parameters=GatewayParameters(port=" + port + "))");
            System.out.println("  server = gateway.entry_point");
            System.out.println();
            
            // Keep running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Configure la simulation depuis Python (schéma inspiré du repo de référence).
     * Exemples de clés supportées: maxEpisodeLength (Integer), tSlaSimpleMs (Double), tSlaComplexMs (Double)
     */
    public void configureSimulation(Map<String, Object> params) {
        if (params == null) return;
        Map<String, Object> p = new HashMap<>(params);
        Object maxEp = p.get("maxEpisodeLength");
        if (maxEp instanceof Number n) settings.setMaxEpisodeLength(n.intValue());
        Object tSimple = p.get("tSlaSimpleMs");
        if (tSimple instanceof Number n) settings.setTSlaSimpleMs(n.doubleValue());
        Object tComplex = p.get("tSlaComplexMs");
        if (tComplex instanceof Number n) settings.setTSlaComplexMs(n.doubleValue());
    }

    /** Reset structuré (retourne un objet avec state + info). */
    public TrainingResetResult resetStructured(boolean complex, long seed) {
        TrainingEnvironment env = complex ? complexEnv : simpleEnv;
        if (env == null || env.isDifferentSeedOrSettings(seed, settings)) {
            env = createEnvironment(seed, complex);
        }
        double[] state = env.reset();
        TrainingStepInfo info = new TrainingStepInfo(0.0, env.getCurrentQuery(), env.getCumulativeReward());
        return new TrainingResetResult(state, info);
    }

    /** Step structuré (retourne state + reward + done + info). */
    public TrainingStepResult stepStructured(int action, boolean complex) {
        TrainingEnvironment env = complex ? complexEnv : simpleEnv;
        if (env == null) {
            throw new IllegalStateException("Environment not created. Call resetStructured() first.");
        }
        TrainingEnvironment.StepResult r = env.step(action);
        // Pas de troncation dans notre boucle actuelle (pas timestep), donc truncated=false
        boolean truncated = false;
        TrainingStepInfo info = new TrainingStepInfo(
            env.getLastLatency(),
            env.getCurrentQuery(),
            env.getCumulativeReward()
        );
        return new TrainingStepResult(r.state(), r.reward(), r.done(), truncated, info);
    }
}

/** Paramètres configurables de l'entraînement/simulation. */
class TrainingSettings {
    private int maxEpisodeLength = -1; // -1 = utiliser constantes
    private double tSlaSimpleMs = -1;  // -1 = utiliser constantes
    private double tSlaComplexMs = -1; // -1 = utiliser constantes

    public int getMaxEpisodeLength() { return maxEpisodeLength; }
    public void setMaxEpisodeLength(int v) { this.maxEpisodeLength = v; }
    public double getTSlaSimpleMs() { return tSlaSimpleMs; }
    public void setTSlaSimpleMs(double v) { this.tSlaSimpleMs = v; }
    public double getTSlaComplexMs() { return tSlaComplexMs; }
    public void setTSlaComplexMs(double v) { this.tSlaComplexMs = v; }
}

/** Info renvoyée à chaque reset/step (horodatage simple). */
class TrainingStepInfo {
    private final double lastLatencyMs;
    private final int currentQuery;
    private final double cumulativeReward;

    public TrainingStepInfo(double lastLatencyMs, int currentQuery, double cumulativeReward) {
        this.lastLatencyMs = lastLatencyMs;
        this.currentQuery = currentQuery;
        this.cumulativeReward = cumulativeReward;
    }

    public double getLastLatencyMs() { return lastLatencyMs; }
    public int getCurrentQuery() { return currentQuery; }
    public double getCumulativeReward() { return cumulativeReward; }
}

/** Résultat de reset structuré: état + info. */
class TrainingResetResult {
    private final double[] state;
    private final TrainingStepInfo info;

    public TrainingResetResult(double[] state, TrainingStepInfo info) {
        this.state = state;
        this.info = info;
    }

    public double[] getState() { return state; }
    public TrainingStepInfo getInfo() { return info; }
}

/** Résultat de step structuré: état + reward + done + truncated + info. */
class TrainingStepResult {
    private final double[] state;
    private final double reward;
    private final boolean terminated;
    private final boolean truncated;
    private final TrainingStepInfo info;

    public TrainingStepResult(double[] state, double reward, boolean terminated, boolean truncated, TrainingStepInfo info) {
        this.state = state;
        this.reward = reward;
        this.terminated = terminated;
        this.truncated = truncated;
        this.info = info;
    }

    public double[] getState() { return state; }
    public double getReward() { return reward; }
    public boolean isTerminated() { return terminated; }
    public boolean isTruncated() { return truncated; }
    public TrainingStepInfo getInfo() { return info; }
}
