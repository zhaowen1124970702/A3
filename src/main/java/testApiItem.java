import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ItemsApi;

import java.io.File;
import java.util.*;

public class testApiItem {

  public static void main(String[] args) {

    ItemsApi apiInstance = new ItemsApi();
    Integer itemID = 56; // Integer | ID of the items that we want to find top stores
    try {
      TopStores result = apiInstance.topStores(itemID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemsApi#topStores");
      e.printStackTrace();
    }
  }
}