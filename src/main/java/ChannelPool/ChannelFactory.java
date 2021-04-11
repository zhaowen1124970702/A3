package ChannelPool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import static ChannelPool.ChannelConst.HOST;
import static ChannelPool.ChannelConst.PORT;
import static ChannelPool.ChannelConst.USERNAME;
import static ChannelPool.ChannelConst.PASSWORD;

public class ChannelFactory implements PooledObjectFactory<Channel> {
  private Connection connection;

  public ChannelFactory() {
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(HOST);
      factory.setPort(PORT);
      factory.setUsername(USERNAME);
      factory.setPassword(PASSWORD);
      factory.setVirtualHost("/");
      factory.setAutomaticRecoveryEnabled(true);
      connection = factory.newConnection();
    } catch (Exception e) {
      throw new ChannelException("connection failed in creating channelFactory", e);
    }
  }

  public PooledObject<Channel> makeObject() throws Exception {
    return new DefaultPooledObject<Channel>(connection.createChannel());
  }

  public void destroyObject(PooledObject<Channel> pooledObject) throws Exception {
    final Channel channel = pooledObject.getObject();
    if (channel.isOpen()) {
      try {
        channel.close();
      } catch (Exception e) {
      }
    }
  }

  public boolean validateObject(PooledObject<Channel> pooledObject) {
    final Channel channel = pooledObject.getObject();
    return channel.isOpen();
  }

  public void activateObject(PooledObject<Channel> pooledObject) throws Exception {

  }

  public void passivateObject(PooledObject<Channel> pooledObject) throws Exception {

  }
}
