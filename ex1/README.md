# ChatServer Concurrency Demo

A chat orchestration demo built with Spring Boot that manages client-server communication and concurrency





## Setup Instructions

## 1. Prerequisites

Before you begin, ensure you have the following installed on your machine:

### Required
- **Docker Compose** (v1.29+) 
- **Node.js** (v16+) 
- **npm** (v7+) 

### 2. Environment Configuration

For a demo, no .env is required



### 3. Backend Setup (Docker)

The backend runs in a Docker container. Build and start it with:

```bash
docker-compose up --build -d
```


**Verify backend is running:**
```bash
docker-compose ps
```

**View logs:**
```bash
docker-compose logs -f <container_id>
```

**Stop backend:**
```bash
docker-compose down
```

### 4. Frontend Setup (Next.js)

Navigate to the frontend directory and install dependencies:

```bash
cd frontend/chat-app
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will typically be available at `http://localhost:3000` (or as specified in your project).





## How It Works

When a client joins, the following flow occurs:



1. **Client Creation**  
   - A client is created by sending a request to `/clients`.

2. **Sending the First Message**  
   - The first message is sent via `/message/send`.  
   - This request **creates a session** for the client and assigns them to a server.  
   - multiple servers are available behind NGINX, the client is routed to the server with the **least current connections**.
    ```shell
    upstream chat_servers {
        least_conn;
    
        server chat-server-1:8081;
        server chat-server-2:8081;
        server chat-server-3:8081;
    }
    ```
3. **Sticky Session Routing**  
   - Subsequent requests from the same client **bypass NGINX routing**.  
   - The server is determined by the **session ID**, ensuring all requests for a session go to the same server.

4. **Server Queue and Load Management**  
   - If all servers are fully loaded, the client is placed in a **thread-safe queue** (`LinkedBlockingQueue`) and waits until a slot becomes available.  
   - Semaphore controls the total number of sessions that can pass through to nginx, preventing overload.  
   - When a slot frees up, the next client in the queue is signaled to proceed.
   - if a client in the queue is waiting for 5minutes
    ```java
            // enqueue and block message , until signal
            WaitingClient wc = serverManager.enqueueClient(clientId, session.getId());
    
            // after 5min it becomes lost and removed, if latch not released
            boolean ready = wc.getReadyLatch().await(300, TimeUnit.SECONDS);
    ```
5. **Request Processing**  
   - Each `/message/send` request (first or subsequent) is **simulated with a 5-second wait** in the server container, representing server-side processing.  
   - Subsequent messages do not go through the queue, since the session already holds a reserved thread on the server.

6. **Thread and Queue Management Summary**  
   - Queue ensures servers are **never overloaded**.  
   - Semaphore coordinates session slots.  
   - Sticky sessions guarantee all requests for a client go to the **same server**, maintaining session consistency.


# Load Test Report: Iterative vs Concurrent vs Forking (Spring Boot)

## Overview

This test compares three execution models:

- **Iterative mode** (single-threaded)
- **Concurrent mode** (single instance, multi-threaded)
- **Concurrent with Forking** (3 instances behind a load balancer)

Each test attempted **100 requests** across:
- create client
- first message ( 5 seconds )
- second message ( 5 seconds )
- finish session

---

## Iterative Results (Single-threaded)

 **Test did NOT complete successfully**

- Many requests timed out (~310s)
- System could not keep up with load

**Summary:**

- Avg response time: **169,852 ms**
- Throughput: **0.53 req/sec**
- Error rate: **54.25%**

**Key issues:**

- Requests queued up (only 1 processed at a time)
- High timeouts → test effectively stalled
- Not all requests completed

---

## Concurrent Results (Single Instance)

 **Test completed successfully**

**Summary:**

- Avg response time: **6,156 ms**
- Throughput: **7.33 req/sec**
- Error rate: **0%**

---

## Concurrent Results (Forking / 3 Instances)

 **Test completed successfully**

**Summary:**

- Avg response time: **3,250 ms**
- Throughput: **10.06 req/sec**
- Error rate: **0%**

---

## Comparison

| Metric | Iterative | 1 Server | 3 Servers (Forked) |
|--------|---------|---------|------------------|
| Completed |  No |  Yes |  Yes |
| Avg Time | 169,852 ms | 6,156 ms | **3,250 ms** |
| Throughput | 0.53 req/s | 7.33 req/s | **10.06 req/s** |
| Errors | 54.25% | 0% | 0% |

---

## Conclusion

- Iterative mode **fails under load** and cannot complete all requests
- Single-instance concurrency handles load reliably with multithreading
- Forking (multiple instances) further **reduces latency**, the limiting factor becomes the database operations within the main springboot application.

# Logging

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

| Metric          | Logging Enabled | Logging Disabled |
| --------------- | --------------- | ---------------- |
| Wall Clock Time | 59s             | 58s              |
| Throughput      | ~1.37/s         | ~1.4/s           |
| Total Served    | 81              | 81               |
| Total Lost      | 0               | 0                |

- File logging had slight impact on overall performance.

### Logging Problem

By default, Logback writes logs **synchronously** , every `logger.info()` call
blocks the calling worker thread until the entry is flushed to disk. Under
concurrent load, this means all 5 worker threads compete for disk access,
introducing latency on every client assignment and completion.

### Solution : Async Logging

Switching to `AsyncAppender` decouples disk writes from worker threads entirely:

## Async Logging vs Sync Logging

## Logging Performance Comparison (1000 Clients, 20 Servers)

### Results

| Metric          | Sync Logging | Async Logging |
| --------------- | ------------ | ------------- |
| Wall Clock Time | 231s         | 229s          |
| Throughput      | ~4.3/s       | ~4.4/s        |

Asynchronous logging performed better than sync logging .
