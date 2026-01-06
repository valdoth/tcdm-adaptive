package org.tcdrm.adaptive.core;

import org.cloudsimplus.datacenters.Datacenter;
import java.util.ArrayList;
import java.util.List;

public class CloudProvider {
    private final String name;
    private final double costPerHour;
    private final List<CloudRegion> regions;
    private final List<Datacenter> datacenters;

    public CloudProvider(String name, double costPerHour) {
        this.name = name;
        this.costPerHour = costPerHour;
        this.regions = new ArrayList<>();
        this.datacenters = new ArrayList<>();
    }

    public void addRegion(CloudRegion region) {
        regions.add(region);
        datacenters.addAll(region.getDatacenters());
    }

    public String getName() {
        return name;
    }

    public double getCostPerHour() {
        return costPerHour;
    }

    public List<CloudRegion> getRegions() {
        return regions;
    }

    public List<Datacenter> getDatacenters() {
        return datacenters;
    }

    @Override
    public String toString() {
        return String.format("%s (Cost: $%.3f/h, Regions: %d, DCs: %d)",
            name, costPerHour, regions.size(), datacenters.size());
    }
}
