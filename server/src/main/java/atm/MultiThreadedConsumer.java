package atm;

import atm.service.ClientConnection;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static atm.service.Protocol.*;
import static atm.service.Protocol.REQUEST_EXCHANGE_NAME;

public class MultiThreadedConsumer extends DefaultConsumer {
    private final Connection conn;

    public MultiThreadedConsumer(Connection conn,
                                 Channel channel) {
        super(channel);
        this.conn = conn;
    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        this.getChannel().basicAck(envelope.getDeliveryTag(), false);

        Channel replyChannel = conn.createChannel();
        System.out.println("Created channel: " + replyChannel);

        // Declare reply queue
        replyChannel.queueDeclare(REPLY_QUEUE_NAME, false, false, false, null);

        // Declare reply exchange
        replyChannel.exchangeDeclare(REPLY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // Start virtual thread to handle db connections, processing etc.
        Thread.ofVirtual().start(new ClientConnection(replyChannel, body));
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        System.out.println("Consumer subscribed successfully with tag: " + consumerTag);
    }
}
