# UDP Fundraising Server and Client Application

A multi-threaded UDP-based fundraising system that allows multiple clients to create and donate to fundraising events on a centralized server. The application demonstrates concurrent programming, thread synchronization, and network communication using Java UDP sockets.

## Features

- **Multi-threaded Server**: Handles multiple concurrent clients using a fixed thread pool
- **Thread-safe Data Management**: Synchronized event management to prevent data races when multiple clients donate simultaneously
- **Create Fundraising Events**: Clients can create new fundraising events with a target amount and a deadline
- **Event Categorization**: Automatic categorization of events into "Current" (active) and "Past" (expired) based on deadline
- **Donation Processing**: Accept donations to fundraising events with real-time progress tracking
- **Comprehensive Logging**: Detailed logging of all requests, responses, and errors with timestamps and client information
- **Input Validation**: Robust validation of user inputs and exception handling
- **Clean Object Serialization**: Java object serialization for UDP message transmission

## Architecture

### Class Structure

**Server-Side Classes:**
- **UDPServer**: Main server that listens for incoming UDP requests and spawns handler threads
- **ClientHandler**: Handles individual client requests in a dedicated thread
- **RequestProcessor**: Validates and routes requests to appropriate handlers
- **FundManager**: Singleton that manages all fundraising events with thread-safe operations

**Client-Side Classes:**
- **UDPClient**: Handles UDP communication with the server
- **ClientUI**: Interactive menu-driven user interface

**Shared/Utility Classes:**
- **FundraisingEvent**: Data model representing a fundraising event with synchronized donation processing
- **Request**: Message structure for client requests
- **Response**: Message structure for server responses
- **RequestType**: Enum defining request types (CREATE_EVENT, LIST_CURRENT, LIST_PAST, DONATE)
- **MessageProtocol**: Serialization/deserialization utility for message transmission
- **Constants**: Configuration constants for the application

### Data Synchronization

Thread safety is achieved through:
1. **FundManager** - All public methods are `synchronized` to handle concurrent requests from multiple client threads
2. **FundraisingEvent** - The `donate()` method and all data access methods are `synchronized` to prevent race conditions when multiple clients donate to the same event
3. **Collections.synchronizedList** - Event list uses a synchronized list for internal data storage

## System Requirements

- Java 8 or higher
- Maven (for building)
- Command-line terminal/console

## Installation

No external dependencies required beyond Java standard library.

## Compilation

### Using Maven

```bash
cd c:\path\UDPClientServerApp
mvn clean compile
```

### Using javac (Manual)

```bash
# Compile all Java files
javac -d target/classes src/main/java/org/company/cs455project2/*.java
```

## Running the Application

### Start the Server

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="org.company.cs455project2.UDPServer"

# Using java directly
java -cp target/classes org.company.cs455project2.UDPServer
```

The server will start listening on port **9999** and display:
```
[TIMESTAMP] [SERVER] ========================================
[TIMESTAMP] [SERVER] UDP Fundraising Server Started
[TIMESTAMP] [SERVER] Server Port: 9999
[TIMESTAMP] [SERVER] Thread Pool Size: 10
[TIMESTAMP] [SERVER] Max Buffer Size: 4096 bytes
[TIMESTAMP] [SERVER] ========================================
[TIMESTAMP] [SERVER] Waiting for client requests...
```

### Start Client(s)

Open a **new terminal window** and run:

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="org.company.cs455project2.ClientUI"

# Using java directly
java -cp target/classes org.company.cs455project2.ClientUI

# Connect to a different server and/or port
java -cp target/classes org.company.cs455project2.ClientUI <serverIP> <serverPort>
```

Example with different server:
```bash
java -cp target/classes org.company.cs455project2.ClientUI 192.168.1.100 9999
```

## Usage Guide

Once a client is started, you'll see the main menu:

```
--- Main Menu ---
1. Create a new fundraising event
2. List current events
3. List past events
4. Donate to an event
5. Exit
```

### 1. Create a New Fundraising Event

1. Select option `1` from the menu
2. Enter the event name (max 100 characters)
3. Enter the target amount (must be > 0)
4. Enter the deadline in format: `yyyy-MM-dd HH:mm:ss.SSS`
   - Example: `2026-12-31 23:59:59.999`

Example:
```
--- Create New Event ---
Enter event name: School Building Fund
Enter target amount ($): 10000.00
Enter deadline (format: yyyy-MM-dd HH:mm:ss.SSS)
Example: 2026-12-31 23:59:59.999
Deadline: 2026-06-30 23:59:59.000

*** Success: Event created successfully ***
```

### 2. List Current Events

Select option `2` to view all active (non-expired) events.

Events are sorted by deadline (earliest first) and display:
- Event index (for donation reference)
- Event name
- Target amount
- Current donations collected
- Progress percentage
- Deadline

Example:
```
--- Current Events ---

[0] School Building Fund | Target: $10000.00 | Current: $2500.00 | Progress: 25.0% | Deadline: 2026-06-30 23:59:59.000
[1] Disaster Relief | Target: $50000.00 | Current: $15000.00 | Progress: 30.0% | Deadline: 2026-05-15 12:00:00.000
```

### 3. List Past Events

Select option `3` to view all expired events.

Past events are sorted by deadline (most recent first) and additionally show whether the target was reached.

### 4. Donate to an Event

1. Select option `4`
2. Choose event category (Current or Past)
3. View the list of available events
4. Enter the event index to donate to
5. Enter the donation amount ($0.01 - $1,000,000.00)

