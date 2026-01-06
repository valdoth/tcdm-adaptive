package org.tcdrm.adaptive.replication;

import org.cloudsimplus.datacenters.Datacenter;
import java.util.ArrayList;
import java.util.List;

public class NoReplicationStrategy implements ReplicationStrategy {

    @Override
    public List<Datacenter> selectReplicaLocations(List<Datacenter> availableDatacenters, int replicationFactor) {
        List<Datacenter> single = new ArrayList<>();
        if (!availableDatacenters.isEmpty()) {
            single.add(availableDatacenters.get(0));
        }
        return single;
    }

    @Override
    public Datacenter selectBestReplica(List<Datacenter> replicas, Datacenter clientLocation) {
        return replicas.isEmpty() ? null : replicas.get(0);
    }

    @Override
    public String getStrategyName() {
        return "NOREP (No Replication)";
    }
}
