// User class
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private static List<User> userDatabase = new ArrayList<>();
    private static int userIdCounter = 1;

    private int userId;
    private String name;
    private String email;
    private String password;
    private Wallet wallet;
    private boolean isActive;
    private LocalDateTime lastLoginTime;

    public User(String name, String email, String password) {
        this.userId = userIdCounter++;
        this.name = name;
        this.email = email;
        this.password = password;
        this.wallet = new Wallet(this);
        this.isActive = true;
        this.lastLoginTime = null;
    }

    public void register() throws EmailAlreadyRegisteredException {
        for (User user : userDatabase) {
            if (user.getEmail().equalsIgnoreCase(this.email)) {
                throw new EmailAlreadyRegisteredException("Email already registered: " + email);
            }
        }
        userDatabase.add(this);
        System.out.println("User registered successfully: " + email);
    }

    public static User login(String email, String password)
            throws AuthenticationException, InactiveAccountException {
        for (User user : userDatabase) {
            if (user.getEmail().equalsIgnoreCase(email) && user.password.equals(password)) {
                if (!user.isActive) {
                    throw new InactiveAccountException("Account is inactive. Contact admin.");
                }
                user.lastLoginTime = LocalDateTime.now();
                System.out.println("Login successful: " + email);
                return user;
            }
        }
        throw new AuthenticationException("Invalid email or password.");
    }

    public void checkInactivity() {
        if (lastLoginTime != null) {
            Duration inactivity = Duration.between(lastLoginTime, LocalDateTime.now());
            if (inactivity.toDays() > 30) {
                this.isActive = false;
                System.out.println("User " + email + " deactivated due to inactivity.");
            }
        }
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
        this.lastLoginTime = LocalDateTime.now();
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Wallet getWallet() { return wallet; }
    public boolean isActive() { return isActive; }
}