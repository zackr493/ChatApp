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


