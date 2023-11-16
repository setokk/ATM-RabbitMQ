package db;

/**
 * Class is used for providing the DatabaseDriver class with appropriate
 * atm.db configs, thus implementing dependency injection for easier testing
 */
public class DBConfig {
    private final String URL;
    private final String username;
    private final String password;

    public DBConfig(String URL,
                    String username,
                    String password) {
        this.URL = URL;
        this.username = username;
        this.password = password;
    }

    public String getURL() {
        return URL;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
