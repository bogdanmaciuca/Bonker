package com.bonker.service;

import com.bonker.model.Client;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

public class ClientService {
    private static ClientService instance;
    private final Set<Client> clients = new HashSet<>();

    private ClientService() {}

    public static ClientService getInstance() {
        if (instance == null) {
            instance = new ClientService();
        }
        return instance;
    }

    public void registerClient(Client client) {
        clients.add(client);
    }

    public void removeClient(String idNumber) {
        clients.removeIf(c -> c.getIdNumber().equals(idNumber));
    }

    public List<Client> findByName(String firstName, String lastName) {
        return clients.stream().filter(c -> c.getFirstName().equals(firstName) && c.getLastName().equals(lastName)).toList();
    }

    public Optional<Client> findByIdentityNumber(String id) {
        return clients.stream()
            .filter(c -> c.getIdNumber().equals(id))
            .findFirst();
    }

    public Set<Client> getAllClients() {
        return clients;
    }
}
