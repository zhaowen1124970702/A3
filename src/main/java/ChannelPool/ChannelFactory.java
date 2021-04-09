package ChannelPool;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import static ChannelPool.ChannelConst.HOST;
import static ChannelPool.ChannelConst.PORT;
import static ChannelPool.ChannelConst.USERNAME;
import static ChannelPool.ChannelConst.PASSWORD;

public class ChannelFactory implements PooledObjectFactory<Channel> {
  private Connection connection;

//  public ChannelFactory() {
//    this(null);
//  }

  public ChannelFactory() {
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(HOST);
      factory.setPort(PORT);
      factory.setUsername(USERNAME);
      factory.setPassword(PASSWORD);
      factory.setVirtualHost("/");
//      if (uri != null) {
//        factory.setUri(uri);
//      }
      connection = factory.newConnection();
    } catch (Exception e) {
      throw new ChannelException("连接失败", e);
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

//public class ChannelFactory extends BasePooledObjectFactory<Channel> {
//  private Connection connection;
//  private volatile static ChannelFactory channelFactory = null;
//
//  public ChannelFactory() {
//    try {
//      ConnectionFactory factory = new ConnectionFactory();
//      factory.setHost(HOST);
//      factory.setUsername(USERNAME);
//      factory.setPassword(PASSWORD);
//      factory.setPort(5672);
//      connection = factory.newConnection();
//    } catch (Exception e) {
//      throw new ChannelException("Fail to connect channel", e);
//    }
//  }
//
//  public static ChannelFactory getInstance() {
//    if (channelFactory == null) {
//      synchronized (ChannelFactory.class){
//        if(channelFactory == null){
//          try {
//            channelFactory =  new ChannelFactory();
//          } catch (Exception e) {
//            throw new ChannelException("Fail to get channel instance", e);
//          }
//        }
//      }
//    }
//    return channelFactory;
//  }
//
//
//  @Override
//  public Channel create() throws Exception {
//    return connection.createChannel();
//  }
//
//  @Override
//  public PooledObject<Channel> wrap(Channel channel) {
//    return new DefaultPooledObject<>(channel);
//  }
//
//
//}
