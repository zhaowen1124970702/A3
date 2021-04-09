package ChannelPool;

import com.rabbitmq.client.Channel;

public class testPool {

  public static void main(String[] args)
      throws Exception {
    testThreadPool();

  }
  private static void testThreadPool() {
    final ChannelPool channelPool = new ChannelPool();
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        try {
          while (true) {
            Channel channel = channelPool.getChannel();

            final String EXCHANGE_NAME = "dyh";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            //发送消息
            channel.basicPublish(EXCHANGE_NAME, "1", null, "s".getBytes());
            channelPool.returnChannel(channel);
          }
        } catch (Exception e) {

        }
      }).start();
    }

  }
}