Example:
```
--- Donate to Event ---

Donate to which type of event?
1. Current event
2. Past event
Enter your choice: 1

[0] School Building Fund | Target: $10000.00 | Current: $2500.00 | Progress: 25.0% | Deadline: 2026-06-30 23:59:59.000

Enter event index: 0
Enter donation amount ($): 500.50

*** Success: Donation processed successfully ***
```

The response will show:
- Event name
- Donation amount
- New total collected
- Remaining amount needed
- Whether target is now reached
- Updated progress percentage

### 5. Exit

Select option `5` to disconnect from the server and close the client.

## Multiple Clients

Run multiple client instances in separate terminal windows. They can all:
- Create events simultaneously
- Donate to the same event concurrently (thread-safe)
- View the same event list
- See real-time updates of donations

Example with 3 clients:

**Terminal 1 (Server):**
```bash
java -cp target/classes org.company.cs455project2.UDPServer
```

**Terminal 2 (Client 1):**
```bash
java -cp target/classes org.company.cs455project2.ClientUI
```

**Terminal 3 (Client 2):**
```bash
java -cp target/classes org.company.cs455project2.ClientUI
```

**Terminal 4 (Client 3):**
```bash
java -cp target/classes org.company.cs455project2.ClientUI
```

Each client can create and donate independently while sharing the same server data.

## Logging

The application provides detailed logging:

**Server Logs** (Server Terminal):
```
[2026-03-29 14:23:45.123] [SERVER] UDP Fundraising Server Started
[2026-03-29 14:23:50.456] [CLIENT 127.0.0.1:54321] CLIENT CONNECTED
[2026-03-29 14:23:50.457] [CLIENT 127.0.0.1:54321] REQUEST RECEIVED: Create a new fundraising event
[2026-03-29 14:23:50.458] [CLIENT 127.0.0.1:54321] REQUEST PROCESSED: CREATE_EVENT - Status: 200
[2026-03-29 14:23:50.459] [CLIENT 127.0.0.1:54321] RESPONSE SENT: Event created successfully
[2026-03-29 14:23:50.460] [CLIENT 127.0.0.1:54321] CLIENT DISCONNECTED
```

**Client Logs** (Client Terminal):
```
[2026-03-29 14:23:50.451] [CLIENT] Connected to server at localhost:9999
[2026-03-29 14:23:50.457] [CLIENT] Sending request: CREATE_EVENT
[2026-03-29 14:23:50.458] [CLIENT] Response received: Event created successfully
```

**Error Logs** (Printed to stderr):
```
[2026-03-29 14:23:55.123] [ERROR] [CLIENT 127.0.0.1:54322] Failed to deserialize request: Invalid class descriptor
```

## Error Handling

The application handles various error scenarios:

1. **Invalid Input**: Users are prompted to re-enter data if validation fails
2. **Network Errors**: Timeouts and communication failures are caught and reported
3. **Server Errors**: All exceptions are caught and appropriate error responses are sent
4. **Data Validation**: 
   - Event names must be 1-100 characters
   - Target amounts must be > 0
   - Deadlines must be in the future
   - Donations must be between $0.01 and $1,000,000.00
   - Event indices must be valid

## Configuration Constants

Edit [Constants.java](src/main/java/org/company/cs455project2/Constants.java) to customize:

```java
public static final int SERVER_PORT = 9999;           // Server listening port
public static final int BUFFER_SIZE = 4096;           // UDP buffer size
public static final int SOCKET_TIMEOUT_MS = 30000;    // Client timeout (30 seconds)
public static final double MIN_DONATION_AMOUNT = 0.01;
public static final double MAX_DONATION_AMOUNT = 1000000.00;
public static final int MAX_EVENT_NAME_LENGTH = 100;
```

## Data Persistence

⚠️ **Note**: All event data is stored in RAM. When the server is restarted, all events and donations are lost. This is intentional for this educational project.

## Troubleshooting

### Server won't start
- Check if port 9999 is already in use
- Try a different port by editing `Constants.SERVER_PORT`
- Ensure firewall allows UDP on port 9999

### Client can't connect
- Verify server is running first
- Check that server IP address is correct
- Ensure both are on the same network (if not localhost)
- Check firewall settings

### No response from server
- Server may have crashed; check server terminal for errors
- Network may be congested; wait and retry
- Server timeout is 30 seconds; increase if needed in Constants

### Donation fails with "invalid event index"
- Event may have expired (check if it's still in current events)
- Index may be out of range; verify with list command first

## Performance Notes

- **Thread Pool Size**: Fixed at 10 threads; handles up to 10 concurrent clients actively
- **Buffer Size**: 4096 bytes per packet; adjust if messages are too large
- **RAM Usage**: Depends on number of events and total donations
- **Scalability**: Can handle 20+ clients with current configuration; increase thread pool for more

## Project Structure

```
UDPClientServerApp/
├── pom.xml
└── src/main/java/org/company/cs455project2/
    ├── Constants.java
    ├── RequestType.java
    ├── Request.java
    ├── Response.java
    ├── FundraisingEvent.java
    ├── FundManager.java
    ├── MessageProtocol.java
    ├── RequestProcessor.java
    ├── ClientHandler.java
    ├── UDPServer.java
    ├── UDPClient.java
    └── ClientUI.java
```

## Learning Outcomes

This project demonstrates:
- Multi-threaded server programming
- UDP socket communication
- Thread synchronization and data safety
- Java object serialization
- Request-response protocol design
- Input validation and error handling
- Interactive command-line user interface
- Concurrent access to shared resources

## Author

- [Efe Awo-Osagie](https://www.linkedin.com/in/efe-awo/)

## Licence:

This application served as the Author's Java TCP socket programming project 2 in CSCI455 (Networking and Parallel Computation) course. It is for educational purposes only. See your institution's guidelines for code sharing and reuse.
