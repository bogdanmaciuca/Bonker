package com.bonker;

import com.bonker.model.CheckingAccount;
import com.bonker.model.SavingsAccount;
import com.bonker.model.Client;
import com.bonker.model.Currency;
import com.bonker.model.FixedTermSavingsAccount;
import com.bonker.exception.AccountNotFoundException;
import com.bonker.exception.InsufficientFundsException;
import com.bonker.model.Account;
import com.bonker.model.Card;
import com.bonker.service.AccountService;
import com.bonker.service.AuditService;
import com.bonker.service.ClientService;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class App {
    private static final AuditService auditService = AuditService.getInstance();
    private static final AccountService accountService = AccountService.getInstance();
    private static final ClientService clientService = ClientService.getInstance();
    private static final JTextArea output = new JTextArea(20, 60);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Bonking - Banking Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel buttons = new JPanel(new GridLayout(0, 2, 5, 5));
        buttons.add(btn("Register Client",     App::registerClient));
        buttons.add(btn("Open Account",        App::openAccount));
        buttons.add(btn("Deposit",             App::deposit));
        buttons.add(btn("Withdraw",            App::withdraw));
        buttons.add(btn("Transfer",            App::transfer));
        buttons.add(btn("Exchange Currency",   App::exchangeCurrency));
        buttons.add(btn("Transaction History", App::transactionHistory));
        buttons.add(btn("Apply Interest",      App::applyInterest));
        buttons.add(btn("Close Account",       App::closeAccount));
        buttons.add(btn("Total Balance",       App::totalBalance));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(output);

        frame.setLayout(new BorderLayout());
        frame.add(buttons, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        log("Application started.");
    }

    private static void registerClient() {
        auditService.log("Register client");

        String fn = input("First name:");
        if (fn == null) return;
        String ln = input("Last name:");
        if (ln == null) return;
        String id = input("ID number: ");
        if (id == null) return;

        Client c = new Client(fn, ln, id);
        clientService.registerClient(c);

        log("Registered: " + fn + " " + ln + " (" + id + ")");
    }

    private static void openAccount() {
        auditService.log("Open account");

        String iban = input("IBAN:");
        if (iban == null) return;
        String currencyCode = input("Currency code (RON/EUR):");
        if (currencyCode == null) return;
        String type = input("Account type (checking/savings/fixed):");
        if (type == null) return;
        String balanceStr = input("Initial balance:");
        if (balanceStr == null) return;

        Currency currency = new Currency(currencyCode.toUpperCase(), currencyCode.toUpperCase());
        BigDecimal balance = new BigDecimal(balanceStr);

        Account account;
        switch (type.toLowerCase()) {
            case "savings" -> {
                String rate = input("Interest rate:");
                if (rate == null) return;
                account = new SavingsAccount(iban, currency, new BigDecimal(rate), balance);
            }
            case "fixed"   -> account = new FixedTermSavingsAccount(iban, currency, balance);
            default        -> account = new CheckingAccount(iban, currency, balance);
        }

        accountService.addAccount(account);
        log("Opened account " + iban + " (" + type + ", " + currencyCode + ")");
    }

    private static void deposit() {
        auditService.log("Deposit");

        String iban   = input("IBAN:");
        if (iban == null) return;
        String amount = input("Amount:");
        if (amount == null) return;

        accountService.deposit(iban, new BigDecimal(amount));
        log("Deposited " + amount + " into " + iban);
    }

    private static void withdraw() {
        auditService.log("Withdraw");

        String iban   = input("IBAN:");
        if (iban == null) return;
        String amount = input("Amount:");
        if (amount == null) return;

        try {
            accountService.withdraw(iban, new BigDecimal(amount));
            log("Withdrew " + amount + " from " + iban);
        } catch (InsufficientFundsException e) {
            log("ERROR: " + e.getMessage());
        }
    }

    private static void transfer() {
        auditService.log("Transfer");

        String src   = input("Source IBAN:");
        if (src == null) return;
        String dst   = input("Destination IBAN:");
        if (dst == null) return;
        String amount = input("Amount:");
        if (amount == null) return;

        try {
            accountService.transfer(src, dst, new BigDecimal(amount));
            log("Transferred " + amount + " from " + src + " to " + dst);
        } catch (InsufficientFundsException | AccountNotFoundException e) {
            log("ERROR: " + e.getMessage());
        }
    }

    private static void exchangeCurrency() {
        auditService.log("Exchange currency");

        String iban  = input("IBAN:");
        if (iban == null) return;
        String newCur = input("New currency (EUR/USD):");
        if (newCur == null) return;
        String rate   = input("Exchange rate:");
        if (rate == null) return;

        accountService.exchangeCurrency(iban, new Currency(newCur.toUpperCase(), newCur.toUpperCase()), new BigDecimal(rate));
        log("Exchanged currency on " + iban + " to " + newCur + " at rate " + rate);
    }

    private static void transactionHistory() {
        auditService.log("Transaction History");

        String iban = input("IBAN:");
        if (iban == null) return;

        Account acc = accountService.getAccount(iban);
        if (acc == null) {
            log("ERROR: Account not found: " + iban);
            return;
        }

        log("--- Transaction history for " + iban + " ---");
        acc.getTransactions().forEach(t -> log("  " + t));
    }

    private static void applyInterest() {
        auditService.log("Apply interest");

        accountService.applyInterest();
        log("Interest applied to all savings accounts.");
    }

    private static void closeAccount() {
        auditService.log("Close account");

        String iban = input("IBAN to close:");
        if (iban == null) return;

        try {
            accountService.closeAccount(iban);
            log("Closed account " + iban);
        } catch (AccountNotFoundException | IllegalStateException e) {
            log("ERROR: " + e.getMessage());
        }
    }

    private static void totalBalance() {
        auditService.log("Total balance");

        String id = input("Client ID number:");
        if (id == null) return;

        clientService.findByIdentityNumber(id).ifPresentOrElse(
            client -> {
                BigDecimal total = accountService.calculateTotalBalance(client.getAccounts());
                log("Total balance for " + client.getFirstName() + " " + client.getLastName() + ": " + total);
            }, () -> log("ERROR: Client not found: " + id)
        );
    }

    private static JButton btn(String label, Runnable action) {
        JButton b = new JButton(label);
        b.addActionListener(e -> action.run());
        return b;
    }

    private static String input(String message) {
        return JOptionPane.showInputDialog(output.getTopLevelAncestor(), message);
    }

    private static void log(String message) {
        output.append(message + "\n");
    }

}

