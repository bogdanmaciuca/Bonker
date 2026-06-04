package com.bonker.service;

import com.bonker.model.Client;
import com.bonker.repository.ClientRepository;
import java.util.List;
import java.util.Optional;

public class ClientService {
    private static ClientService instance;
    private final ClientRepository clientRepository = new ClientRepository();

    private ClientService() {}

    public static ClientService getInstance() {
        if (instance == null) {
            instance = new ClientService();
        }
        return instance;
    }

    public void registerClient(Client client) {
        clientRepository.save(client);
    }

    public void removeClient(String idNumber) {
        clientRepository.delete(idNumber);
    }

    public List<Client> findByName(String firstName, String lastName) {
        return clientRepository.findAll().stream().filter(
            c -> c.getFirstName().equals(firstName) && c.getLastName().equals(lastName)
        ).toList();
    }

    public Optional<Client> findByIdentityNumber(String id) {
        return clientRepository.findById(id);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
}
