package atm;

import atm.service.ClientConnection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class MultiThreadedConsumer extends DefaultConsumer {
    private ExecutorService service;

    public MultiThreadedConsumer(Channel channel) {
        super(channel);
    }

    public MultiThreadedConsumer(Channel channel, ExecutorService service) {
        super(channel);
        this.service = service;
    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) {
        service.execute(new ClientConnection(this.getChannel(), properties.getHeaders(), body));
    }
}
