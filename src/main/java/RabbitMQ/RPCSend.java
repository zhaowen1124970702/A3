package RabbitMQ;


import ChannelPool.ChannelPool;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCSend implements AutoCloseable {


  private Channel channel;
  private String requestQueueName = "rpc_queue";
  private ChannelPool channelPool ;

  public RPCSend() throws IOException, TimeoutException {
    channelPool = new ChannelPool();
    channel = channelPool.getChannel();
  }

  public static void main(String[] argv) throws Exception {

    try (RPCSend marketRpc = new RPCSend()) {
      for (int i = 0; i < 32; i++) {
        String i_str = Integer.toString(i);
        System.out.println(" [x] Requesting fib(" + i_str + ")");
        String response = marketRpc.call(i_str);
        System.out.println(" [.] Got '" + response + "'");
      }
    } catch (IOException | TimeoutException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public String call(String message) throws IOException, InterruptedException {
    final String corrId = UUID.randomUUID().toString();

    String replyQueueName = channel.queueDeclare().getQueue();
    AMQP.BasicProperties props = new AMQP.BasicProperties
        .Builder()
        .correlationId(corrId)
        .replyTo(replyQueueName)
        .build();

    channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

    final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

    String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
      if (delivery.getProperties().getCorrelationId().equals(corrId)) {
        response.offer(new String(delivery.getBody(), "UTF-8"));
      }
    }, consumerTag -> {
    });

    String result = response.take();
    channel.basicCancel(ctag);
    return result;
  }

  @Override
  public void close() throws Exception {
  }
}
