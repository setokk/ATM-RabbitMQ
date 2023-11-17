package client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client {
    private static volatile boolean isConsumeComplete = false;

    public static void main(String[] args)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);

        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        // Declare queues
        channel.queueDeclare(Protocol.REQUEST_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(Protocol.REPLY_QUEUE_NAME, false, false, false, null);

        // Declare exchanges
        channel.exchangeDeclare(Protocol.REQUEST_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Protocol.REPLY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // Bind queues to exchanges.
        // That means that each queue will be interested in messages
        // from the corresponding exchanges
        String ipAddress = InetAddress.getLocalHost().getHostName();
        channel.queueBind(Protocol.REPLY_QUEUE_NAME, Protocol.REPLY_EXCHANGE_NAME, ipAddress);
        channel.queueBind(Protocol.REQUEST_QUEUE_NAME, Protocol.REQUEST_EXCHANGE_NAME, ipAddress);

        while (true) {
            var rm = new RequestManager(channel, ipAddress);
            Client.isConsumeComplete = false;

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
            rm.send(request);

            // Consume server message
            channel.basicConsume(Protocol.REPLY_QUEUE_NAME, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    String response = new String(body, StandardCharsets.UTF_8);
                    Number result;
                    if (response.contains("."))
                        result = Double.parseDouble(response);
                    else
                        result = Integer.parseInt(response);

                    var message = "";

                    if (code == Protocol.BALANCE)
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
                    Client.isConsumeComplete = true;
                }
            });

            waitForConsumeCompletion();

            System.out.println("Enter c to continue, otherwise enter any other key if you wish to exit...");
            var answer = new Scanner(System.in).nextLine();
            if (!answer.equalsIgnoreCase("c"))
                break;
        }
    }

    private static void waitForConsumeCompletion() {
        while (!isConsumeComplete) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("Error while waiting for consume completion: Main Thread interrupted!");
                System.exit(1);
            }
        }
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