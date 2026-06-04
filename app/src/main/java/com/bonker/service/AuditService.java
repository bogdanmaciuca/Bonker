package com.bonker.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class AuditService {
    private static AuditService instance;
    private static final String FILE = "audit.csv";

    private AuditService() {}

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public synchronized void log(String action) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE, true))) {
            out.println(LocalDateTime.now() + ": " + action);
        } catch (IOException e) {
            System.err.println("Audit error: " + e.getMessage());
        }
    }
}
