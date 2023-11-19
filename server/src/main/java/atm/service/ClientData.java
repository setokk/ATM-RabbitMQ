package atm.service;

public class ClientData {
    private int code;
    private long userID;
    private double amount;
    private String ipAddress;

    public ClientData(int code,
                      long userID,
                      double amount,
                      String ipAddress) {
        this.code = code;
        this.userID = userID;
        this.amount = amount;
        this.ipAddress = ipAddress;
    }

    public boolean hasError() {
        return (this.code == -1);
    }

    public static ClientData error() {
        return new ClientData(-1, -1, -1, "");
    }

    public int getCode() {
        return code;
    }

    public long getUserID() {
        return userID;
    }

    public double getAmount() {
        return amount;
    }

    public String getIpAddress() { return ipAddress; }
}
