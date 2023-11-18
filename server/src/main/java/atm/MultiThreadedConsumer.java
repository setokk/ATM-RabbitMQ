package atm;

import atm.service.ClientConnection;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static atm.service.Protocol.*;
import static atm.service.Protocol.REQUEST_EXCHANGE_NAME;

public class MultiThreadedConsumer extends DefaultConsumer {
    private Connection conn;
    private ExecutorService service;

    public MultiThreadedConsumer(Channel channel) {
        super(channel);
    }

    public MultiThreadedConsumer(Connection conn,
                                 Channel channel,
                                 ExecutorService service) {
        super(channel);
        this.conn = conn;
        this.service = service;
    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        Channel replyChannel = conn.createChannel();

        // Declare reply queue
        replyChannel.queueDeclare(REPLY_QUEUE_NAME, false, false, false, null);

        // Declare reply exchange
        replyChannel.exchangeDeclare(REPLY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // Get the ip address sent by the client
        String ipAddress = "";
        Map<String, Object> headers = properties.getHeaders();
        if (headers != null && headers.containsKey("ip_address"))
            ipAddress = (String) headers.get("ip_address");

        System.out.println("Processing request from: " + ipAddress);
        service.execute(new ClientConnection(replyChannel, ipAddress, body));
    }
}
