package com.smartcity.parta;

enum GrievanceSeverity {
    LOW,
    MEDIUM,
    HIGH
}

class GrievanceRequest {
    private String complaintId;
    private String citizenName;
    private String description;
    private GrievanceSeverity severity;

    public GrievanceRequest(String complaintId, String citizenName, String description, GrievanceSeverity severity) {
        this.complaintId = complaintId;
        this.citizenName = citizenName;
        this.description = description;
        this.severity = severity;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public String getDescription() {
        return description;
    }

    public GrievanceSeverity getSeverity() {
        return severity;
    }
}

abstract class GrievanceHandler {
    protected GrievanceHandler nextHandler;

    public GrievanceHandler setNextHandler(GrievanceHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }
    public abstract void handleRequest(GrievanceRequest request);
}

class FrontlineDeskHandler extends GrievanceHandler {
    @Override
    public void handleRequest(GrievanceRequest request) {
        if (request.getSeverity() == GrievanceSeverity.LOW) {
            System.out.println("[Frontline Desk] Resolved complaint " + request.getComplaintId()
                    + " for " + request.getCitizenName()
                    + ": " + request.getDescription());
        } else if (nextHandler != null) {
            System.out.println("[Frontline Desk] Escalating complaint " + request.getComplaintId() + " to Department Supervisor...");
            nextHandler.handleRequest(request);
        } else {
            System.out.println("[Frontline Desk] No handler available to resolve complaint: " + request.getComplaintId());
        }
    }
}

class DepartmentSupervisorHandler extends GrievanceHandler {
    @Override
    public void handleRequest(GrievanceRequest request) {
        if (request.getSeverity() == GrievanceSeverity.MEDIUM) {
            System.out.println("[Supervisor] Resolved operational issue " + request.getComplaintId()
                    + " for " + request.getCitizenName()
                    + ": " + request.getDescription());
        } else if (nextHandler != null) {
            System.out.println("[Supervisor] Escalating complaint " + request.getComplaintId() + " to Municipal Manager...");
            nextHandler.handleRequest(request);
        } else {
            System.out.println("[Supervisor] No handler available to resolve complaint: " + request.getComplaintId());
        }
    }
}

class MunicipalManagerHandler extends GrievanceHandler {
    @Override
    public void handleRequest(GrievanceRequest request) {
        if (request.getSeverity() == GrievanceSeverity.HIGH) {
            System.out.println("[Municipal Manager] Resolved critical hazard " + request.getComplaintId()
                    + " for " + request.getCitizenName()
                    + ": " + request.getDescription());
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        } else {
            System.out.println("[Municipal Manager] Complaint " + request.getComplaintId() + " exceeds municipal threshold. Referred to Emergency Response.");
        }
    }
}

public class PartCTest {
    public static void main(String[] args) {
        System.out.println("=== SmartCity360: Citizen Grievance Escalation Workflow ===");

        GrievanceHandler frontlineDesk = new FrontlineDeskHandler();
        GrievanceHandler supervisor = new DepartmentSupervisorHandler();
        GrievanceHandler manager = new MunicipalManagerHandler();

        frontlineDesk.setNextHandler(supervisor).setNextHandler(manager);

        GrievanceRequest req1 = new GrievanceRequest("GRV-001", "Kamal Perera", "Streetlight blinking near main gate", GrievanceSeverity.LOW);
        GrievanceRequest req2 = new GrievanceRequest("GRV-002", "Sunil Silva", "Sewage blockage overflow in residential sector", GrievanceSeverity.MEDIUM);
        GrievanceRequest req3 = new GrievanceRequest("GRV-003", "Nimal Fernando", "Industrial gas leak alarm triggered near school", GrievanceSeverity.HIGH);

        System.out.println("\n--- Processing Request 1 (Low Severity) ---");
        frontlineDesk.handleRequest(req1);

        System.out.println("\n--- Processing Request 2 (Medium Severity) ---");
        frontlineDesk.handleRequest(req2);

        System.out.println("\n--- Processing Request 3 (High Severity) ---");
        frontlineDesk.handleRequest(req3);
    }
}