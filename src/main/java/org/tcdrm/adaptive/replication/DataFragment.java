package org.tcdrm.adaptive.replication;

import org.cloudsimplus.datacenters.Datacenter;
import java.util.ArrayList;
import java.util.List;

public class DataFragment {
    private final String fragmentId;
    private final double sizeGb;
    private final List<Datacenter> replicaLocations;
    private Datacenter primaryLocation;

    public DataFragment(String fragmentId, double sizeGb) {
        this.fragmentId = fragmentId;
        this.sizeGb = sizeGb;
        this.replicaLocations = new ArrayList<>();
    }

    public void addReplicaLocation(Datacenter datacenter) {
        if (!replicaLocations.contains(datacenter)) {
            replicaLocations.add(datacenter);
            if (primaryLocation == null) {
                primaryLocation = datacenter;
            }
        }
    }

    public String getFragmentId() {
        return fragmentId;
    }

    public double getSizeGb() {
        return sizeGb;
    }

    public List<Datacenter> getReplicaLocations() {
        return new ArrayList<>(replicaLocations);
    }

    public Datacenter getPrimaryLocation() {
        return primaryLocation;
    }

    public int getReplicationFactor() {
        return replicaLocations.size();
    }

    @Override
    public String toString() {
        return String.format("Fragment[%s, %.2f GB, %d replicas]", 
            fragmentId, sizeGb, replicaLocations.size());
    }
}
