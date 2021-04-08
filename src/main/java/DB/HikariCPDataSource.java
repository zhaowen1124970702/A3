package DB;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HikariCPDataSource {
  private static Logger logger = LoggerFactory.getLogger(HikariCPDataSource.class);
//  private static Logger logger = Logger.getLogger(HikariCPDataSource.class);

  //  private static BasicDataSource dataSource;
  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource dataSource;

  // NEVER store sensitive information below in plain text!
//  private static final String HOST_NAME = System.getProperty("HOST_NAME");
//  private static final String PORT = System.getProperty("PORT");
//  private static final String DATABASE = System.getProperty("DATABASE");;
//  private static final String USERNAME = System.getProperty("USERNAME");
//  private static final String PASSWORD = System.getProperty("PASSWORD");

  private static final String HOST_NAME = "127.0.0.1";
  private static final String PORT = "3306";
  private static final String DATABASE = "A3_Supermarket";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "Mf9005061123";

//  private static final String HOST_NAME = "a2supermarket.cabss680rsch.us-east-1.rds.amazonaws.com";
//  private static final String PORT = "3306";
//  private static final String DATABASE = "A2_Supermarket";
//  private static final String USERNAME = "admin";
//  private static final String PASSWORD = "Mf9005061123";

  static {
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    logger.info("USERNAME: "+USERNAME);
    logger.info("PASSWORD: "+PASSWORD);
    logger.info("DATABASE: "+DATABASE);
    logger.info("HOST_NAME: "+HOST_NAME);
    logger.info("PORT: "+PORT);

    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    config.setJdbcUrl(url);
    config.setUsername(USERNAME);
    config.setPassword(PASSWORD);
//    config.setConnectionTimeout(2000);
//    config.setConnectionTimeout(300000);
//    config.setLeakDetectionThreshold(300000);
//    config.setMaximumPoolSize(256);

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "500");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.setConnectionTimeout(30000);
    config.setMaximumPoolSize(30);
    config.setMinimumIdle(5);
    config.setMaxLifetime(180000);
    dataSource = new HikariDataSource(config);
  }

  public static HikariDataSource getDataSource() {
    return dataSource;
  }


}
