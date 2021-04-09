package ChannelPool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class testPoolRec {
  public static void main(String[] args)
      throws Exception {
    testThreadPoolRec();

  }
  private static void testThreadPoolRec() {
    final ChannelPool channelPool = new ChannelPool();
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        try {
            Channel channel = channelPool.getChannel();

            final String EXCHANGE_NAME = "dyh";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

//            //发送消息
//            channel.basicPublish(EXCHANGE_NAME, "1", null, "s".getBytes());

            //接收消息
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "1");

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
              String message = new String(delivery.getBody(), "UTF-8");
              System.out.println(" [x] Received '" + message + "'");
          };
            boolean autoAck = true;
            channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });
            //关闭资源
            channelPool.returnChannel(channel);
        } catch (Exception e) {

        }
      }).start();
    }

  }

}


