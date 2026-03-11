### LOADING TESTING RESULTS

# Load Test Results

## Configuration

| Metric         | Value       |
|----------------|-------------|
| Total Clients  | 81          |
| Chat Duration  | 3s          |
| Queue Timeout  | 30,000ms    |
| Batch Size     | 100         |
| Servers        | 5           |

## Performance

| Metric                  | Value       |
|-------------------------|-------------|
| Wall Clock Time         | 58s         |
| Throughput              | ~1.4 clients/s |
| Max Concurrent Sessions | 5           |
| Theoretical Max Clients | ~95         |
| Efficiency              | ~85%        |

### Speed
81 clients were processed through a 5-worker concurrent queue in 58 seconds.

### Load Factor
With 3s chat duration per slot, each server can theoretically serve `58 / 3 = ~19`
clients over the test period, giving a theoretical maximum of `5 × 19 = 95` clients.
The system achieved 81 served — **85% of theoretical maximum** — indicating efficient
performance under load.

### Concurrency
Maximum of 5 simultaneous sessions, limited by the number of server worker threads.
All remaining clients queue via `LinkedBlockingQueue` and are served as slots free up.


## Iterative Serving

### Load Test Configuration

| Metric | Value |
|--------|-------|
| Total Clients | 81 |
| Chat Duration | 3s |
| Queue Timeout | 30,000ms |
| Batch Size | 100 |
| Servers | 1 |

### Results

| Metric | Value |
|--------|-------|
| Total Approached | 81 |
| Total Served | 28 |
| Total Lost | 53 |
| Wall Clock Time | 98s |
| Throughput | ~0.3/s |
| Theoretical Max | ~32 |
| Efficiency | ~87% |

With 1 server and 3s chat duration, the theoretical maximum is `98 / 3 = ~32` clients.
The system achieved 28 served — **87% efficiency** — but 53 clients (65%) timed out
waiting since only one server slot was ever free at a time.

Compared to concurrent serving, iterative delivered **3x less throughput** (0.3/s vs
1.4/s) with a **65% lost client rate** vs 0% — demonstrating the limitation of
single-threaded serving under load.


## Forking

### Load Test Configuration

| Metric | Value |
|--------|-------|
| Total Clients | 82 |
| Chat Duration | 3s |
| Queue Timeout | 30,000ms |
| Batch Size | 100 |
| Servers | 82 |

### Results

| Metric | Value |
|--------|-------|
| Total Approached | 81 |
| Total Served | 81 |
| Total Lost | 0 |
| Wall Clock Time | 14s |
| Throughput | ~5.8/s |
| Theoretical Max | ~82 |
| Efficiency | ~98% |

With 82 dedicated server slots — one per client — every client was assigned
immediately with no queuing. 14 seconds wall clock time reflects purely the
chat duration with near-zero wait time.

Compared to concurrent and iterative serving, forking delivered the highest
throughput (5.8/s) and zero lost clients, but at the cost of spawning 82
simultaneous threads and server slots — one per client. This approach does
not scale beyond the number of available servers.
