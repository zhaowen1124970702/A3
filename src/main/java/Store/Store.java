package Store;

import DB.MarketDao;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.PurchaseItems;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;
import com.rabbitmq.client.*;
import java.io.IOException;
import org.json.JSONObject;

public class Store {

  private static final String EXCHANGE_NAME = "logs";
  private static final String RPC_QUEUE_NAME = "rpc_queue";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setPort(5672);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    //

    Channel channelRPC = connection.createChannel();
    channelRPC.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
    channelRPC.queuePurge(RPC_QUEUE_NAME);
    channelRPC.basicQos(1);
    System.out.println(" [x] Awaiting RPC requests");
    Object monitor = new Object();

    //

    channel.basicQos(1);
//    channel.queueDeclare(QUEUE_NAME,false,false,false,null);
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName,EXCHANGE_NAME,"");


    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    StoreMap storeMap = new StoreMap();

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [x] Received '" + message + "'");

      JSONObject json = new JSONObject(message);
      String storeID = json.getString("storeID");
      JSONObject purchase = json.getJSONObject("purchase");

      try{
        storeMap.createMap(storeID,purchase);
      }catch (Exception e){
        e.printStackTrace();
      }

    };
    boolean autoAck = true;
    channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });



    DeliverCallback deliverCallbackForRPC = (consumerTag, delivery) -> {
      AMQP.BasicProperties replyProps = new AMQP.BasicProperties
          .Builder()
          .correlationId(delivery.getProperties().getCorrelationId())
          .build();

      String response = "";

      try {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println("messagefromDoGet: " + message);

        JSONObject json = new JSONObject(message);
        String storeID = null;
        String itemID = null;
        if(json.has("storeID")){
          storeID = json.getString("storeID");
          response = convertListToString(storeMap.top10Items(storeID));
        }

        if(json.has("itemID")){
          itemID = json.getString("itemID");
          response = convertListToString(storeMap.top10Stores(itemID));
        }

      } catch (RuntimeException e) {
        System.out.println(" [.] " + e.toString());
      } finally {
        channelRPC.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
        channelRPC.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        // RabbitMq consumer worker thread notifies the RPC server owner thread
        synchronized (monitor) {
          monitor.notify();
        }
      }
    };

    channelRPC.basicConsume(RPC_QUEUE_NAME, false, deliverCallbackForRPC, (consumerTag -> { }));
    // Wait and be prepared to consume the message from RPC client.
    while (true) {
      synchronized (monitor) {
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static String convertListToString( List list) {
    String result = (String) list.stream()
        .map(n -> String.valueOf(n))
        .collect(Collectors.joining(",", "{", "}"));

    return result;

  }



}
