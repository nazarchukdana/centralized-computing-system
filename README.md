# Centralized Computing System (CCS) Server

## Overview
The **Centralized Computing System (CCS) Server** is a high-performance, multi-threaded server designed for **distributed computation**. It enables seamless **service discovery**, **client-server communication**, and **real-time statistics reporting**. The system operates using **UDP for service discovery** and **TCP for computation requests**, allowing multiple clients to connect and execute operations efficiently.

## Features
- **Service Discovery**: Clients can discover the CCS server dynamically over a local network via **UDP broadcasting**.
- **Client Communication**: Supports **multiple concurrent connections** for executing arithmetic computations using a **thread pool**.
- **Statistics Reporting**: Automatically tracks **client activity and computational requests** and reports statistics **every 10 seconds**.
- **Robust Error Handling**: Ensures network stability by handling **invalid requests**, **disconnections**, and **division by zero gracefully**.

---

## Functionalities
### 1. Service Discovery
- Implements a **broadcast mechanism** to allow clients to locate the CCS server.
- Listens for **UDP service discovery messages (`CCS DISCOVER`)** and responds with `CCS FOUND`.
- **Invalid discovery messages** are ignored to prevent errors.

### 2. Client Communication
- Supports the following **arithmetic operations**:
  - **Addition (ADD)**
  - **Subtraction (SUB)**
  - **Multiplication (MUL)**
  - **Division (DIV)**
- Clients send requests using the format:
  ```plaintext
  <OPER> <ARG1> <ARG2>
  ```
- Handles **multiple simultaneous client requests** using **multi-threading**.

### 3. Statistics Reporting
- Automatically logs system activity **every 10 seconds**, displaying:
  - Total **connected clients**
  - **Count of processed operations** (ADD, SUB, MUL, DIV)
  - **Number of invalid operations**
  - **Sum of computed results**
- Uses a **dedicated scheduler thread** for efficient reporting.

---

## Installation & Setup
1. **Clone the repository:**
   ```sh
   git clone <repository_url>
   cd <repository_directory>
   ```
2. **Compile the project** (if applicable):
   ```sh
   javac CCS.java
   ```
3. **Run the CCS server** specifying a port:
   ```sh
   java CCS <port>
   ```

---

## Methods & Implementation
### `CCS(int port)`
- Initializes the server and sets up **data structures for statistics tracking**.

### `void start()`
- Starts the **UDP listener, TCP server**, and statistics reporting thread.

### `void startUDPListener()`
- Handles **service discovery** requests over UDP.

### `void startTCPServer()`
- Accepts **client connections** and assigns them to available threads for processing.

### `void handleClient(Socket clientSocket)`
- Processes **client computation requests** and updates statistics.

### `int performOperation(String operation, int arg1, int arg2)`
- Executes the requested **arithmetic operation** and returns the result.

### `void reportStatistics()`
- Prints **global statistics** and **last-10-second statistics**.

### `void changeStats(Stats stat, int value)`
- Updates a specific **statistical metric safely**.

### `void incrementStats(Stats stat)`
- Increments a specific statistic by 1.

---

## Error Handling
- **Malformed UDP discovery messages** are ignored.
- **Handles client disconnections** gracefully to prevent crashes.
- **Catches and logs exceptions**, such as division by zero and invalid input formats.

---

## Known Issues
- **None observed** during testing.

---

## Contributors
- **Author:** Dana Nazarchuk
- Developed as a **school project at the Polish-Japanese Academy of Information Technologies**.

## Additional Information
The **CCS Server** demonstrates a **distributed computing architecture**, showcasing expertise in **multi-threading, network programming, and system monitoring**. This project can be extended with additional computation capabilities, security enhancements, or performance optimizations.

