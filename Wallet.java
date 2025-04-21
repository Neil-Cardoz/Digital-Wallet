import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Wallet {
    private String walletId;  // Unique Wallet ID
    private User owner;       // Reference to the owning user

    private double balance;
    private double spendingLimit;
    private List<Transaction> transactionHistory;

    public Wallet(User owner) {
        this.walletId = UUID.randomUUID().toString(); // Generate a unique wallet ID
        this.owner = owner;
        this.balance = 0.0;
        this.spendingLimit = Double.MAX_VALUE;
        this.transactionHistory = new ArrayList<>();
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        } // Throw exception for negative amount
    }

    public void withdraw(double amount) {
        if (amount <= balance && amount <= spendingLimit) {
            this.balance -= amount;
        }
        // Overspending or balance exceptions to be handled here
    }

    public double getBalance() {
        return balance;
    }

    public void setSpendingLimit(double limit) {
        this.spendingLimit = limit;
    } // Throw invalid spending limit here

    public boolean checkSpendingLimit(double amount) {
        return amount <= spendingLimit;
    } 

    public void addTransaction(Transaction txn) {
        transactionHistory.add(txn);
    } // Throw exception for null transaction

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