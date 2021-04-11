package Store;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;


public class StoreMap {

  private static HashMap<String , Integer> itemMap = new HashMap<String ,Integer>() ;
  private static Map<String , HashMap<String , Integer> > storeMap = new ConcurrentHashMap<String ,HashMap<String , Integer>>() ;

  private static HashMap<String , Integer> storeItemNumMap = new HashMap<String ,Integer>();
  private static Map<String , HashMap<String , Integer> > ItemSaleMap = new ConcurrentHashMap<String ,HashMap<String , Integer>>();


  public void updateMap (String storeID, JSONObject purchase) throws Exception {
//    List<PurchaseItems> items = new ArrayList<>();
    JSONArray jsonArray = (JSONArray) purchase.get("items");

    for(int i = 0; i < jsonArray.length(); i++){
      JSONObject item = jsonArray.getJSONObject(i);
      String itemID = item.get("ItemID").toString();
      Integer numOfItems = Integer.parseInt(item.get("numberOfItems").toString());
      if(numOfItems <= 0) {
        throw new Exception("Error Item quantity!");
      }
      // STORE DATA INTO ItemSaleMap
      if(ItemSaleMap.containsKey(itemID) && storeItemNumMap.containsKey(storeID) ){
        storeItemNumMap.put(storeID, storeItemNumMap.get(storeID) + numOfItems);
        } else{
        storeItemNumMap.put(storeID, numOfItems);
      }
      ItemSaleMap.put(itemID, storeItemNumMap);

      // STORE DATA INTO storeMap
      if(itemMap.containsKey(itemID)){
        itemMap.put(itemID, itemMap.get(itemID) + numOfItems);
      }else{
        itemMap.put(itemID, numOfItems);
      }
    }
    storeMap.put(storeID, this.itemMap);

  }

  public String top10Items(String argvStoreID){
    if(!storeMap.containsKey(argvStoreID)){
      return "{\"Errors\" : \"Query data not found\"}";
    }
    HashMap<String ,Integer> newStoreMap = storeMap.get(argvStoreID);
    Map<String, Integer> sortedMap = newStoreMap.entrySet()
        .stream() .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(10)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    StringBuilder res = new StringBuilder("{\"stores\" : [");

    for (Map.Entry<String, Integer> pair : sortedMap.entrySet()) {
      res.append("{\"itemsID\" : ").append(pair.getKey()).append(", \"numberOfItems\" : ").append(pair.getValue()).append("}, ");
    }
    res = new StringBuilder(res.substring(0, res.length() - 2));
    res.append("]}");
    return res.toString();

  }

  public String top10Stores(String argvItemID){
    if(!ItemSaleMap.containsKey(argvItemID)){
      return "{\"Errors\" : \"Query data not found\"}";
    }
    HashMap<String ,Integer> newStoreMap = this.ItemSaleMap.get(argvItemID);
    Map<String, Integer> sortedMap = newStoreMap.entrySet()
        .stream() .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(10)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    StringBuilder res = new StringBuilder("{\"stores\" : [");

    for (Map.Entry<String, Integer> pair : sortedMap.entrySet()) {
      res.append("{\"storeID\" : ").append(pair.getKey()).append(", \"numberOfItems\" : ").append(pair.getValue()).append("}, ");
    }
    res = new StringBuilder(res.substring(0, res.length() - 2));
    res.append("]}");
    return res.toString();
  }

}

