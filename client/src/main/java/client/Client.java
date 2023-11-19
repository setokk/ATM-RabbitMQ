package client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client {
    private static String IP_ADDRESS;

    static {
        try {
            IP_ADDRESS = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);

        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        // Declare reply queue
        channel.queueDeclare(Protocol.REPLY_QUEUE_NAME, false, false, false, null);

        // Declare exchanges
        channel.exchangeDeclare(Protocol.REQUEST_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Protocol.REPLY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // Bind queues to exchanges.
        // That means that each queue will be interested in messages
        // from the corresponding exchanges

        // Bind reply queue to reply exchange (ip address as binding key)
        channel.queueBind(Protocol.REPLY_QUEUE_NAME, Protocol.REPLY_EXCHANGE_NAME, IP_ADDRESS);

        boolean autoAck = false;
        channel.basicConsume(Protocol.REPLY_QUEUE_NAME,
                autoAck,
                new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String response = new String(body, StandardCharsets.UTF_8);
                channel.basicAck(envelope.getDeliveryTag(), false);

                Number result;

                boolean isBalance = false;
                if (response.contains(".")) {
                    result = Double.parseDouble(response);
                    isBalance = true;
                }
                else {
                    result = Integer.parseInt(response);
                }

                var message = "";
                if (isBalance)
                    message = "Balance is " + result.doubleValue();
                else
                    message = switch (result.intValue()) {
                        case StatusCode.OK -> "Successful!";
                        case StatusCode.BAD_REQUEST -> "Request body is invalid...";
                        case StatusCode.USER_NOT_FOUND -> "User was not found...";
                        case StatusCode.INSUFFICIENT_BALANCE -> "Insufficient balance...";
                        default -> "Unrecognized status code";
                    };

                System.out.println("Status: " + message);

                System.out.println("Enter c to continue, otherwise enter any other key if you wish to exit...");
                var answer = new Scanner(System.in).nextLine();
                if (!answer.equalsIgnoreCase("c"))
                    System.exit(0);

                prepareAndSendRequest(channel);
            }
        });

        prepareAndSendRequest(channel);
    }

    public static void prepareAndSendRequest(Channel channel) throws IOException {
        // Run menu once.
        // Since RabbitMQ runs asynchronously, we output results when they arrive.
        // We don't wait for the results to arrive.
        // So that's why we don't have a while loop
        // We get the input again when the client wishes to continue again in the async call
        var rm = new RequestManager(channel, IP_ADDRESS);

        // Show menu and get code
        int code = menu();
        clear_terminal();

        var request = "";
        if (code == Protocol.BALANCE) {
            request = code + ",1,1";
        }
        else {
            // Get amount if code is not balance
            double amount;
            do {
                System.out.print("Enter a non negative amount: ");
                Scanner in = new Scanner(System.in);
                amount = in.nextDouble();
            } while (amount < 0);
            request = code + ",1," + amount;
        }

        // Send IP Address as well with the message so that the
        // server can know which routing key to use
        // Remember: for direct exchanges, routing_key=binding_key
        // in order for the message to be delivered to the right client
        rm.send(request + "," + Client.IP_ADDRESS);
    }

    public static int menu() {
        int code;
        do {
            System.out.println("+--------------------+");
            System.out.println("|(0)----Withdraw-----|");
            System.out.println("|(1)----Deposit------|");
            System.out.println("|(2)----Balance------|");
            System.out.println("+--------------------+");
            System.out.print("Please select an option[0-2]: ");

            Scanner in = new Scanner(System.in);
            var input = in.nextLine();
            try {
                code = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                code = -1; // Set an invalid value
            }
        } while (code < 0 || code > 2);

        return code;
    }

    public static void clear_terminal() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }
}