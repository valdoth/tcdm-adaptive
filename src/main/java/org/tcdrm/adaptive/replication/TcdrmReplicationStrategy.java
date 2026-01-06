package org.tcdrm.adaptive.replication;

import org.cloudsimplus.datacenters.Datacenter;
import org.tcdrm.adaptive.core.NetworkTopology;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TcdrmReplicationStrategy implements ReplicationStrategy {
    private final NetworkTopology networkTopology;
    private final int replicationFactor;

    public TcdrmReplicationStrategy(NetworkTopology networkTopology, int replicationFactor) {
        this.networkTopology = networkTopology;
        this.replicationFactor = replicationFactor;
    }

    @Override
    public List<Datacenter> selectReplicaLocations(List<Datacenter> availableDatacenters, int replicationFactor) {
        if (availableDatacenters.size() <= replicationFactor) {
            return new ArrayList<>(availableDatacenters);
        }

        List<Datacenter> selected = new ArrayList<>();
        List<Datacenter> remaining = new ArrayList<>(availableDatacenters);

        selected.add(remaining.remove(0));

        while (selected.size() < replicationFactor && !remaining.isEmpty()) {
            Datacenter best = findMostDistantDatacenter(selected, remaining);
            selected.add(best);
            remaining.remove(best);
        }

        return selected;
    }

    @Override
    public Datacenter selectBestReplica(List<Datacenter> replicas, Datacenter clientLocation) {
        return replicas.stream()
            .min(Comparator.comparingDouble(dc -> networkTopology.getLatency(clientLocation, dc)))
            .orElse(replicas.get(0));
    }

    private Datacenter findMostDistantDatacenter(List<Datacenter> selected, List<Datacenter> remaining) {
        return remaining.stream()
            .max(Comparator.comparingDouble(dc -> calculateMinDistance(dc, selected)))
            .orElse(remaining.get(0));
    }

    private double calculateMinDistance(Datacenter dc, List<Datacenter> selected) {
        return selected.stream()
            .mapToDouble(s -> networkTopology.getLatency(dc, s))
            .min()
            .orElse(0.0);
    }

    @Override
    public String getStrategyName() {
        return "TCDRM (Time-Critical Disaster Recovery Management)";
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }
}
