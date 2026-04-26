package com.bonker.models;

import java.util.ArrayList;
import java.util.List;

public class Bank {
    private final String bankName;
    private final String swiftCode;
    private List<Client> clients;

    public Bank(String bankName, String swiftCode) {
        this.bankName = bankName;
        this.swiftCode = swiftCode;
        this.clients = new ArrayList<>();
    }

    public String getBankName() {
        return bankName;
    }

    public List<Client> getClients() {
        return clients;
    }
}
