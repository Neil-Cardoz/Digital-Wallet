import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Wallet {
    private String walletId;
    private User owner;
    private double balance;
    private double spendingLimit;
    private final List<Transaction> transactionHistory;

    public Wallet(User owner) {
        this.walletId = UUID.randomUUID().toString();
        this.owner = owner;
        this.balance = 0.0;
        this.spendingLimit = Double.MAX_VALUE;
        this.transactionHistory = new ArrayList<>();
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new NegativeAmountException("Deposit amount must be positive.");
        }
        this.balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new NegativeAmountException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal.");
        }
        if (amount > spendingLimit) {
            throw new SpendingLimitExceededException("Amount exceeds spending limit.");
        }
        this.balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setSpendingLimit(double limit) {
        if (limit < 0) {
            throw new InvalidSpendingLimitException("Spending limit cannot be negative.");
        }
        this.spendingLimit = limit;
    }

    public boolean checkSpendingLimit(double amount) {
        return amount <= spendingLimit;
    }

    public void addTransaction(Transaction txn) {
        if (txn == null) {
            throw new NullTransactionException("Transaction cannot be null.");
        }
        transactionHistory.add(txn);
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public String getWalletId() {
        return walletId;
    }

    public User getOwner() {
        return owner;
    }
}
