# ChatServer

A concurrent chat server built with Spring Boot that manages client-server communication through a thread pool architecture.

## Prerequisites

- Java 21+
- Maven 3.8+

## How to Run

**1. Clone the repository:**
```bash
git clone 
cd services/chat-app
```

**2. Build:**
```bash
mvn clean install
```

**3. Run:**
```bash
mvn spring:boot run
```

The app starts on `http://localhost:8080`.

**4. Open the dashboard:**

Navigate to `http://localhost:8080` in your browser.

## Configuration

Key settings in `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `chat.max-servers` | `5` | Number of concurrent server workers |
| `spring.datasource.url` | `jdbc:h2:file:./chatapp` | H2 database location |


## How It Works

When a client joins, the following flow occurs:
```
Client → POST /servers/join
       → Session created (status: WAITING)
       → Added to LinkedBlockingQueue
              ↓
       Worker thread frees up, dequeues next client
       → Checks if client has expired (isExpired())
       → If expired → marked LOST, worker loops back for next client
       → If valid   → Session updated (status: ASSIGNED)
       → Worker blocks on CountDownLatch

Client → POST /servers/finish
       → Session updated (status: FINISHED)
       → CountDownLatch released
       → Worker clears server slot + updates stats
       → Worker loops back for next client in queue

Note: If all workers are busy, expired clients remain in the queue
until a worker frees up — they are marked LOST at that point.
```




## Load Testing

Place `load_test.sh` and `clients.txt` in the same folder, then:
```bash
chmod +x load_test.sh

# default
./load_test.sh


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

| Metric                  | Value          |
|-------------------------|----------------|
| Wall Clock Time         | 58s            |
| Throughput              | ~1.4 clients/s |
| Max Concurrent Sessions | 5              |
| Theoretical Max Clients | ~95            |
| Efficiency              | ~85%           |

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



## Concurrent Serving

### Load Test Configuration

| Metric | Value |
|--------|-------|
| Total Clients | 81 |
| Chat Duration | 3s |
| Queue Timeout | 30,000ms |
| Batch Size | 100 |
| Servers | 5 |

### Results

| Metric | Value |
|--------|-------|
| Total Approached | 81 |
| Total Served | 81 |
| Total Lost | 0 |
| Wall Clock Time | 58s |
| Throughput | ~1.4 clients/s |
| Theoretical Max | ~95 |
| Efficiency | ~85% |

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

### Logging Off
```shell

logging.level.root=OFF
logging.level.com.chatapp.chat_app=DEBUG
logging.file.name=archive/app.log
logging.level.org.springframework.web=DEBUG
```

### Test Configuration
- Servers: 5
- Clients: 81
- Chat Duration: 3s

### Results

| Metric | Logging Enabled | Logging Disabled |
|--------|-----------------|-----------------|
| Wall Clock Time | 59s             | 58s |
| Throughput | ~1.37/s         | ~1.4/s |
| Total Served | 81              | 81 |
| Total Lost | 0               | 0 |

- File logging had slight impact on overall performance. 

### Problem
By default, Logback writes logs **synchronously** — every `logger.info()` call 
blocks the calling worker thread until the entry is flushed to disk. Under 
concurrent load, this means all 5 worker threads compete for disk access, 
introducing latency on every client assignment and completion.

### Solution — Async Logging
Switching to `AsyncAppender` decouples disk writes from worker threads entirely:


## Async Logging vs Sync Logging


## Logging Performance Comparison (1000 Clients, 20 Servers)

### Results

| Metric | Sync Logging | Async Logging |
|--------|--------------|---------------|
| Wall Clock Time | 231s | 229s |
| Throughput | ~4.3/s | ~4.4/s |

Asynchronous logging performed better than sync logging . 


