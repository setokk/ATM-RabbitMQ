package atm;

import atm.db.DatabaseDriver;
import atm.service.ClientConnection;
import com.rabbitmq.client.*;
import atm.db.DBConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import static atm.service.Protocol.*;

public class Server {
    public static final DBConfig DB_CONFIG =
            new DBConfig("jdbc:postgresql://db:5432/atm",
                    "postgres", "root!238Ji*");
    public static void main(String[] args)
            throws IOException, TimeoutException, SQLException
    {
        var db = new DatabaseDriver(DB_CONFIG);
        db.init();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);

        Connection conn = factory.newConnection();
        while (true) {
            Channel channel = conn.createChannel();

            // Declare queues
            channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(REPLY_QUEUE_NAME, false, false, false, null);

            // Declare exchanges
            channel.exchangeDeclare(REQUEST_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.exchangeDeclare(REPLY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            // Bind queues to exchanges.
            // That means that each queue will be interested in messages
            // from the corresponding exchanges
            channel.queueBind(REPLY_QUEUE_NAME, REPLY_EXCHANGE_NAME, "");
            channel.queueBind(REQUEST_QUEUE_NAME, REQUEST_EXCHANGE_NAME, "");

            channel.basicConsume(REQUEST_QUEUE_NAME, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    Thread.ofVirtual().start(new ClientConnection(channel, properties.getHeaders(), body));
                }
            });
        }
    }
}
