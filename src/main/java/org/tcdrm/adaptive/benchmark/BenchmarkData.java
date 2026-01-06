package org.tcdrm.adaptive.benchmark;

import java.util.List;

public record BenchmarkData(
    List<Integer> repetitions,
    List<Double> totalTimeMs,
    List<Double> bandwidthCost,
    List<Double> cpuCost,
    List<Double> storageCost
) {
    public double getTotalCost(int index) {
        return bandwidthCost.get(index) + cpuCost.get(index) + storageCost.get(index);
    }

    public List<Double> getTotalCosts() {
        return repetitions.stream()
            .map(i -> getTotalCost(repetitions.indexOf(i)))
            .toList();
    }
}
