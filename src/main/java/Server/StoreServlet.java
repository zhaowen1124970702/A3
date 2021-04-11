package Server;

import ChannelPool.ChannelPool;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "StoreServlet")
public class StoreServlet extends HttpServlet {
  private ChannelPool channelPool ;
  private String requestQueueName = "rpc_queue";

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      channelPool = new ChannelPool();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void doGet(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    if (!isUrlValidDoGet(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {

      String query = new String();
      String queryID = new String();
      String message = "{\"query\" : " + query + ", \"queryID\" : " + queryID + "}";

      if((urlParts[1]).equals("store")){
        queryID = urlParts[2];
        query = "store";
        message = "{\"query\" : " + query + ", \"queryID\" : " + queryID + "}";
      }
      if((urlParts[1]).equals("top10")){
        queryID = urlParts[2];
        query = "item";
        message = "{\"query\" : " + query + ", \"queryID\" : " + queryID + "}";

      }
      String result = new String();
      try{
        result = sendQuery(message);
      } catch (Exception e){
        e.printStackTrace();
      }
      if (result.equals("{\"Errors\" : \"Query data not found\"}")) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
        res.setStatus(HttpServletResponse.SC_OK);
      }
      res.getWriter().write(result);

    }
  }

  private boolean isUrlValidDoGet(String[] split) {
    return split.length == 3 &&
        ((split[1]).equals("store") || split[1].equals("top10")) && isInteger(split[2]);
  }

  private boolean isInteger(String s){
    try{
      Integer.parseInt(s);
      return true;
    }catch (NumberFormatException ex){
      return false;
    }
  }

  public String sendQuery(String message) throws IOException, InterruptedException {
    final String corrId = UUID.randomUUID().toString();
    Channel channel = channelPool.getChannel();

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

}
