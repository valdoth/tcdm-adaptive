package org.tcdrm.adaptive.core;

import org.cloudsimplus.datacenters.Datacenter;
import java.util.ArrayList;
import java.util.List;

public class CloudRegion {
    private final String name;
    private final String location;
    private final double timeZone;
    private final double latencyMs;
    private final double bandwidthGbps;
    private final List<Datacenter> datacenters;

    public CloudRegion(String name, String location, double timeZone, double latencyMs, double bandwidthGbps) {
        this.name = name;
        this.location = location;
        this.timeZone = timeZone;
        this.latencyMs = latencyMs;
        this.bandwidthGbps = bandwidthGbps;
        this.datacenters = new ArrayList<>();
    }

    public void addDatacenter(Datacenter datacenter) {
        datacenters.add(datacenter);
        datacenter.setTimeZone(timeZone);
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getTimeZone() {
        return timeZone;
    }

    public double getLatencyMs() {
        return latencyMs;
    }

    public double getBandwidthGbps() {
        return bandwidthGbps;
    }

    public List<Datacenter> getDatacenters() {
        return datacenters;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) GMT%+.0f - Latency: %.1fms, BW: %.1f Gbps, DCs: %d",
            name, location, timeZone, latencyMs, bandwidthGbps, datacenters.size());
    }
}
