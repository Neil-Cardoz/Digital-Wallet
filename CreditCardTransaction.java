public class CreditCardTransaction extends Transaction {
    private double interestRate;   // Interest rate for the credit card transaction

    // Constructor with interest rate
    public CreditCardTransaction(int id, int userId, double amount, String category, String date, double interestRate, Wallet wallet) throws NegativeAmountException, InsufficientFundsException, SpendingLimitExceededException, NullTransactionException {
        super(id, userId, amount, category, date, wallet);

        // Validate interest rate (optional check for logic, but not provided in the exceptions)
        if (interestRate < 0) {
            throw new NegativeAmountException("Interest rate cannot be negative.");
        }

        this.interestRate = interestRate;
    }

    // Get the interest rate
    public double getInterestRate() {
        return interestRate;
    }

    // Calculate the final amount with interest
    public double calculateFinalAmount() {
        return getAmount() + (getAmount() * interestRate / 100);
    }

    @Override
    public String toString() {
        return super.toString() + ", Interest Rate=" + interestRate + "%";
    }
}
