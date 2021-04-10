package Server;

import ChannelPool.ChannelPool;
import java.io.IOException;
//import java.nio.channels.Channel;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.rabbitmq.client.Channel;




@WebServlet(name = "Server.SupermarketsServlet")
public class SupermarketsServlet extends HttpServlet {
  private ChannelPool channelPool ;

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      channelPool = new ChannelPool();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  protected void doPost(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");
    String urlPath = request.getPathInfo();
//    System.out.println(urlPath);

    if (urlPath == null || urlPath.length() == 0) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }
    String[] split = urlPath.split("/");
//    System.out.println(split);

    if (!isUrlValid(split)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Invalid Values/formats provided!");
    } else {
      StringBuilder jsonBody = new StringBuilder();
      String line;
      try {
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
          jsonBody.append(line);
      } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("404 occurs when parsing json request body");
        return;
      }

      if (jsonBody.toString().isEmpty()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("400 occurs when validating json request body");
        return;
      }

      // send to queue
      String customerID = split[3];
      String storeID = split[1];
      String orderDate = split[5];
      String purchase = jsonBody.toString();

      String string = "{" + "\"customerID\": \"" + customerID +"\", " + "\"storeID\": \"" + storeID + "\", "
          + "\"orderDate\": \""+ orderDate + "\", " + "\"purchase\": "+ purchase + "}";

      Boolean isPublished = false;
      try{
        isPublished = publishMessage(string);
      }catch (Exception e){
        e.printStackTrace();
      }

      if(isPublished){
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Send data successfully!");
      } else{
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Failed to Send data to rabbitMQ!");
      }

    }
  }

    private boolean isUrlValid(String[] split) {
      return split.length == 6 &&
          isInteger(split[1]) && split[2].equals("customer") &&
          isInteger(split[3]) && split[4].equals("date") && isValidDate(split[5]);
    }


    private boolean isInteger(String s){
      try{
        Integer.parseInt(s);
        return true;
      }catch (NumberFormatException ex){
        return false;
      }
    }

    private boolean isValidDate(String s){
      DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
      try {
        LocalDate.parse(s, dateFormatter);
      } catch (DateTimeParseException e) {
        return false;
      }
      return true;
    }

    public boolean publishMessage(String message) throws IOException{
      boolean isPublished = false;
      Channel channel = channelPool.getChannel();
      final String EXCHANGE_NAME = "purchase";
      channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
      channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
      channelPool.returnChannel(channel);
      isPublished =  true;
      return isPublished;


    }
}


