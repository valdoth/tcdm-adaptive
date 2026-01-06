# TCDRM Adaptive - Project Structure

## 📁 Architecture Overview

```
tcdrm-adaptive/
├── src/main/java/org/tcdrm/adaptive/
│   ├── core/                          # Core infrastructure classes
│   │   ├── CloudProvider.java         # Cloud provider representation
│   │   ├── CloudRegion.java           # Geographic region with datacenters
│   │   └── NetworkTopology.java       # Network links and costs
│   │
│   ├── replication/                   # Data replication strategies
│   │   ├── ReplicationStrategy.java   # Strategy interface
│   │   ├── TcdrmReplicationStrategy.java  # TCDRM implementation
│   │   ├── NoReplicationStrategy.java     # Baseline (no replication)
│   │   └── DataFragment.java          # Data fragment with replicas
│   │
│   ├── benchmark/                     # Performance benchmarking
│   │   ├── BenchmarkData.java         # Benchmark results record
│   │   ├── TcdrmBenchmark.java        # TCDRM benchmark
│   │   └── NoRepBenchmark.java        # No replication benchmark
│   │
│   ├── examples/                      # Usage examples
│   │   ├── BasicSimulationExample.java    # Basic CloudSim Plus example
│   │   └── TcdrmVsNoRepComparison.java    # TCDRM vs NOREP comparison
│   │
│   └── utils/                         # Utility classes (future)
│
├── pom.xml                            # Maven configuration
├── README.md                          # Main documentation
└── PROJECT_STRUCTURE.md               # This file
```

## 🎯 Key Features

### 1. **Core Infrastructure** (`core/`)

- **CloudProvider**: Represents cloud providers (AWS, GCP, Azure, etc.)

  - Multiple regions per provider
  - Cost per hour configuration
  - Datacenter management

- **CloudRegion**: Geographic regions with datacenters

  - Timezone configuration
  - Latency and bandwidth parameters
  - Multiple datacenters per region

- **NetworkTopology**: Network configuration
  - Inter-datacenter latency
  - Bandwidth capacity
  - Transfer costs

### 2. **Replication Strategies** (`replication/`)

- **ReplicationStrategy Interface**: Common interface for all strategies

  - `selectReplicaLocations()`: Choose where to place replicas
  - `selectBestReplica()`: Choose optimal replica for query

- **TcdrmReplicationStrategy**: Time-Critical Disaster Recovery Management

  - Geographic distribution of replicas
  - Latency-aware replica selection
  - Configurable replication factor

- **NoReplicationStrategy**: Baseline without replication

  - Single copy of data
  - Used for comparison

- **DataFragment**: Represents data with replicas
  - Fragment ID and size
  - Replica locations tracking
  - Primary location management

### 3. **Benchmarking** (`benchmark/`)

- **BenchmarkData**: Results container (Java record)

  - Repetitions count
  - Execution time
  - Costs (bandwidth, CPU, storage)

- **TcdrmBenchmark**: TCDRM performance evaluation

  - Local vs remote access simulation
  - Load factor simulation (1.0 to 1.6x)
  - Network and CPU jitter
  - Cost calculation

- **NoRepBenchmark**: Baseline performance
  - Always remote access
  - Same load and jitter model
  - Cost calculation

### 4. **Examples** (`examples/`)

- **BasicSimulationExample**: Simple CloudSim Plus simulation

  - 2 datacenters, 2 VMs, 4 cloudlets
  - Demonstrates basic setup

- **TcdrmVsNoRepComparison**: Complete comparison
  - Generates time and cost charts
  - Multiple query scenarios (R1, R2)
  - Performance summary

## 🔧 Technology Stack

- **CloudSim Plus**: 8.5.1+ (cloud simulation framework)
- **Java**: 25 (compatible with 17+)
- **XChart**: 3.8.8 (chart generation)
- **Logback**: 1.5.18 (logging)
- **Maven**: 3.9+ (build tool)

## 🚀 Usage Examples

### Running Basic Simulation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home
mvn clean compile exec:java -Dexec.mainClass="org.tcdrm.adaptive.BasicSimulationExample"
```

### Running TCDRM vs NOREP Comparison

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home
mvn clean compile exec:java -Dexec.mainClass="org.tcdrm.adaptive.examples.TcdrmVsNoRepComparison"
```

## 📊 Benchmark Parameters

### Network Configuration

- **Local BW**: 10 Gbps, Latency: 5ms, Cost: $0.00/GB
- **Remote BW**: 1 Gbps, Latency: 80ms, Cost: $0.0125/GB

### Cost Model

- **CPU**: $0.02/hour
- **Storage**: $0.01/GB/hour
- **Processing**: 0.5 min/GB

### Load Simulation

- **Repetitions**: 500 to 5000 (step 500)
- **Load Factor**: 1.0x to 1.6x (increases with repetitions)
- **Jitter**: ±5% (network and CPU)

## 🎓 Design Patterns

1. **Strategy Pattern**: Replication strategies are interchangeable
2. **Builder Pattern**: CloudSim Plus fluent API
3. **Record Pattern**: Immutable benchmark data (Java 17+)
4. **Factory Pattern**: Cloud provider and region creation

## 📈 Expected Outputs

### Charts Generated

- `tcdrm_vs_norep_time_R1.png` - Execution time comparison for query R1
- `tcdrm_vs_norep_time_R2.png` - Execution time comparison for query R2
- `tcdrm_vs_norep_cost_R1.png` - Cost comparison for query R1
- `tcdrm_vs_norep_cost_R2.png` - Cost comparison for query R2

### Console Output

```
================ TCDRM vs NOREP Comparison ================

Generating comparison for query: R1
Fragment sizes: [2.0, 1.5, 1.0]
Replication factor: 3
✓ Time chart generated: tcdrm_vs_norep_time_R1.png
✓ Cost chart generated: tcdrm_vs_norep_cost_R1.png

--- Summary for R1 ---
At 5000 repetitions:
  TCDRM Time: 1234.56 s | NOREP Time: 2345.67 s | Improvement: 47.35%
  TCDRM Cost: $12.34 | NOREP Cost: $10.23 | Difference: +20.61%

...
```

## 🔄 Future Extensions

### Planned Features

1. **Migration Strategies** (`migration/`)

   - VM migration policies
   - Live migration simulation
   - Cost-aware migration

2. **Advanced Topologies** (`core/`)

   - BRITE topology integration
   - Custom network models
   - Failure simulation

3. **More Strategies** (`replication/`)

   - Cost-optimized replication
   - Latency-optimized replication
   - Hybrid strategies

4. **Analytics** (`utils/`)
   - Statistical analysis
   - Performance metrics
   - Cost optimization

## 📚 References

Based on:

- CloudSim Plus Examples - `multicloud/` package
- MultiCloudFinalNoRep.java - Benchmark implementation
- Template projects - Code structure and organization

## 🤝 Contributing

This project follows the structure and patterns from:

- `/Users/valdo/Desktop/cloud/avancement/cloudsimplus-examples`
- `/Users/valdo/Desktop/cloud/avancement/template`

Maintain consistency with existing code style and patterns.
