# SmartCity360 - Design Patterns Control Center & Web Platform

A lightweight, pure Java web frontend and embedded database platform for **SmartCity360** (JIAT/OODP II/EX/01) implementing five Gang of Four (GoF) Object-Oriented Design Patterns with **zero external server setup** (NO Spring Boot required).

---

## 🌟 Key Architecture

1. **Pure Java Embedded Web Server:**
   - Powered by standard library `com.sun.net.httpserver.HttpServer` on port `8080`.
   - Serves static assets (`src/main/resources/web/index.html`) and handles REST JSON API endpoints (`/api/part-a` through `/api/part-e`).

2. **Embedded H2 Database (`DatabaseManager.java`):**
   - Embedded JDBC storage (`jdbc:h2:./smartcity360_db;DB_CLOSE_DELAY=-1`).
   - Automatically initializes and manages tables:
     - `sensor_telemetry_logs`: Telemetry logs with applied security decorator layers (Part A).
     - `grievance_tickets`: Citizen complaints and resolving handlers (Part C).
     - `streetlights`: Registered streetlights and shared flyweight instance hashes (Part D).

3. **Modern Interactive Dark-Mode Dashboard (`index.html`):**
   - Built with HTML, Tailwind CSS (CDN), and Vanilla JS.
   - 5 dedicated tabs corresponding to Parts A–E:
     - **Part A (Decorator):** Interactive telemetry generator, decorator toggles (Encryption, Authentication, Compression), live pipeline flowchart, and H2 database audit table.
     - **Part B (Composite):** Interactive tree explorer showing dynamic recursive power calculations across districts, sectors, and buildings, with live form to add new buildings/zones to the active grid.
     - **Part C (Chain of Responsibility):** Lodge citizen complaints with LOW/MEDIUM/HIGH severity, observe real-time escalation path across `FrontlineDeskHandler` &rarr; `DepartmentSupervisorHandler` &rarr; `MunicipalManagerHandler`, and inspect persisted tickets.
     - **Part D (Flyweight):** Register new streetlights, inspect shared memory reference pointers (`Shared@hash`), and simulate 500+ lights to visually demonstrate 60%+ memory footprint reduction.
     - **Part E (Interpreter):** Interactive AST (Abstract Syntax Tree) visualizer evaluating municipal energy quota rules (`Allowed Quota - Total Grid Load`) and displaying compliance status.

---

## 🚀 How to Run

### Option 1: Double-Click or Run `run.bat`
```cmd
run.bat
```

### Option 2: IntelliJ IDEA or Eclipse
Open the project, navigate to:
`src/main/java/com/smartcity/parta/SmartCityWebApp.java`  
Right-click &rarr; **Run 'SmartCityWebApp.main()'**.

### Option 3: Maven Command Line
```cmd
mvn compile exec:java
```

Once started, open your browser at:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 📁 Project Structure

```
SmartCity360/
├── pom.xml                                 # Maven configuration with H2 & Gson
├── run.bat                                 # Quick startup batch script
├── README.md                               # Project documentation
├── smartcity360_db.mv.db                   # Persistent H2 embedded database
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── smartcity/
        │           └── parta/
        │               ├── PartATest.java          # Part A: Decorator Pattern
        │               ├── PartBTest.java          # Part B: Composite Pattern
        │               ├── PartCTest.java          # Part C: Chain of Responsibility Pattern
        │               ├── PartDTest.java          # Part D: Flyweight Pattern
        │               ├── PartETest.java          # Part E: Interpreter Pattern
        │               ├── DatabaseManager.java    # Embedded H2 JDBC Manager & Schema
        │               └── SmartCityWebApp.java    # Embedded HttpServer & REST Controller
        └── resources/
            └── web/
                └── index.html              # Futuristic Dark-Mode Control Center
```
