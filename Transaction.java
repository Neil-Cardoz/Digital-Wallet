public class Transaction {
    private int id;             // Unique transaction ID
    private int userId;         // ID of the user associated with the transaction
    private double amount;      // Transaction amount
    private String category;    // Type or category of transaction (e.g. "FOOD", "TRAVEL")
    private String date;        // Date of transaction in string format (e.g. "2025-04-21")
    private Wallet wallet;      // Wallet associated with the transaction

    // Constructor
    public Transaction(int id, int userId, double amount, String category, String date, Wallet wallet) throws NegativeAmountException, InsufficientFundsException, NullTransactionException {
        if (wallet == null) {
            throw new NullTransactionException("Wallet cannot be null.");
        }

        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.wallet = wallet;

        // Validate transaction amount
        if (amount <= 0) {
            throw new NegativeAmountException("Transaction amount must be greater than zero.");
        }

        // Validate if the transaction exceeds the spending limit
        double spendingLimit = wallet.getSpendingLimit();  // Assuming wallet has a spending limit
        if (amount > spendingLimit) {
            throw new SpendingLimitExceededException("Transaction amount exceeds the spending limit.");
        }

        // Deduct amount from the wallet when the transaction occurs
        processTransaction();
    }

    // Deduct the amount from the wallet
    private void processTransaction() throws InsufficientFundsException {
        if (wallet.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient balance for this transaction.");
        }
        wallet.withdraw(amount); // Call withdraw from the wallet
        System.out.println("Transaction completed. Amount deducted: " + amount);
    }

    // Getter for amount to allow access in subclasses like CreditCardTransaction
    public double getAmount() {
        return amount;
    }

    // Getters for other fields
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    // String representation
    @Override
    public String toString() {
        return "Transaction{" +
                "ID=" + id +
                ", UserID=" + userId +
                ", Amount=" + amount +
                ", Category='" + category + '\'' +
                ", Date='" + date + '\'' +
                '}';
    }
}
