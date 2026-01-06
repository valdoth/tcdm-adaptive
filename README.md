# TCDRM Adaptive

A cloud simulation project using CloudSim Plus for adaptive **Time-Critical Disaster Recovery Management** (TCDRM) with data replication strategies.

## 🎯 Description

This project implements and compares TCDRM (with replication) vs NOREP (without replication) strategies using CloudSim Plus framework. It provides:

- **Multi-cloud infrastructure simulation** with providers, regions, and datacenters
- **Data replication strategies** (TCDRM, NOREP)
- **Performance benchmarking** with realistic network conditions
- **Cost analysis** (bandwidth, CPU, storage)
- **Visual comparison** through generated charts

## ✨ Key Features

### 1. **Core Infrastructure** (`core/`)

- `CloudProvider`: Multi-cloud provider management (AWS, GCP, Azure, etc.)
- `CloudRegion`: Geographic regions with timezone and network parameters
- `NetworkTopology`: Inter-datacenter latency, bandwidth, and costs

### 2. **Replication Strategies** (`replication/`)

- `TcdrmReplicationStrategy`: Geographic distribution with latency-aware selection
- `NoReplicationStrategy`: Baseline without replication
- `DataFragment`: Data fragments with replica tracking

### 3. **Benchmarking** (`benchmark/`)

- `TcdrmBenchmark`: Performance with replication (local + remote access)
- `NoRepBenchmark`: Performance without replication (remote only)
- `BenchmarkData`: Results container (time, costs)

### 4. **Examples** (`examples/`)

- `BasicSimulationExample`: Simple CloudSim Plus demo
- `TcdrmVsNoRepComparison`: Complete TCDRM vs NOREP comparison with charts

## 📋 Prerequisites

- **Java 25** (or 17+)
- **Maven 3.9+**
- **JAVA_HOME** configured to Java 25

## 🚀 Quick Start

### 1. Configure Java 25

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home
```

### 2. Build the Project

```bash
cd /Users/valdo/Desktop/cloud/avancement/tcdrm-adaptive
mvn clean compile
```

### 3. Run TCDRM vs NOREP Comparison

```bash
mvn exec:java -Dexec.mainClass="org.tcdrm.adaptive.examples.TcdrmVsNoRepComparison"
```

### 4. Run Basic Simulation

```bash
mvn exec:java -Dexec.mainClass="org.tcdrm.adaptive.BasicSimulationExample"
```

## 📊 Benchmark Results

The comparison generates 4 PNG charts:

- `tcdrm_vs_norep_time_R1.png` - Execution time for query R1
- `tcdrm_vs_norep_time_R2.png` - Execution time for query R2
- `tcdrm_vs_norep_cost_R1.png` - Total cost for query R1
- `tcdrm_vs_norep_cost_R2.png` - Total cost for query R2

### Sample Output

```
================ TCDRM vs NOREP Comparison ================

Generating comparison for query: R1
Fragment sizes: [2.0, 1.5, 1.0]
Replication factor: 3
✓ Time chart generated: tcdrm_vs_norep_time_R1.png
✓ Cost chart generated: tcdrm_vs_norep_cost_R1.png

--- Summary for R1 ---
At 5000 repetitions:
  TCDRM Time: 876678.01 s | NOREP Time: 963668.22 s | Improvement: 9.03%
  TCDRM Cost: $199.66 | NOREP Cost: $293.44 | Difference: -31.96%
```

**Key Findings:**

- ✅ **TCDRM is ~9% faster** due to local replica access
- ✅ **TCDRM is ~32% cheaper** by reducing remote data transfers

## 📁 Project Structure

```
tcdrm-adaptive/
├── src/main/java/org/tcdrm/adaptive/
│   ├── core/                          # Infrastructure (Provider, Region, Network)
│   ├── replication/                   # Replication strategies
│   ├── benchmark/                     # Performance benchmarking
│   ├── examples/                      # Usage examples
│   └── utils/                         # Utilities (future)
├── pom.xml                            # Maven configuration
├── README.md                          # This file
└── PROJECT_STRUCTURE.md               # Detailed architecture
```

See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for detailed architecture documentation.

## 🔧 Dependencies

- **CloudSim Plus**: LATEST (8.5.1+) - Cloud simulation framework
- **Logback**: 1.5.18 - Logging framework
- **XChart**: 3.8.8 - Chart generation library

## 📈 Benchmark Parameters

### Network Configuration

| Type   | Bandwidth | Latency | Cost/GB |
| ------ | --------- | ------- | ------- |
| Local  | 10 Gbps   | 5 ms    | $0.00   |
| Remote | 1 Gbps    | 80 ms   | $0.0125 |

### Cost Model

- **CPU**: $0.02/hour
- **Storage**: $0.01/GB/hour
- **Processing**: 0.5 min/GB

### Load Simulation

- **Repetitions**: 500 to 5000 (step 500)
- **Load Factor**: 1.0x to 1.6x
- **Jitter**: ±5% (network and CPU)

## 🎓 Based On

This project is based on:

- `cloudsimplus-examples/multicloud/` - Multi-cloud simulation patterns
- `cloudsimplus-examples/multicloudfinal/MultiCloudFinalNoRep.java` - Benchmark implementation
- `template/` projects - Code structure and organization

## 🔄 Future Extensions

- [ ] VM migration strategies
- [ ] Live migration simulation
- [ ] BRITE topology integration
- [ ] Failure simulation
- [ ] Cost-optimized replication
- [ ] Statistical analysis tools

## 📚 References

- [CloudSim Plus](https://cloudsimplus.org/)
- [CloudSim Plus Documentation](https://cloudsimplus.org/docs/)
- [CloudSim Plus Examples](https://github.com/cloudsimplus/cloudsimplus-examples)

## 📝 License

Based on CloudSim Plus examples framework (GPLv3).

---

**Version**: 1.0.0-SNAPSHOT  
**Java**: 25  
**CloudSim Plus**: 8.5.1+
