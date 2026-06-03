package com.bonker;

import com.bonker.model.CheckingAccount;
import com.bonker.model.SavingsAccount;
import com.bonker.model.Client;
import com.bonker.model.Currency;
import com.bonker.exception.InsufficientFundsException;
import com.bonker.model.Account;
import com.bonker.model.Card;
import com.bonker.service.AccountService;
import com.bonker.service.ClientService;

import java.math.BigDecimal;
import java.util.List;

public class App {
    public static void main(String[] args) {
        AccountService accountService = AccountService.getInstance();
        ClientService clientService = ClientService.getInstance();

        Currency ron = new Currency("RON", "Romanian Leu");
        Currency eur = new Currency("EUR", "Euro");

        Client client = new Client("John", "Doe", "1234567890123");
        clientService.registerClient(client);

        CheckingAccount checking = new CheckingAccount("RO01BONK001", ron, new BigDecimal("500.0"));
        SavingsAccount savings = new SavingsAccount("RO01BONK002", ron, new BigDecimal("0.05"), new BigDecimal("500.0"));

        accountService.addAccount(checking);
        accountService.addAccount(savings);
        client.getAccounts().add(checking);
        client.getAccounts().add(savings);

        accountService.deposit("RO01BONK001", new BigDecimal("1000.0"));
        accountService.deposit("RO01BONK002", new BigDecimal("500.0"));

        try {
            accountService.withdraw("RO01BONK001", new BigDecimal("200.0"));
        } catch (InsufficientFundsException e) {
            System.err.println(e.getMessage());
        }

        try {
            accountService.withdraw("RO01BONK002", new BigDecimal("300.0"));
        } catch (InsufficientFundsException e) {
            System.err.println(e.getMessage());
        }

        accountService.exchangeCurrency("RO01BONK002", eur, new BigDecimal("0.2"));

        System.out.println("Transaction history for checking account:");
        checking.getTransactions().forEach(System.out::println);

        accountService.applyInterest();

        BigDecimal total = accountService.calculateTotalBalance(client.getAccounts());
        System.out.println("Total balance for " + client.getFirstName() + " " + client.getLastName() + ": " + total);

        List<Account> sorted = accountService.getAccountsSortedByBalance(client.getAccounts());
        System.out.println("Accounts sorted by balance:");
        sorted.forEach(a -> System.out.println("IBAN: " + a.getIban() + " -> Balance " + a.getBalance() + a.getCurrency()));

        Card visa1 = new Card("4444-5555-6666-7777", "12/28", "123", "RO01BONK001");
        accountService.attachCard("RO01BONK001", visa1);
        Card visa2 = new Card("4444-5555-6666-8888", "12/28", "123", "RO01BONK001");
        accountService.attachCard("RO01BONK001", visa2);

        System.out.println("Numar carduri inainte: " + checking.getCards().size());
        accountService.removeCard("RO01BONK001", "4444-5555-6666-7777");
        System.out.println("Numar carduri dupa: " + checking.getCards().size());

        accountService.closeAccount("RO01BONK001");
    }
}

