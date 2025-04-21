import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private static List<User> userDatabase = new ArrayList<>();
    private static int userIdCounter = 1; // Start user ID from 1

    private int userId;
    private String name;
    private String email;
    private String password;
    private Wallet wallet;
    private boolean isActive;
    private LocalDateTime lastLoginTime;

    public User(int userId, String name, String email, String password) {
        this.userId = userIdCounter++;
        this.name = name;
        this.email = email;
        this.password = password;
        this.wallet = new Wallet(this);
        this.isActive = true;
        this.lastLoginTime = null;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Wallet getWallet() { return wallet; }
    public boolean isActive() { return isActive; }

    public void register() {
        for (User user : userDatabase) {
            if (user.getEmail().equalsIgnoreCase(this.email)) {
                System.out.println("Email already registered.");
                return;
            }
        }

        userDatabase.add(this);
        System.out.println("User registered successfully: " + email);
    }

    public static User login(String email, String password) {
        for (User user : userDatabase) {
            if (user.getEmail().equalsIgnoreCase(email) && user.password.equals(password)) {
                if (!user.isActive) {
                    System.out.println("Account is inactive. Contact admin.");
                    return null;
                }

                user.lastLoginTime = LocalDateTime.now();
                System.out.println("Login successful: " + email);
                return user;
            }
        }
        System.out.println("Login failed: Invalid email or password.");
        return null; // Throw exception for invalid user
    }

    // Auto-deactivate if inactive for over 30 days
    public void checkInactivity() {
        if (lastLoginTime != null) {
            Duration inactivity = Duration.between(lastLoginTime, LocalDateTime.now());
            if (inactivity.toDays() > 30) {
                this.isActive = false;
                System.out.println("User " + email + " deactivated due to inactivity.");
            }
        }
    }

    // Optional: manually deactivate/activate
    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
        this.lastLoginTime = LocalDateTime.now();
    }
}
