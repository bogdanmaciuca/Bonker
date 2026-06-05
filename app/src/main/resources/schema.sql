DROP TABLE IF EXISTS "transaction";
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS client;

CREATE TABLE client (
    id_number  TEXT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name  TEXT NOT NULL
);

CREATE TABLE account (
    iban TEXT PRIMARY KEY,
    client_id TEXT,
    currency  TEXT NOT NULL,
    balance   REAL NOT NULL DEFAULT 0,
    type      TEXT NOT NULL,
    interest  REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (client_id) REFERENCES client(id_number)
);

CREATE TABLE card (
    number       TEXT PRIMARY KEY,
    exp_date     TEXT NOT NULL,
    cvv          TEXT NOT NULL,
    account_iban TEXT NOT NULL,
    FOREIGN KEY (account_iban) REFERENCES account(iban)
);

CREATE TABLE "transaction" (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    account_iban TEXT NOT NULL,
    type         TEXT NOT NULL,
    amount       REAL NOT NULL,
    currency     TEXT NOT NULL,
    timestamp    TEXT NOT NULL,
    FOREIGN KEY (account_iban) REFERENCES account(iban)
);
