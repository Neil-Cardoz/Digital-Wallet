import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class WalletApp {
    private static final String URL = "jdbc:mysql://localhost:3306/nebula_wallet_nofk";
    private static final String USER = "root";
    private static final String PASS = "*****";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in);
             Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            while (true) {
                System.out.println("\n=== Wallet Menu ===");
                System.out.println("1) Login");
                System.out.println("2) Register");
                System.out.println("3) Exit");
                System.out.print("Select> ");
                String option = sc.nextLine().trim();

                switch (option) {
                    case "1":
                        try {
                            Integer userId = login(sc, conn);
                            if (userId != null) {
                                userMenu(sc, conn, userId);
                            } else {
                                System.out.println("Invalid credentials.");
                            }
                        } catch (Exception e) {
                            System.out.println("Login failed: " + e.getMessage());
                        }
                        break;
                    case "2":
                        try {
                            registerUser(sc, conn);
                        } catch (Exception e) {
                            System.out.println("Registration failed: " + e.getMessage());
                        }
                        break;
                    case "3":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private static Integer login(Scanner sc, Connection conn) throws SQLException {
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String pw = sc.nextLine().trim();

        String sql = "SELECT user_id FROM users WHERE email=? AND password=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, pw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    System.out.println("Login successful! User ID: " + userId);
                    return userId;
                }
            }
        }
        return null;
    }

    private static void userMenu(Scanner sc, Connection conn, int userId) {
        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1) Deposit");
            System.out.println("2) Withdraw");
            System.out.println("3) View balance");
            System.out.println("4) Delete wallet");
            System.out.println("5) Logout");
            System.out.println("6) View transactions");

            System.out.print("Select> ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        deposit(sc, conn, userId);
                        break;
                    case "2":
                        withdraw(sc, conn, userId);
                        break;
                    case "3":
                        viewBalance(conn, userId);
                        break;
                    case "4":
                        deleteWallet(conn, userId);
                        return;
                    case "5":
                        System.out.println("Logged out.");
                        return;
                    case "6":
                        viewTransactions(conn, userId);
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void registerUser(Scanner sc, Connection conn) throws SQLException {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String pw = sc.nextLine().trim();

        String insertUser = "INSERT INTO users (name,email,password) VALUES (?,?,?)";
        try (PreparedStatement pu = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
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
        String insertW = "INSERT INTO wallets (wallet_id,user_id,balance) VALUES (?,?,0)";
        try (PreparedStatement pw = conn.prepareStatement(insertW)) {
            pw.setString(1, walletId);
            pw.setInt(2, userId);
            pw.executeUpdate();
            System.out.println("Wallet created with ID: " + walletId);
        }
    }

    private static void deposit(Scanner sc, Connection conn, int userId) {
        System.out.print("Amount to deposit: ");
        try {
            double amt = Double.parseDouble(sc.nextLine().trim());

            String sql = "UPDATE wallets SET balance = balance + ? WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, amt);
                ps.setInt(2, userId);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    String walletId = getWalletId(conn, userId);
                    if (walletId != null) {
                        System.out.println("Deposited ₹" + amt);
                        logTransaction(conn, walletId, amt, "DEPOSIT");
                    } else {
                        System.out.println("Failed to log transaction: Wallet ID not found.");
                    }
                } else {
                    System.out.println("Wallet not found.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        } catch (SQLException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private static void withdraw(Scanner sc, Connection conn, int userId) {
        System.out.print("Amount to withdraw: ");
        try {
            double amt = Double.parseDouble(sc.nextLine().trim());

            double bal = 0;
            String qry = "SELECT balance FROM wallets WHERE user_id=?";
            try (PreparedStatement pq = conn.prepareStatement(qry)) {
                pq.setInt(1, userId);
                try (ResultSet rs = pq.executeQuery()) {
                    if (rs.next()) {
                        bal = rs.getDouble("balance");
                    } else {
                        System.out.println("Wallet not found.");
                        return;
                    }
                }
            }

            if (amt > bal) {
                System.out.println("Insufficient funds (balance ₹" + bal + ")");
                return;
            }

            String upd = "UPDATE wallets SET balance = balance - ? WHERE user_id=?";
            try (PreparedStatement pw = conn.prepareStatement(upd)) {
                pw.setDouble(1, amt);
                pw.setInt(2, userId);
                int updated = pw.executeUpdate();
                if (updated > 0) {
                    String walletId = getWalletId(conn, userId);
                    if (walletId != null) {
                        System.out.println("Withdrew ₹" + amt);
                        logTransaction(conn, walletId, amt, "WITHDRAW");
                    } else {
                        System.out.println("Failed to log transaction: Wallet ID not found.");
                    }
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        } catch (SQLException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }


    private static void logTransaction(Connection conn, String walletId, double amt, String txnType) {
        String sql = "INSERT INTO transactions (wallet_id, amount, txn_type, txn_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, walletId);
            ps.setDouble(2, amt);
            ps.setString(3, txnType);
            ps.executeUpdate();
            System.out.println("Transaction logged: " + txnType + " ₹" + amt);
        } catch (SQLException e) {
            System.out.println("Failed to log transaction: " + e.getMessage());
        }
    }



    private static String getWalletId(Connection conn, int userId) {
        String sql = "SELECT wallet_id FROM wallets WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("wallet_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching wallet ID: " + e.getMessage());
        }
        return null;
    }


    private static void viewBalance(Connection conn, int userId) {
        String sql = "SELECT balance FROM wallets WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Current balance: ₹" + rs.getDouble("balance"));
                } else {
                    System.out.println("Wallet not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("View balance failed: " + e.getMessage());
        }
    }

    private static void deleteWallet(Connection conn, int userId) {
        String sql = "DELETE FROM wallets WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int deleted = ps.executeUpdate();
            if (deleted > 0)
                System.out.println("Wallet deleted.");
            else
                System.out.println("Wallet not found.");
        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }
    private static void viewTransactions(Connection conn, int userId) {
        String walletId = getWalletId(conn, userId);
        if (walletId == null) {
            System.out.println("No wallet found for the user.");
            return;
        }

        String sql = "SELECT txn_id, amount, txn_type, txn_time FROM transactions WHERE wallet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, walletId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n--- Transaction History ---");
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    int id = rs.getInt("txn_id");
                    double amount = rs.getDouble("amount");
                    String type = rs.getString("txn_type");
                    String txnTime = rs.getString("txn_time"); // Fetch txn_time (Datetime column)
                    System.out.printf("Txn ID: %d | ₹%.2f | Type: %s | Date: %s%n",
                            id, amount, type, txnTime);
                }
                if (!hasResults) {
                    System.out.println("No transactions found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch transactions: " + e.getMessage());
        }
    }




}
