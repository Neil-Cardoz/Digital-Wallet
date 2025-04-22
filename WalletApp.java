import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class WalletApp {
    // === Configure these ===
    private static final String URL  = "jdbc:mysql://localhost:3306/nebula_wallet_nofk?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "A1s2D3f4@1"; // Put your password here

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== Wallet Menu ===");
                System.out.println("1) Register new user");
                System.out.println("2) Deposit");
                System.out.println("3) Withdraw");
                System.out.println("4) View balance");
                System.out.println("5) Exit");
                System.out.print("Select> ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1": registerUser(sc);          break;
                    case "2": deposit(sc);               break;
                    case "3": withdraw(sc);              break;
                    case "4": viewBalance(sc);           break;
                    case "5": System.out.println("Goodbye!"); return;
                    default:  System.out.println("Invalid choice.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void registerUser(Scanner sc) throws SQLException {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String pw = sc.nextLine().trim();

        String insertUser = "INSERT INTO users (name,email,password) VALUES (?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pu = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
            pu.setString(1, name);
            pu.setString(2, email);
            pu.setString(3, pw);
            pu.executeUpdate();

            try (ResultSet rs = pu.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    createWallet(conn, userId);
                    System.out.println("Registered! Your user_id = " + userId);
                }
            }
        }
    }

    private static void createWallet(Connection conn, int userId) throws SQLException {
        String walletId = UUID.randomUUID().toString();
        String insertW = "INSERT INTO wallets (wallet_id,user_id) VALUES (?,?)";
        try (PreparedStatement pw = conn.prepareStatement(insertW)) {
            pw.setString(1, walletId);
            pw.setInt(2, userId);
            pw.executeUpdate();
            System.out.println("Wallet created with ID: " + walletId);
        }
    }

    private static void deposit(Scanner sc) throws SQLException {
        System.out.print("Wallet ID: ");
        String wId = sc.nextLine().trim();
        System.out.print("Amount to deposit: ");
        double amt = Double.parseDouble(sc.nextLine().trim());

        if (amt <= 0) {
            throw new NegativeAmountException("Deposit amount must be positive.");
        }

        // Add transaction record
        String insertTxn = "INSERT INTO transactions (wallet_id, amount, type, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect()) {
            // Proceed to deposit and create transaction record
            String sql = "UPDATE wallets SET balance = balance + ? WHERE wallet_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, amt);
                ps.setString(2, wId);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    System.out.println("Deposited ₹" + amt);
                    // Add the transaction
                    String date = java.time.LocalDate.now().toString();
                    try (PreparedStatement txnPs = conn.prepareStatement(insertTxn)) {
                        txnPs.setString(1, wId);
                        txnPs.setDouble(2, amt);
                        txnPs.setString(3, "Deposit");
                        txnPs.setString(4, date);
                        txnPs.executeUpdate();
                    }
                } else {
                    System.out.println("Wallet not found.");
                }
            }
        }
    }

    private static void withdraw(Scanner sc) throws SQLException {
        System.out.print("Wallet ID: ");
        String wId = sc.nextLine().trim();
        System.out.print("Amount to withdraw: ");
        double amt = Double.parseDouble(sc.nextLine().trim());

        if (amt <= 0) {
            throw new NegativeAmountException("Withdrawal amount must be positive.");
        }

        // Simple check: fetch balance first
        String qry = "SELECT balance, spending_limit FROM wallets WHERE wallet_id=?";
        try (Connection conn = connect();
             PreparedStatement pq = conn.prepareStatement(qry)) {
            pq.setString(1, wId);
            try (ResultSet rs = pq.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Wallet not found.");
                    return;
                }
                double bal = rs.getDouble("balance");
                double limit = rs.getDouble("spending_limit");

                if (amt > bal) {
                    throw new InsufficientFundsException("Insufficient funds (balance ₹" + bal + ")");
                }
                if (amt > limit) {
                    throw new SpendingLimitExceededException("Withdrawal exceeds spending limit.");
                }
            }

            // Proceed with withdrawal and record the transaction
            String upd = "UPDATE wallets SET balance = balance - ? WHERE wallet_id=?";
            try (PreparedStatement pw = conn.prepareStatement(upd)) {
                pw.setDouble(1, amt);
                pw.setString(2, wId);
                pw.executeUpdate();
                System.out.println("Withdrew ₹" + amt);

                // Add the transaction record
                String insertTxn = "INSERT INTO transactions (wallet_id, amount, type, date) VALUES (?, ?, ?, ?)";
                String date = java.time.LocalDate.now().toString();
                try (PreparedStatement txnPs = conn.prepareStatement(insertTxn)) {
                    txnPs.setString(1, wId);
                    txnPs.setDouble(2, amt);
                    txnPs.setString(3, "Withdrawal");
                    txnPs.setString(4, date);
                    txnPs.executeUpdate();
                }
            }
        }
    }

    private static void viewBalance(Scanner sc) throws SQLException {
        System.out.print("Wallet ID: ");
        String wId = sc.nextLine().trim();

        String sql = "SELECT balance FROM wallets WHERE wallet_id=?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Current balance: ₹" + rs.getDouble("balance"));
                } else {
                    System.out.println("Wallet not found.");
                }
            }
        }
    }
}

