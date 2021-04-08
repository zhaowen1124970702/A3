package RabbitMQ;

import DB.MarketDao;
import com.rabbitmq.client.*;
import java.io.IOException;
import org.json.JSONObject;

public class RabbitMQRecv {

//  private final static String QUEUE_NAME = "rabbitQueue1";
  private static final String EXCHANGE_NAME = "logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setPort(5672);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.basicQos(1);
//    channel.queueDeclare(QUEUE_NAME,false,false,false,null);
    String queueName = channel.queueDeclare().getQueue();
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


  }
}