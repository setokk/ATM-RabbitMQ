package client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class RequestManager {
    private final Channel channel;
    private final String ipAddress;

    public RequestManager(Channel channel, String ipAddress) {
        this.channel = channel;
        this.ipAddress = ipAddress;
    }

    public void send(String request) throws IOException {
        // Publish with "server" as routing key
        channel.basicPublish(Protocol.REQUEST_EXCHANGE_NAME,
                "server",
                null,
                request.getBytes(StandardCharsets.UTF_8));
    }
}
