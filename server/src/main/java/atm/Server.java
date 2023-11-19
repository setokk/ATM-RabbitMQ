package atm;

import atm.db.DatabaseDriver;
import atm.service.ClientConnection;
import com.rabbitmq.client.*;
import atm.db.DBConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static atm.service.Protocol.*;

public class Server {
    public static final DBConfig DB_CONFIG =
            new DBConfig("jdbc:postgresql://db:5432/atm",
                    "postgres", "root!238Ji*");
    public static void main(String[] args)
            throws TimeoutException, SQLException
    {
        var db = new DatabaseDriver(DB_CONFIG);
        db.init();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);


        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            // Declare queues
            channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(REPLY_QUEUE_NAME, false, false, false, null);

            // Declare exchanges
            channel.exchangeDeclare(REQUEST_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.exchangeDeclare(REPLY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            // Bind request queue to request exchange ("server" as binding key)
            channel.queueBind(REQUEST_QUEUE_NAME, REQUEST_EXCHANGE_NAME, "server");

            boolean autoAck = false;
            channel.basicConsume(REQUEST_QUEUE_NAME,
                    autoAck,
                    new MultiThreadedConsumer(conn, channel));
        } catch (IOException e) {
            System.err.println("Problem connecting to server");
            e.printStackTrace();
        }
    }
}
