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
                            Map<String, Object> headers,
                            byte[] body) {
        this.channel = channel;
        if (headers != null && headers.containsKey("ip_address"))
            this.ipAddress = (String) headers.get("ip_address");
        this.body = body;
    }

    @Override
    public void run() {
        try {
            final String message = new String(body, StandardCharsets.UTF_8);
            final String error =
            ClientData data = Protocol.processRequest(message);
            if (data.hasError()) {
                channel.basicPublish(REPLY_EXCHANGE_NAME,
                        ipAddress,
                        null,
                        message.getBytes(StandardCharsets.UTF_8));
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
                            message.getBytes(StandardCharsets.UTF_8));
                }
                case Protocol.WITHDRAW -> {
                    int status = db.withdraw(data.getUserID(), data.getAmount());
                    output.println(status);
                }
                case Protocol.DEPOSIT -> {
                    int status = db.deposit(data.getUserID(), data.getAmount());
                    output.println(status);
                }
            }

            // Close client socket (stateless)
            clientSocket.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
