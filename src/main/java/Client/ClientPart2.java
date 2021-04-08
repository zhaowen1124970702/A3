package Client;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import Server.Parameter;

public class ClientPart2 {


  public static void main(String[] args) {

    //  Take a timestamp before commencing Phase 1
    System.out.println("--Client 2 starting--");

    int successRequests = 0;
    int unSucceddRequests = 0;
    Long startTime = (long)0;
    Long endTime = (long)0;
    List<StoreThreadPart2> threadPool = new ArrayList<>();
    List<String[]> data = new ArrayList<>();


    try {
      Parameter parameter = Parameter.parse(args);
      int threads = parameter.getMaxStores();
      System.out.println( "Total threads: " + threads);

      // create new CountDownLatch object
      CountDownLatch countDown3Hour = new CountDownLatch(1);
      CountDownLatch countDown5Hour = new CountDownLatch(1);
      CountDownLatch complete = new CountDownLatch(parameter.getMaxStores());
      // phase1
      startTime = System.currentTimeMillis();
      System.out.println(String
          .format("Start at: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(startTime)));
      System.out.println("--Store opens in east--");
      int storeId = 0;
      for (int i = 0; i < parameter.getMaxStores() / 4; i++) {
        StoreThreadPart2 storeThread = new StoreThreadPart2(storeId, parameter, countDown3Hour, countDown5Hour, complete);
        new Thread(storeThread).start();
        storeId++;
        threadPool.add(storeThread);
      }
      System.out.println("Wait for central store to open...");
      countDown3Hour.await();
      // phase2
      System.out.println("--Store opens in central--");
      for (int i = 0; i < parameter.getMaxStores() / 4; i++) {
        StoreThreadPart2 storeThread = new StoreThreadPart2(storeId, parameter, countDown3Hour, countDown5Hour, complete);
        new Thread(storeThread).start();
        storeId++;
        threadPool.add(storeThread);
      }
      System.out.println("Wait for western store to open...");
      countDown3Hour.await();
      // phase3
      System.out.println("--Store opens in west--");
      while (storeId < parameter.getMaxStores()) {
        StoreThreadPart2 storeThread = new StoreThreadPart2(storeId, parameter, countDown3Hour, countDown5Hour, complete);
        new Thread(storeThread).start();
        storeId++;
        threadPool.add(storeThread);
      }
      // wait for all threads to complete
      System.out.println("Wait for all threads to complete...");
      complete.await();

      // Take another timestamp after all Phase 3 threads are complete
      endTime = System.currentTimeMillis();
      System.out.println(String.format(
          "All threads complete at: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(endTime)));

      // Collect record data from StoreThread and calculate the requests
      for (StoreThreadPart2 storeThread : threadPool) {
        successRequests += storeThread.getSuccessRequests();
        unSucceddRequests += storeThread.getUnSuccessRequests();
        // join data in three three phase
        data = Stream.concat(data.stream(), storeThread.getRecordList().stream())
            .collect(Collectors.toList());
      }

      // total number of successful requests sent
      System.out.println("\n--------------------\n");
      System.out.println("Total number of successful requests sent: " + successRequests);
      System.out.println("Total number of unsuccessful requests sent: " + unSucceddRequests);
      // write record data of every request into CSV file
      // System.out.println("record size:" + data.size());
      String CSV_FILE_PATH = "./result.csv";
      writeData(CSV_FILE_PATH,data);
    } catch (Exception e) {
      System.err.println("Exception occurs when calling PurchaseApi#newPurchase");
      e.printStackTrace();
    }

    Double wallTime = (endTime - startTime)/1000.0;
    System.out.println("Wall Time: " + wallTime + "s");
    // Print out throughput: requests per second = total number of requests/wall time
    System.out.println("Throughput is: " + (double) successRequests / (double) wallTime + "s");

    // get response time from data
    List<Integer> responseTimeList = new ArrayList<>();
    getResData(data,responseTimeList);

    // mean response time for POSTs (millisecs)
    double meanRes = responseTimeList.stream().mapToDouble(val -> val).average().orElse(0.0);
    System.out.println("Mean response time for POSTs:" + meanRes + " ms");

    // median response time for POSTs (millisecs)
    DoubleStream sortedAges = responseTimeList.stream().mapToDouble(val -> val).sorted();
    double medianRes = responseTimeList.size()%2 == 0?
        sortedAges.skip(responseTimeList.size()/2-1).limit(2).average().getAsDouble():
        sortedAges.skip(responseTimeList.size()/2).findFirst().getAsDouble();
    System.out.println("Median response time for POSTs:" + medianRes + " ms");

    // p99 (99th percentile) response time for POSTs
    long p99Res = percentile(responseTimeList,99);
    System.out.println("p99 (99th percentile) response time for POSTs:" + p99Res + " ms");

    // max response time for POSTs
    int maxRes = Collections.max(responseTimeList);
    System.out.println("Max response time for POSTs:" + maxRes + " ms");
  }

  private static void writeData(String filePath, List<String[]> data)
  {
    //first create file
    File file = new File(filePath);
    try {
      // create FileWriter object with file as parameter
      FileWriter outputfile = new FileWriter(file);

      // create CSVWriter object filewriter object as parameter
      CSVWriter writer = new CSVWriter(outputfile);

      // adding header to csv
      String[] header = { "Request start time", "Response time", "Request type", "Latency(ms)", "Response code"};
      writer.writeNext(header);

      // add data to csv
      for(String[] itemData:data){
//        String[] toWrite = itemData.split(" ");
        writer.writeNext(itemData);
      }

      // closing writer connection
      writer.close();
    }
    catch (IOException e) {
      System.err.println("Error in writing CSV file.");
      e.printStackTrace();
    }
  }

//  private static void readData(String filePath,List<Integer> responseTime) throws Exception{
//    File file = new File(filePath);
//    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
//    for (int i = 0; i < lines.size(); i++) {
//      if(i==0){
//        continue;
//      }else{
//        String[] array = lines.get(i).split(",");
//        String data = array[3].substring(1,array[3].length()-1);
//        responseTime.add(Integer.parseInt(data));
//      }
//    }
//  }

  // Calculate the percentile
  private static long percentile(List<Integer> list, double percentile) {
    Collections.sort(list);
    int index = (int) Math.ceil(percentile / 100.0 * list.size());
    return list.get(index-1);
  }

  private static void getResData(List<String[]> data, List<Integer> resData){
    for (int i = 0; i < data.size(); i++) {
      resData.add(Integer.parseInt(data.get(i)[3]));
    }

  }

}
