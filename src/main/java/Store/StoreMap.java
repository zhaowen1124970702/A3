package Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class StoreMap {

  private static HashMap<String , Integer> itemMap = new HashMap<String ,Integer>() ;
  private static HashMap<String , HashMap<String , Integer> > storeMap = new HashMap<String ,HashMap<String , Integer>>() ;
  private static HashMap<String , Integer> storeItemNumMap = new HashMap<String ,Integer>();
  private static HashMap<String , HashMap<String , Integer> > ItemSaleMap = new HashMap<String ,HashMap<String , Integer>>();


  public void createMap (String storeID, JSONObject purchase) throws Exception {
//    List<PurchaseItems> items = new ArrayList<>();
    JSONArray jsonArray = (JSONArray) purchase.get("items");

    for(int i = 0; i < jsonArray.length(); i++){
      JSONObject item = jsonArray.getJSONObject(i);
      String itemID = item.get("itemID").toString();
      Integer numOfItems = Integer.parseInt(item.get("numOfItems").toString());
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

  public List top10Items(String argvStoreID){
    HashMap<String ,Integer> newItemMap = this.storeMap.get(argvStoreID);
    HashMap<String ,Integer> sortedMap = sortByValues(newItemMap);
    ArrayList<String> res = new ArrayList<>();
    String s = null;
    for (Map.Entry<String, Integer> pair : sortedMap.entrySet()) {
      s = " { itemID: " + pair.getKey() + " numberOfItems: " + pair.getValue() + " } ";
      res.add(s);
    }
    if(res.size() < 10){
      return res;
    }else{
      return res.subList(res.size()-10,res.size());
    }
  }

  public List top10Stores(String argvItemID){
    HashMap<String ,Integer> newStoreMap = this.ItemSaleMap.get(argvItemID);
    HashMap<String ,Integer> sortedMap = sortByValues(newStoreMap);
    ArrayList<String> res = new ArrayList<>();
    String s = null;
    for (Map.Entry<String, Integer> pair : sortedMap.entrySet()) {
      s = " { storeID: " + pair.getKey() + " numberOfItems: " + pair.getValue() + " } ";
      res.add(s);
    }

    if(res.size() < 10){
      return res;
    }else{
      return res.subList(res.size()-10,res.size());
    }
  }

  private HashMap sortByValues(HashMap map) {
    List list = new LinkedList(map.entrySet());
    // Defined Custom Comparator here
    Collections.sort(list, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((Comparable) ((Map.Entry) (o1)).getValue())
            .compareTo(((Map.Entry) (o2)).getValue());
      }
    });

    HashMap sortedHashMap = new LinkedHashMap();
    for (Iterator it = list.iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      sortedHashMap.put(entry.getKey(), entry.getValue());
    }
    return sortedHashMap;
  }
}

