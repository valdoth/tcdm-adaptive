package org.tcdrm.adaptive.replication;

import org.cloudsimplus.datacenters.Datacenter;
import java.util.List;

public interface ReplicationStrategy {
    List<Datacenter> selectReplicaLocations(List<Datacenter> availableDatacenters, int replicationFactor);
    
    Datacenter selectBestReplica(List<Datacenter> replicas, Datacenter clientLocation);
    
    String getStrategyName();
}
