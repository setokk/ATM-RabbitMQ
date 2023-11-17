package atm.db;

import atm.service.StatusCode;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseDriver {
    private DBConfig config;

    public DatabaseDriver(DBConfig config) throws SQLException {
        this.config = config;
    }

    /**
     * Initializes DB and inserts 1 user ONLY, if no rows are present
     * in bankuser table
     * @throws SQLException
     */
    public void init() throws SQLException {
        var conn = DriverManager.getConnection(config.getURL(),
                config.getUsername(), config.getPassword());

        var statement = conn.createStatement();
        var query = "CREATE TABLE IF NOT EXISTS bankuser(" +
                        "id bigserial primary key," +
                        "username text unique not null," +
                        "password text not null," +
                        "balance double precision not null);" +
                    "INSERT INTO bankuser (username, password, balance) " +
                    "SELECT 'user', 'user', 25000 " +
                    "WHERE NOT EXISTS (SELECT 1 FROM bankuser);";
        statement.execute(query);
    }

    public int withdraw(long userID, double amount) throws SQLException {
        var conn = DriverManager.getConnection(config.getURL(),
                config.getUsername(), config.getPassword());

        var statement = conn.createStatement();
        var query = "SELECT balance FROM bankuser " +
                    "WHERE id="+userID+";";
        var rs = statement.executeQuery(query);

        /* Check if result set is empty */
        if (!rs.next())
            return StatusCode.USER_NOT_FOUND;

        double balance = rs.getDouble("balance");
        if (balance < amount)
            return StatusCode.INSUFFICIENT_BALANCE;

        statement.execute("UPDATE bankuser " +
                             "SET balance=balance-"+amount+" " +
                             "WHERE id="+userID+";");
        statement.close();

        return StatusCode.OK;
    }

    public int deposit(long userID, double amount) throws SQLException {
        var conn = DriverManager.getConnection(config.getURL(),
                config.getUsername(), config.getPassword());

        var statement = conn.createStatement();
        var query = "SELECT balance FROM bankuser " +
                "WHERE id="+userID+";";
        var rs = statement.executeQuery(query);

        /* Check if result set is empty */
        if (!rs.next())
            return StatusCode.USER_NOT_FOUND;

        statement.execute("UPDATE bankuser " +
                             "SET balance=balance+" + amount + " " +
                             "WHERE id="+userID+";");
        statement.close();

        return StatusCode.OK;
    }

    public double balance(long userID) throws SQLException {
        var conn = DriverManager.getConnection(config.getURL(),
                config.getUsername(), config.getPassword());

        var statement = conn.createStatement();
        var query = "SELECT balance FROM bankuser " +
                    "WHERE id=" + userID + ";";

        var rs = statement.executeQuery(query);
        if (!rs.next())
            return StatusCode.USER_NOT_FOUND;

        double balance = rs.getDouble("balance");
        statement.close();

        return balance;
    }
}
