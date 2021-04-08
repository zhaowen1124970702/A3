package Server;

import java.util.HashMap;
import java.util.Map;

public class Parameter {
  private Integer maxStores;
  private Integer maxCustomerPerStore;
  private Integer maxItemID;
  private Integer numPurchases;
  private Integer numItemPerPurchase;
  private String date;
  private String ipAddress;

  public static Parameter parse(String[] args) throws Exception {
    Map<String, String> map = new HashMap<>();
//    System.out.println(args);
    for(int i = 0 ; i < args.length-1;i+=2) {
      map.put(args[i], args[i+1]);
    }
    // Integer | maximum number of stores to simulate (maxStores)
    Parameter parameter = new Parameter();
    if(!map.containsKey("--maxStores")) {
      throw new Exception("Invalid input: maxStores missing.");
    }
    parameter.maxStores = Integer.parseInt(map.get("--maxStores"));

    // Integer | number of customers/store (default 1000). This is the range of custIDs per store
    if(!map.containsKey("--maxCustomerPerStore")) {
      parameter.maxCustomerPerStore = 1000;
    } else{
      parameter.maxCustomerPerStore = Integer.parseInt(map.get("--maxCustomerPerStore"));
    }

    // Integer | maximum itemID - default 100000
    if(!map.containsKey("--maxItemID")) {
      parameter.maxItemID = 100000;
    } else{
      parameter.maxItemID = Integer.parseInt(map.get("--maxItemID"));

    }

    // Integer | number of purchases per hour: (default 60) - thus is numPurchases
    if(!map.containsKey("--numPurchases")) {
      parameter.numPurchases = 300;
    } else{
      parameter.numPurchases = Integer.parseInt(map.get("--numPurchases"));

    }

    // Integer | number of items for each purchase (range 1-20, default 5)
    if(!map.containsKey("--numItemPerPurchase")) {
      parameter.numItemPerPurchase = 5;
    } else{
      parameter.numItemPerPurchase = Integer.parseInt(map.get("--numItemPerPurchase"));
    }
    // validate numItemPerPurchase: range 1-20
    if(parameter.numItemPerPurchase < 1 || parameter.numItemPerPurchase > 20){
      throw new Exception("Invalid numItemPerPurchase -- range(1-20)");
    }

    // date - default to 20210101
    if(!map.containsKey("--date")) {
      parameter.date = "20210101";
    } else{
      parameter.date = map.get("--date");
    }

    // String | IP/port address of the server
    if(!map.containsKey("--ipAddress")) {
      throw new Exception("Invalid input: ipAddress missing.");
    }
    parameter.ipAddress = map.get("--ipAddress");
    return parameter;
  }

  public Integer getMaxStores() {
    return maxStores;
  }

  public Integer getMaxCustomerPerStore() {
    return maxCustomerPerStore;
  }

  public Integer getMaxItemID() {
    return maxItemID;
  }

  public Integer getNumPurchases() {
    return numPurchases;
  }

  public String getDate() {
    return date;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public Integer getNumItemPerPurchase() {
    return numItemPerPurchase;
  }

}
