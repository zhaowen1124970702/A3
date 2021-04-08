import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ItemsApi;

import java.io.File;
import java.util.*;

public class testApi{

  public static void main(String[] args) {

    ItemsApi apiInstance = new ItemsApi();
    Integer storeID = 56; // Integer | ID of the store for top 10 sales
    try {
      TopItems result = apiInstance.topItems(storeID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemsApi#topItems");
      e.printStackTrace();
    }
  }
}
