package RabbitMQ;

import ChannelPool.ChannelPool;
import com.rabbitmq.client.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;



public class RabbitMQSend implements AutoCloseable{

  private static Connection connection;
  private static Channel channel;
  private final static String QUEUE_NAME = "rabbitQueue";
  private static final String EXCHANGE_NAME = "logs";

  public RabbitMQSend() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setPort(5672);
    connection = factory.newConnection();
    channel = connection.createChannel();
  }

  public void Send(String message) throws Exception {
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + message + "'");
  }


  public void ThreadPoolSend(ChannelPool channelPool, String message) {
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        try {
          while (true) {
            Channel channel = channelPool.getChannel();

            final String EXCHANGE_NAME = "logs";
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            //发送消息
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
            //关闭资源
            channelPool.returnChannel(channel);
          }
        } catch (Exception e) {

        }
      }).start();
    }

  }

  @Override
  public void close() throws Exception {
    connection.close();
  }
}