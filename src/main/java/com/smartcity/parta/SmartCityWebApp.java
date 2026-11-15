package com.smartcity.parta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class SmartCityWebApp {

    private static final int PORT = 8080;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    private static ZoneGroup rootCityZone;

    public static void main(String[] args) {
        try {

            DatabaseManager.initDatabase();
            initCityHierarchy();
            initStreetlightPool();

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(10));

            server.createContext("/", new StaticFileHandler());


            server.createContext("/api/part-a", new PartAApiHandler());
            server.createContext("/api/part-b", new PartBApiHandler());
            server.createContext("/api/part-c", new PartCApiHandler());
            server.createContext("/api/part-d", new PartDApiHandler());
            server.createContext("/api/part-e", new PartEApiHandler());

            server.start();


            System.out.println(" SmartCity360 Web Dashboard & Design Patterns Platform Started! ");
            System.out.println(" Server URL   : http://localhost:" + PORT);
            System.out.println(" Embedded DB  : H2 Embedded (jdbc:h2:./smartcity360_db)");
            System.out.println(" REST APIs    : /api/part-a through /api/part-e");
        

        } catch (IOException e) {
            System.err.println("Failed to start SmartCityWebApp server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initCityHierarchy() {
        rootCityZone = new ZoneGroup("Metropolitan Smart City");


        ZoneGroup centralDistrict = new ZoneGroup("Central District");
        ZoneGroup galleRoad = new ZoneGroup("Galle Road Commercial Sector");
        galleRoad.add(new Building("Commercial Tower A", 850.0));
        galleRoad.add(new Building("Techhub Complex", 500.0));

        ZoneGroup hospitalSector = new ZoneGroup("Civic & Health Sector");
        hospitalSector.add(new Building("General Hospital", 850.0));
        hospitalSector.add(new Building("Municipal Council Hall", 320.0));

        centralDistrict.add(galleRoad);
        centralDistrict.add(hospitalSector);


        ZoneGroup residentialDistrict = new ZoneGroup("Residential District");
        residentialDistrict.add(new Building("Sunset Apartment", 850.0));
        residentialDistrict.add(new Building("Green Valley Residencies", 800.0));
        residentialDistrict.add(new Building("Lotus Eco Homes", 450.0));


        ZoneGroup industrialDistrict = new ZoneGroup("Harbor & Industrial District");
        industrialDistrict.add(new Building("Container Terminal Substation", 620.0));
        industrialDistrict.add(new Building("Cold Storage Logistics", 410.0));

        rootCityZone.add(centralDistrict);
        rootCityZone.add(residentialDistrict);
        rootCityZone.add(industrialDistrict);
    }

    private static void initStreetlightPool() {

        SharedStreetlightPool.getSharedStreetlight("LED-LUMEN-60W", 60, "4000K");
        SharedStreetlightPool.getSharedStreetlight("HIGH-MAST-150W", 150, "5000K");
        SharedStreetlightPool.getSharedStreetlight("SOLAR-HYBRID-80W", 80, "3000K");
    }


    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/web/index.html";
            } else if (!path.startsWith("/web/")) {
                path = "/web" + path;
            }


            byte[] bytes = null;
            try (InputStream in = getClass().getResourceAsStream(path)) {
                if (in != null) {
                    bytes = in.readAllBytes();
                }
            } catch (Exception ignored) {}

            if (bytes == null) {

                File localFile = new File("src/main/resources" + path);
                if (localFile.exists() && localFile.isFile()) {
                    bytes = java.nio.file.Files.readAllBytes(localFile.toPath());
                } else {
                    File targetFile = new File("target/classes" + path);
                    if (targetFile.exists() && targetFile.isFile()) {
                        bytes = java.nio.file.Files.readAllBytes(targetFile.toPath());
                    }
                }
            }

            if (bytes != null) {
                String contentType = "text/html; charset=UTF-8";
                if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
                else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
                else if (path.endsWith(".json")) contentType = "application/json; charset=UTF-8";
                else if (path.endsWith(".png")) contentType = "image/png";
                else if (path.endsWith(".svg")) contentType = "image/svg+xml";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1><p>Static resource not found: " + path + "</p>";
                byte[] notFoundBytes = notFound.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFoundBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFoundBytes);
                }
            }
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = gson.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static boolean handleOptionsPreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }


    static class PartAApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptionsPreflight(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {

                List<Map<String, Object>> logs = DatabaseManager.getTelemetryLogs();
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("logs", logs);
                sendJsonResponse(exchange, 200, resp);
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readRequestBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String rawData = json.has("data") ? json.get("data").getAsString() : "Voltage: 230V, Usage: 45.8kWh";
                    boolean encrypt = json.has("encryption") && json.get("encryption").getAsBoolean();
                    boolean compress = json.has("compression") && json.get("compression").getAsBoolean();
                    boolean authenticate = json.has("authentication") && json.get("authentication").getAsBoolean();

                    DataStream stream = new EnergyMeterDataStream();
                    List<String> appliedDecorators = new ArrayList<>();

                    if (encrypt) {
                        stream = new EncryptionDecorator(stream);
                        appliedDecorators.add("Encryption");
                    }
                    if (authenticate) {
                        stream = new AuthenticationDecorator(stream);
                        appliedDecorators.add("Authentication");
                    }
                    if (compress) {
                        stream = new CompressionDecorator(stream);
                        appliedDecorators.add("Compression");
                    }

                    String processedOutput = stream.writeData(rawData);
                    String decoratorNames = appliedDecorators.isEmpty() ? "None (Raw Stream)" : String.join(", ", appliedDecorators);

                    DatabaseManager.saveTelemetryLog(rawData, decoratorNames, processedOutput);

                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("rawData", rawData);
                    resp.put("decoratorsApplied", appliedDecorators);
                    resp.put("decoratorSummary", decoratorNames);
                    resp.put("processedOutput", processedOutput);

                    sendJsonResponse(exchange, 200, resp);
                } catch (Exception e) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("success", false);
                    err.put("error", e.getMessage());
                    sendJsonResponse(exchange, 400, err);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class PartBApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptionsPreflight(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                Map<String, Object> treeData = serializeComponent(rootCityZone);
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("totalPowerConsumption", rootCityZone.getPowerConsumption());
                resp.put("hierarchy", treeData);
                sendJsonResponse(exchange, 200, resp);
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readRequestBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String parentZoneName = json.get("parentZone").getAsString();
                    String type = json.has("type") ? json.get("type").getAsString() : "building";
                    String name = json.get("name").getAsString();

                    ZoneGroup targetZone = findZoneGroup(rootCityZone, parentZoneName);
                    if (targetZone == null) {
                        Map<String, Object> err = new LinkedHashMap<>();
                        err.put("success", false);
                        err.put("error", "Parent zone not found: " + parentZoneName);
                        sendJsonResponse(exchange, 404, err);
                        return;
                    }

                    if ("zone".equalsIgnoreCase(type)) {
                        targetZone.add(new ZoneGroup(name));
                    } else {
                        double power = json.get("power").getAsDouble();
                        targetZone.add(new Building(name, power));
                    }

                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("totalPowerConsumption", rootCityZone.getPowerConsumption());
                    resp.put("hierarchy", serializeComponent(rootCityZone));
                    sendJsonResponse(exchange, 200, resp);
                } catch (Exception e) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("success", false);
                    err.put("error", e.getMessage());
                    sendJsonResponse(exchange, 400, err);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private Map<String, Object> serializeComponent(SmartZoneComponent component) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (component instanceof Building) {
                Building b = (Building) component;
                map.put("type", "building");
                map.put("name", b.getName());
                map.put("power", b.getPowerConsumption());
            } else if (component instanceof ZoneGroup) {
                ZoneGroup zg = (ZoneGroup) component;
                map.put("type", "zone");
                map.put("name", zg.getName());
                map.put("power", zg.getPowerConsumption());
                List<Map<String, Object>> children = new ArrayList<>();
                for (SmartZoneComponent child : zg.getComponents()) {
                    children.add(serializeComponent(child));
                }
                map.put("children", children);
            }
            return map;
        }

        private ZoneGroup findZoneGroup(ZoneGroup root, String targetName) {
            if (root.getName().equalsIgnoreCase(targetName)) {
                return root;
            }
            for (SmartZoneComponent c : root.getComponents()) {
                if (c instanceof ZoneGroup) {
                    ZoneGroup found = findZoneGroup((ZoneGroup) c, targetName);
                    if (found != null) return found;
                }
            }
            return null;
        }
    }

    static class PartCApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptionsPreflight(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Map<String, Object>> tickets = DatabaseManager.getGrievanceTickets();
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("tickets", tickets);
                sendJsonResponse(exchange, 200, resp);
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readRequestBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String complaintId = json.has("complaintId") ? json.get("complaintId").getAsString() : "GRV-" + System.currentTimeMillis() % 10000;
                    String citizenName = json.has("citizenName") ? json.get("citizenName").getAsString() : "Anonymous Citizen";
                    String description = json.has("description") ? json.get("description").getAsString() : "General civic issue";
                    String severityStr = json.has("severity") ? json.get("severity").getAsString().toUpperCase() : "LOW";

                    GrievanceSeverity severity;
                    try {
                        severity = GrievanceSeverity.valueOf(severityStr);
                    } catch (IllegalArgumentException e) {
                        severity = GrievanceSeverity.LOW;
                    }

                    GrievanceRequest request = new GrievanceRequest(complaintId, citizenName, description, severity);


                    GrievanceHandler frontlineDesk = new FrontlineDeskHandler();
                    GrievanceHandler supervisor = new DepartmentSupervisorHandler();
                    GrievanceHandler manager = new MunicipalManagerHandler();
                    frontlineDesk.setNextHandler(supervisor).setNextHandler(manager);


                    ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
                    PrintStream originalOut = System.out;
                    List<String> escalationSteps = new ArrayList<>();
                    String resolvedBy = "Unknown";
                    String resolutionMessage = "";

                    try {
                        System.setOut(new PrintStream(outCapture));
                        frontlineDesk.handleRequest(request);
                    } finally {
                        System.setOut(originalOut);
                    }

                    String[] lines = outCapture.toString(StandardCharsets.UTF_8).split("\r?\n");
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (!trimmed.isEmpty()) {
                            escalationSteps.add(trimmed);
                            if (trimmed.startsWith("[Frontline Desk] Resolved")) {
                                resolvedBy = "Frontline Desk";
                                resolutionMessage = trimmed;
                            } else if (trimmed.startsWith("[Supervisor] Resolved")) {
                                resolvedBy = "Department Supervisor";
                                resolutionMessage = trimmed;
                            } else if (trimmed.startsWith("[Municipal Manager] Resolved")) {
                                resolvedBy = "Municipal Manager";
                                resolutionMessage = trimmed;
                            } else if (trimmed.contains("Emergency Response")) {
                                resolvedBy = "Emergency Response";
                                resolutionMessage = trimmed;
                            }
                        }
                    }

                    if (resolutionMessage.isEmpty() && !escalationSteps.isEmpty()) {
                        resolutionMessage = escalationSteps.get(escalationSteps.size() - 1);
                    }

                    DatabaseManager.saveGrievance(complaintId, citizenName, description, severity.name(), resolvedBy, resolutionMessage);

                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("complaintId", complaintId);
                    resp.put("citizenName", citizenName);
                    resp.put("description", description);
                    resp.put("severity", severity.name());
                    resp.put("resolvedBy", resolvedBy);
                    resp.put("resolutionMessage", resolutionMessage);
                    resp.put("escalationTrail", escalationSteps);

                    sendJsonResponse(exchange, 200, resp);
                } catch (Exception e) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("success", false);
                    err.put("error", e.getMessage());
                    sendJsonResponse(exchange, 400, err);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class PartDApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptionsPreflight(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Map<String, Object>> lights = DatabaseManager.getStreetlights();
                Map<String, Object> metrics = DatabaseManager.getFlyweightMetrics();

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("streetlights", lights);
                resp.put("metrics", metrics);
                sendJsonResponse(exchange, 200, resp);
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readRequestBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    if (json.has("simulateCount")) {

                        int count = json.get("simulateCount").getAsInt();
                        String[] models = {"LED-LUMEN-60W", "HIGH-MAST-150W", "SOLAR-HYBRID-80W"};
                        int[] wattages = {60, 150, 80};
                        String[] colorTemps = {"4000K", "5000K", "3000K"};
                        Random rnd = new Random();

                        for (int i = 0; i < count; i++) {
                            int idx = rnd.nextInt(models.length);
                            String model = models[idx];
                            int watt = wattages[idx];
                            String col = colorTemps[idx];
                            String id = "SL-SIM-" + String.format("%04d", i + 1);
                            double lat = 6.9200 + (rnd.nextDouble() * 0.03);
                            double lng = 79.8500 + (rnd.nextDouble() * 0.03);
                            int bright = 50 + rnd.nextInt(51);

                            Streetlight sl = new Streetlight(model, watt, col, id, lat, lng, bright);
                            SharedStreetlight shared = SharedStreetlightPool.getSharedStreetlight(model, watt, col);
                            String hash = "Shared@" + Integer.toHexString(System.identityHashCode(shared));

                            DatabaseManager.saveStreetlight(id, model, watt, col, lat, lng, bright, hash);
                        }

                        Map<String, Object> resp = new LinkedHashMap<>();
                        resp.put("success", true);
                        resp.put("message", "Simulated " + count + " streetlights successfully!");
                        resp.put("metrics", DatabaseManager.getFlyweightMetrics());
                        sendJsonResponse(exchange, 200, resp);
                        return;
                    }


                    String lightId = json.has("lightId") ? json.get("lightId").getAsString() : "SL-COL-" + String.format("%03d", (int)(Math.random() * 900 + 100));
                    String model = json.has("model") ? json.get("model").getAsString() : "LED-LUMEN-60W";
                    int wattage = json.has("wattage") ? json.get("wattage").getAsInt() : 60;
                    String colorTemp = json.has("colorTemp") ? json.get("colorTemp").getAsString() : "4000K";
                    double latitude = json.has("latitude") ? json.get("latitude").getAsDouble() : 6.9271;
                    double longitude = json.has("longitude") ? json.get("longitude").getAsDouble() : 79.8612;
                    int brightness = json.has("brightness") ? json.get("brightness").getAsInt() : 80;

                    Streetlight sl = new Streetlight(model, wattage, colorTemp, lightId, latitude, longitude, brightness);
                    SharedStreetlight shared = SharedStreetlightPool.getSharedStreetlight(model, wattage, colorTemp);
                    String sharedHash = "Shared@" + Integer.toHexString(System.identityHashCode(shared));

                    DatabaseManager.saveStreetlight(lightId, model, wattage, colorTemp, latitude, longitude, brightness, sharedHash);

                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("lightId", lightId);
                    resp.put("model", model);
                    resp.put("wattage", wattage);
                    resp.put("colorTemp", colorTemp);
                    resp.put("latitude", latitude);
                    resp.put("longitude", longitude);
                    resp.put("brightness", brightness);
                    resp.put("sharedHash", sharedHash);
                    resp.put("metrics", DatabaseManager.getFlyweightMetrics());

                    sendJsonResponse(exchange, 200, resp);
                } catch (Exception e) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("success", false);
                    err.put("error", e.getMessage());
                    sendJsonResponse(exchange, 400, err);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }


    static class PartEApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptionsPreflight(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {

                Map<String, Object> defaultScenario = evaluateQuotaRule(800, 120, 180, 350);
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("evaluation", defaultScenario);
                sendJsonResponse(exchange, 200, resp);
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readRequestBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    int quota = json.has("quota") ? json.get("quota").getAsInt() : 800;
                    int zoneA = json.has("zoneA") ? json.get("zoneA").getAsInt() : 120;
                    int zoneB = json.has("zoneB") ? json.get("zoneB").getAsInt() : 180;
                    int zoneC = json.has("zoneC") ? json.get("zoneC").getAsInt() : 350;

                    Map<String, Object> result = evaluateQuotaRule(quota, zoneA, zoneB, zoneC);
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("evaluation", result);
                    sendJsonResponse(exchange, 200, resp);
                } catch (Exception e) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("success", false);
                    err.put("error", e.getMessage());
                    sendJsonResponse(exchange, 400, err);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private Map<String, Object> evaluateQuotaRule(int quotaVal, int zoneAVal, int zoneBVal, int zoneCVal) {

            Expression sectorLoad = new NonTerminalExpression1(
                    new TerminalExpression(zoneAVal),
                    new TerminalExpression(zoneBVal)
            );
            int sectorLoadResult = sectorLoad.interpret();


            Expression totalCityLoad = new NonTerminalExpression1(
                    sectorLoad,
                    new TerminalExpression(zoneCVal)
            );
            int totalCityLoadResult = totalCityLoad.interpret();

            Expression remainingQuota = new NonTerminalExpression2(
                    new TerminalExpression(quotaVal),
                    totalCityLoad
            );
            int remainingQuotaMargin = remainingQuota.interpret();

            List<String> steps = new ArrayList<>();
            steps.add("Step 1: Evaluated Sector Load (Zone A [" + zoneAVal + " kW] + Zone B [" + zoneBVal + " kW]) = " + sectorLoadResult + " kW");
            steps.add("Step 2: Evaluated Metropolitan Grid Load (Sector Load [" + sectorLoadResult + " kW] + Zone C [" + zoneCVal + " kW]) = " + totalCityLoadResult + " kW");
            steps.add("Step 3: Evaluated Quota Margin (Municipal Quota [" + quotaVal + " kW] - Total Load [" + totalCityLoadResult + " kW]) = " + remainingQuotaMargin + " kW");

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("quota", quotaVal);
            res.put("zoneA", zoneAVal);
            res.put("zoneB", zoneBVal);
            res.put("zoneC", zoneCVal);
            res.put("sectorLoad", sectorLoadResult);
            res.put("totalCityLoad", totalCityLoadResult);
            res.put("remainingQuotaMargin", remainingQuotaMargin);
            res.put("isCompliant", remainingQuotaMargin >= 0);
            res.put("complianceStatus", remainingQuotaMargin >= 0 ? "WITHIN_QUOTA" : "QUOTA_EXCEEDED");
            res.put("steps", steps);


            Map<String, Object> astTree = new LinkedHashMap<>();
            astTree.put("operator", "NonTerminalExpression2 ( - )");
            astTree.put("rule", "Municipal Quota Margin");
            astTree.put("result", remainingQuotaMargin + " kW");

            Map<String, Object> leftNode = new LinkedHashMap<>();
            leftNode.put("type", "TerminalExpression");
            leftNode.put("label", "Allowed Municipal Quota");
            leftNode.put("value", quotaVal + " kW");

            Map<String, Object> rightNode = new LinkedHashMap<>();
            rightNode.put("operator", "NonTerminalExpression1 ( + )");
            rightNode.put("label", "Metropolitan Grid Load");
            rightNode.put("result", totalCityLoadResult + " kW");

            Map<String, Object> subLeftNode = new LinkedHashMap<>();
            subLeftNode.put("operator", "NonTerminalExpression1 ( + )");
            subLeftNode.put("label", "Sector Load (Zone A + Zone B)");
            subLeftNode.put("result", sectorLoadResult + " kW");

            Map<String, Object> subRightNode = new LinkedHashMap<>();
            subRightNode.put("type", "TerminalExpression");
            subRightNode.put("label", "Zone C (Industrial)");
            subRightNode.put("value", zoneCVal + " kW");

            rightNode.put("left", subLeftNode);
            rightNode.put("right", subRightNode);

            astTree.put("left", leftNode);
            astTree.put("right", rightNode);

            res.put("astTree", astTree);
            return res;
        }
    }
}
