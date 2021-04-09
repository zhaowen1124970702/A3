package Store;

import ChannelPool.ChannelPool;
import DB.MarketDao;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.PurchaseItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;
import com.rabbitmq.client.*;
import java.io.IOException;
import org.json.JSONObject;


public class Store {

  private static final String EXCHANGE_NAME = "purchase";
  private static final String RPC_QUEUE_NAME = "rpc_queue";
  private static final int NUM_THREADS = 4;

  public static void main(String[] argv) throws Exception {
    StoreSerevice();
  }

  public static void StoreSerevice() throws InterruptedException {

    final ChannelPool channelPool = new ChannelPool();
    StoreMap storeMap = new StoreMap();

    Runnable PurchaseRunnable =
        () -> {
            try {
              Channel channel = channelPool.getChannel();
              String queueName = channel.queueDeclare().getQueue();
              channel.basicQos(20);
              channel.queueBind(queueName, EXCHANGE_NAME, "");
              System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//              Object monitor = new Object();

              DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");


                JSONObject json = new JSONObject(message);
                String storeID = json.getString("storeID");
                JSONObject purchase = json.getJSONObject("purchase");

                try {
                  storeMap.updateMap(storeID, purchase);
//                  System.out.println(storeMap.storeItemNumMap().size());
                } catch (Exception e) {
                  e.printStackTrace();
                }
              };
              boolean autoAck = true;
              channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });

              channelPool.returnChannel(channel);

          }catch(Exception e){
        e.printStackTrace();
      }
    };


    Runnable RPCRunnable =
        () -> {
      try{
        Channel channelRPC = channelPool.getChannel();
        channelRPC.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channelRPC.queuePurge(RPC_QUEUE_NAME);

        channelRPC.basicQos(1);
        System.out.println(" [x] Awaiting RPC requests");
        Object monitor = new Object();

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
            String query = json.getString("query");
            if(query.equals("store")){
              storeID = String.valueOf(json.getInt("queryID"));
              response = storeMap.top10Items(storeID);
            }

            if(query.equals("item")){
              itemID = String.valueOf(json.getInt("queryID"));
              response = storeMap.top10Stores(itemID);
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
//        channelPool.returnChannel(channelRPC);

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


      }catch (Exception e){
        e.printStackTrace();
      }
    };

    Thread[] purchaseRev = new Thread[NUM_THREADS];
    Thread[] queryThread = new Thread[NUM_THREADS];

    for (int i=0; i< NUM_THREADS; i++) {
      purchaseRev[i] = new Thread(PurchaseRunnable);
      purchaseRev[i].start();

      queryThread[i] = new Thread(RPCRunnable);
      queryThread[i].start();
    }

    for (int i=0; i< NUM_THREADS; i++) {
      purchaseRev[i].join();
      queryThread[i].join();
    }

  }

}
