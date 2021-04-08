package RabbitMQ;

public class Message {

  private String customerID;
  private String storeID;
  private String orderDate;
  private String purchase;

  public Message(String storeID, String customerID, String orderDate, String purchase) {
    this.customerID = customerID;
    this.storeID = storeID;
    this.orderDate = orderDate;
    this.purchase = purchase;
  }


  public String getCustomerID() {
    return customerID;
  }

  public String getStoreID() {
    return storeID;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public String getPurchase() {
    return purchase;
  }


}
