# Centralized Computing System (CCS) Server

## Overview
The **Centralized Computing System (CCS) Server** is a multi-threaded server designed to handle:
- **Service Discovery** (via UDP)
- **Client Computation Requests** (via TCP)
- **Periodic Statistics Reporting**

## Features
- **Service Discovery**: Enables clients to find the CCS server on a local network.
- **Client Communication**: Supports multiple concurrent connections for arithmetic computations.
- **Statistics Reporting**: Displays server activity statistics every 10 seconds.
- **Error Handling**: Ensures network stability and handles invalid requests gracefully.

---

## Functionalities
### 1. Service Discovery
- Uses a **broadcast mechanism** to allow clients to discover the CCS server.
- Listens for UDP discovery messages (`CCS DISCOVER`) and responds with `CCS FOUND`.
- Ignores malformed or invalid discovery messages.

### 2. Client Communication
- Supports arithmetic operations:
  - **Addition (ADD)**
  - **Subtraction (SUB)**
  - **Multiplication (MUL)**
  - **Division (DIV)**
- Clients send requests in the format: 
  ```plaintext
  <OPER> <ARG1> <ARG2>
  ```
- Handles multiple clients **concurrently** using a thread pool.

### 3. Statistics Reporting
- Maintains and reports statistics **every 10 seconds**, including:
  - Number of connected clients
  - Count of computed requests (ADD, SUB, MUL, DIV)
  - Count of incorrect operations
  - Sum of all computation results
- Uses a dedicated **scheduler thread** for periodic reporting.

---

## Installation & Setup
1. **Clone the repository:**
   ```sh
   git clone <repository_url>
   cd <repository_directory>
   ```
2. **Compile the project** (if applicable).
3. **Run the CCS server** specifying a port:
   ```sh
   java CCS <port>
   ```

---

## Methods & Implementation
### `CCS(int port)`
- Initializes the server and **tracks statistics**.

### `void start()`
- Starts the **UDP listener, TCP server**, and statistics reporting thread.

### `void startUDPListener()`
- Handles **service discovery** requests over UDP.

### `void startTCPServer()`
- Manages **client connections** and creates **threads for handling requests**.

### `void handleClient(Socket clientSocket)`
- Processes **client computation requests** and updates statistics.

### `int performOperation(String operation, int arg1, int arg2)`
- Executes the requested **arithmetic operation** and returns the result.

### `void reportStatistics()`
- Prints **global and last-10-second statistics**.

### `void changeStats(Stats stat, int value)`
- Modifies a **specific statistic safely**.

### `void incrementStats(Stats stat)`
- Increments a **specific statistic by 1**.

---

## Error Handling
- **Invalid UDP discovery messages** are ignored.
- **Client disconnections** are handled gracefully.
- **Exceptions** like division by zero and malformed requests are caught and logged.

---

## Known Issues
- **None observed** during testing.

---

## Contributors
- **Author:** Dana Nazarchuk
- Project is designed as a school project in Polish Japanese Academy of Information Technologies


