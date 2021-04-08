package DB;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MarketDao {
  private static HikariDataSource dataSource;

  public MarketDao() {
    dataSource = HikariCPDataSource.getDataSource();
  }

  public void createMarketDao(String storeID,  String customerID, String orderDate, String purchase) {
    String insertQueryStatement =
        "INSERT INTO marketOrder (storeID, customerID,orderDate, purchase) " +
            "VALUES (?,?,?,?)";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement)) {

      preparedStatement.setString(1, storeID);
      preparedStatement.setString(2, customerID);
      preparedStatement.setString(3, orderDate);
      preparedStatement.setString(4, purchase);

      // execute insert SQL statement
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


}
