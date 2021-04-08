package Server;

import ChannelPool.ChannelPool;
import RabbitMQ.RabbitMQSend;
import java.io.IOException;
import java.nio.channels.Channel;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.rabbitmq.client.AMQP;
//import com.rabbitmq.client.Channel;
//import RabbitMQSend;

import RabbitMQ.RPCSend;


@WebServlet(name = "Server.SupermarketsServlet")
public class SupermarketsServlet extends HttpServlet {
  private ChannelPool channelPool ;
  private Channel channel;
//
//  public void init() throws ServletException {
//    super.init();
//    try{
//      channelPool = new ChannelPool();
//    }catch (Exception e){
//    }
//  }


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

      try (RabbitMQSend rabbitMQSend = new RabbitMQSend()) {
//        System.out.println(" Requesting message");
        rabbitMQSend.Send(string);
//        rabbitMQSend.ThreadPoolSend(channelPool, string);
      } catch (Exception e) {
        e.printStackTrace();
      }

      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().write("200: Send data successfully!");
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
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValidDoGet(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {

      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`

      String message = new String();

      if((urlParts[2]).equals("store")){
        String storeID = urlParts[3];
        message = "{" + "\"storeID\": \"" + storeID +"\", " + "}";
      }
      if((urlParts[2]).equals("top10")){
        String itemID = urlParts[3];
        message = "{" + "\"itemID\": \"" + itemID +"\", " + "}";
      }
      String response = new String();
      try (RPCSend marketRpc = new RPCSend()) {
//        System.out.println(" Requesting message");
        response = marketRpc.call(message);
      } catch (Exception e) {
        e.printStackTrace();
      }
      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write(response);
    }


  }
    private boolean isUrlValid(String[] split) {
      return split.length == 6 &&
          isInteger(split[1]) && split[2].equals("customer") &&
          isInteger(split[3]) && split[4].equals("date") && isValidDate(split[5]);
    }

  private boolean isUrlValidDoGet(String[] split) {
    return split.length == 4 && split[1].equals("items") &&
        ((split[2]).equals("store") || split[2].equals("top10")) && isInteger(split[3]);
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
}


