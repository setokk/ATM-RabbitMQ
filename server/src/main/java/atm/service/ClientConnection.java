package atm.service;

import atm.Server;
import atm.db.DatabaseDriver;
import com.rabbitmq.client.Channel;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static atm.service.Protocol.REPLY_EXCHANGE_NAME;

public class ClientConnection implements Runnable {
    private final Channel channel;
    private String ipAddress;
    private final byte[] body;

    public ClientConnection(Channel channel,
                            String ipAddress,
                            byte[] body) {
        this.channel = channel;
        this.ipAddress = ipAddress;
        this.body = body;
    }

    @Override
    public void run() {
        try {
            System.out.println("Processing request from: " + ipAddress);
            final String clientMessage = new String(body, StandardCharsets.UTF_8);
            final String error = "Error status code " + StatusCode.BAD_REQUEST + ". "
                    + "There is a problem with your request syntax. "
                    + "Correct syntax: <code: int [0-2]>,<userID: int [positive]>,<amount: double [positive]>.";

            ClientData data = Protocol.processRequest(clientMessage);
            if (data.hasError()) {
                channel.basicPublish(REPLY_EXCHANGE_NAME,
                        ipAddress,
                        null,
                        error.getBytes(StandardCharsets.UTF_8));
                channel.close();
            }

            // Get DB config and connect to DB (Dependency Injection)
            var db = new DatabaseDriver(Server.DB_CONFIG);

            // Check what atm.atm.service client requested
            switch (data.getCode()) {
                case Protocol.BALANCE -> {
                    double balance = db.balance(data.getUserID());
                    channel.basicPublish(REPLY_EXCHANGE_NAME,
                            ipAddress,
                            null,
                            String.valueOf(balance).getBytes(StandardCharsets.UTF_8));
                }
                case Protocol.WITHDRAW -> {
                    int status = db.withdraw(data.getUserID(), data.getAmount());
                    channel.basicPublish(REPLY_EXCHANGE_NAME,
                            ipAddress,
                            null,
                            String.valueOf(status).getBytes(StandardCharsets.UTF_8));
                }
                case Protocol.DEPOSIT -> {
                    int status = db.deposit(data.getUserID(), data.getAmount());
                    channel.basicPublish(REPLY_EXCHANGE_NAME,
                            ipAddress,
                            null,
                            String.valueOf(status).getBytes(StandardCharsets.UTF_8));
                }
            }

            // Close client socket (stateless)
            channel.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
