package org.tcdrm.adaptive.core;

import org.cloudsimplus.datacenters.Datacenter;
import java.util.HashMap;
import java.util.Map;

public class NetworkTopology {
    private final Map<String, Double> latencyMap;
    private final Map<String, Double> bandwidthMap;
    private final Map<String, Double> costMap;

    public NetworkTopology() {
        this.latencyMap = new HashMap<>();
        this.bandwidthMap = new HashMap<>();
        this.costMap = new HashMap<>();
    }

    public void setLink(Datacenter dc1, Datacenter dc2, double latencyMs, double bandwidthGbps, double costPerGb) {
        String key = getLinkKey(dc1, dc2);
        latencyMap.put(key, latencyMs);
        bandwidthMap.put(key, bandwidthGbps);
        costMap.put(key, costPerGb);
    }

    public double getLatency(Datacenter dc1, Datacenter dc2) {
        String key = getLinkKey(dc1, dc2);
        return latencyMap.getOrDefault(key, 100.0);
    }

    public double getBandwidth(Datacenter dc1, Datacenter dc2) {
        String key = getLinkKey(dc1, dc2);
        return bandwidthMap.getOrDefault(key, 1.0);
    }

    public double getCost(Datacenter dc1, Datacenter dc2) {
        String key = getLinkKey(dc1, dc2);
        return costMap.getOrDefault(key, 0.01);
    }

    private String getLinkKey(Datacenter dc1, Datacenter dc2) {
        long id1 = dc1.getId();
        long id2 = dc2.getId();
        return (id1 < id2) ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    public double calculateTransferTime(Datacenter source, Datacenter dest, double dataSizeGb) {
        double latency = getLatency(source, dest);
        double bandwidth = getBandwidth(source, dest);
        return latency + (dataSizeGb * 8000.0 / bandwidth);
    }

    public double calculateTransferCost(Datacenter source, Datacenter dest, double dataSizeGb) {
        return getCost(source, dest) * dataSizeGb;
    }
}
