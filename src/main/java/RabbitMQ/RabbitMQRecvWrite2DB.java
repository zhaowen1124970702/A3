package RabbitMQ;

import ChannelPool.ChannelPool;
import DB.MarketDao;
import com.rabbitmq.client.*;
import java.io.IOException;
import org.json.JSONObject;

public class RabbitMQRecvWrite2DB {

//  private final static String QUEUE_NAME = "rabbitQueue1";
//  private static final String EXCHANGE_NAME = "logs";
  private static final String EXCHANGE_NAME = "purchase";


  public static void main(String[] argv) throws Exception {
    PoolRecieve();

  }

  private static void PoolRecieve(){
    final ChannelPool channelPool = new ChannelPool();
    try{

      Channel channel = channelPool.getChannel();
      String queueName = channel.queueDeclare().getQueue();
      channel.basicQos(1);
      channel.queueBind(queueName,EXCHANGE_NAME,"");
      System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + message + "'");

        try{
          JSONObject json = new JSONObject(message);
          String customerID = json.getString("customerID");
          String storeID = json.getString("storeID");
          String orderDate = json.getString("orderDate");
          String purchase = json.getJSONObject("purchase").toString();
          MarketDao marketDao = new MarketDao();
          marketDao.createMarketDao(storeID, customerID, orderDate, purchase);
        }finally {
          System.out.println("[x] done");
        }
      };
      boolean autoAck = true;
      channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });
    }catch (Exception e){
      e.printStackTrace();
    }

  }

//  private static void SingleChannelRecieve(){
//    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("localhost");
//    factory.setPort(5672);
//    Connection connection = factory.newConnection();
//    Channel channel = connection.createChannel();
//
//    channel.basicQos(1);
////    channel.queueDeclare(QUEUE_NAME,false,false,false,null);
//    String queueName = channel.queueDeclare().getQueue();
//    channel.queueBind(queueName,EXCHANGE_NAME,"");
//
//
//    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//      String message = new String(delivery.getBody(), "UTF-8");
//      System.out.println(" [x] Received '" + message + "'");
//
//      try{
//        JSONObject json = new JSONObject(message);
//        String customerID = json.getString("customerID");
//        String storeID = json.getString("storeID");
//        String orderDate = json.getString("orderDate");
//        String purchase = json.getJSONObject("purchase").toString();
//        MarketDao marketDao = new MarketDao();
//        marketDao.createMarketDao(storeID, customerID, orderDate, purchase);
//      }finally {
//        System.out.println("[x] done");
//      }
//    };
//    boolean autoAck = true;
//    channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });
//
//  }
}