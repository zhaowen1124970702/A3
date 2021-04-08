package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import Server.Parameter;
import org.apache.commons.httpclient.HttpStatus;


public class StoreThreadPart2 implements Runnable {

  private Integer storeId;
  private Parameter parameter;
  private PurchaseApi apiInstance = new PurchaseApi();
  private static Random random = new Random();
  private int successRequests = 0;
  private int unSuccessRequests = 0;
  private CountDownLatch countDown3Hour;
  private CountDownLatch countDown5Hour;
  private CountDownLatch complete;
  private CopyOnWriteArrayList<String[]> recordList =  new CopyOnWriteArrayList<>();
  private String[] recordPerReq = new String[5];

//  public static void main(String[] args) throws Exception {
////    ApiClient apiClient = new ApiClient();
////    apiClient.setBasePath("http://localhost:8080/A1_war_exploded");
////    PurchaseApi apiInstance = new PurchaseApi();
////    apiInstance.setApiClient(apiClient);
////    Purchase body = new Purchase();
////    ApiResponse<Void> response = apiInstance.newPurchaseWithHttpInfo(body, 0, 1, "20210101");
//  }

  public StoreThreadPart2(Integer storeId, Parameter parameter, CountDownLatch countDown3Hour, CountDownLatch countDown5Hour, CountDownLatch complete) {
    this.storeId = storeId;
    this.parameter = parameter;
    this.countDown3Hour = countDown3Hour;
    this.countDown5Hour = countDown5Hour;
    this.complete = complete;
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(parameter.getIpAddress());
    apiClient.setConnectTimeout(60 * 1000);
    apiClient.setReadTimeout(60*1000);
    apiClient.setWriteTimeout(60*1000);
    this.apiInstance.setApiClient(apiClient);
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    for(int hour = 0;hour < 9;hour++) {
      try {
        for(int send = 0;send < parameter.getNumPurchases();send++) {
          Purchase body = new Purchase();
          body.items(genItems(parameter));
          // Before send requests, add timestamp
          Long reqStartTime = System.currentTimeMillis();



          ApiResponse<Void> response = apiInstance.newPurchaseWithHttpInfo(body, storeId, genCustomerId(parameter,storeId), parameter.getDate());
          Integer resCode = response.getStatusCode();

          // When the HTTP response is received, take another timestamp
          Long responseTime = System.currentTimeMillis();

          // Calculate the latency (end - start) in milliseconds
          Long latency = responseTime - reqStartTime;

          // Write out a record containing {start time, request type (ie POST), latency, response code}
          recordPerReq[0] = reqStartTime.toString();
          recordPerReq[1] = responseTime.toString();
          recordPerReq[2] = "POST";
          recordPerReq[3] = latency.toString();
          recordPerReq[4] = resCode.toString();
          recordList.add(recordPerReq);

          if (resCode == HttpStatus.SC_OK){
            this.successRequests++;
            continue;
          }else if (String.valueOf(resCode).substring(0) == String.valueOf(5)){
            this.unSuccessRequests ++;
            System.err.println(resCode + ": There is a web error");
          } else if (String.valueOf(resCode).substring(0) == String.valueOf(4)){
            this.unSuccessRequests ++;
            System.err.println(resCode + ": Not found");
          }
        }
      }catch (Exception e) {
        System.out.println("Exception when calling StoreThread");
        e.printStackTrace();

      }
      // set hour 3 as the start of second phase
      if(hour == 2) {
        this.countDown3Hour.countDown();
      }
      // set hour 5 as the start of third phase
      if(hour == 4) {
        this.countDown5Hour.countDown();
      }
    }
    // the thread complete
    this.complete.countDown();
  }


  // Generate purchase items
  private List<PurchaseItems> genItems(Parameter parameter) {
    List<PurchaseItems> list = new ArrayList<>();
    for(int i = 0 ;i<parameter.getNumItemPerPurchase(); i++) {
      PurchaseItems purchaseItems = new PurchaseItems();
      purchaseItems.setItemID(String.valueOf(random.nextInt(parameter.getMaxItemID())));
      purchaseItems.setNumberOfItems(1);
      list.add(purchaseItems);
    }
    return list;
  }

  // Randomly generate customerID
  private Integer genCustomerId(Parameter parameter, Integer storeId) {
    Integer customerId = random.nextInt(parameter.getMaxCustomerPerStore());
    customerId += storeId * parameter.getMaxStores();
    return customerId;
  }

  public int getSuccessRequests() {
    return this.successRequests;
  }

  public int getUnSuccessRequests() {
    return this.unSuccessRequests;
  }

  public List<String[]> getRecordList() {
    return recordList;
  }

}
