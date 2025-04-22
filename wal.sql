-- 1) Create a fresh database
CREATE DATABASE nebula_wallet_nofk;
USE nebula_wallet_nofk;

-- 2) Users table
CREATE TABLE users (
  user_id          INT AUTO_INCREMENT PRIMARY KEY,
  name             VARCHAR(100)    NOT NULL,
  email            VARCHAR(100)    NOT NULL UNIQUE,
  password         VARCHAR(255)    NOT NULL,
  is_active        BOOLEAN         NOT NULL DEFAULT TRUE,
  last_login_time  DATETIME        NULL
);

-- 3) Wallets table (no FK constraints)
CREATE TABLE wallets (
  wallet_id       CHAR(36)        PRIMARY KEY,
  user_id         INT             NOT NULL,
  balance         DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
  spending_limit  DECIMAL(15,2)   NOT NULL DEFAULT 9999999999.99
);

-- 4) Transactions table (no FK constraints)
CREATE TABLE transactions (
  txn_id    INT AUTO_INCREMENT PRIMARY KEY,
  wallet_id CHAR(36)           NOT NULL,
  amount    DECIMAL(15,2)      NOT NULL,
  txn_type  ENUM('DEPOSIT','WITHDRAW') NOT NULL,
  txn_time  DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP
);
