/*
 Follow driver installation and setup instructions here:
 https://www.oracle.com/database/technologies/getting-started-using-jdbc.html
*/

package com.marketpilot.adapters.persistence;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;

public class DBConnection {
    // Replace USER_NAME, PASSWORD with your username and password
    private static String DB_USER;
    private static String DB_PASSWORD;

    private final boolean isConnectionEstablished;

    // If you want to connect using Wallet, comment the following line.
    // private final static String CONNECT_STRING = "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.us-ashburn-1.oraclecloud.com))(connect_data=(service_name=g4393aeaf8e37e6_marketpilotdev_high.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))";

    /*
      If you want to connect with a wallet, uncomment the CONNECT_STRING line below (this is only applicable for Oracle Database 23ai versions and THICK MODE).
      dbname - is the TNS alias present in tnsnames.ora.
    */
    private final static String CONNECT_STRING ="marketpilotdev_high";
    private final static String CONN_FACTORY_CLASS_NAME = "oracle.jdbc.replay.OracleConnectionPoolDataSourceImpl";
    private final PoolDataSource poolDataSource;
    public DBConnection() throws SQLException {
        readCredentials();
        this.poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        poolDataSource.setConnectionFactoryClassName(CONN_FACTORY_CLASS_NAME);

        // If THICK mode is needed, comment the following line. Unset TNS_ADMIN environment variable if you are using THIN mode
        poolDataSource.setURL("jdbc:oracle:thin:@" + CONNECT_STRING);
        /*
		 If THICK mode is needed, uncomment the following poolDataSource.setURL line.
		  Note:
		  1. You should download and unzip the wallet. You should also set the TNS_ADMIN environment variable. Otherwise, it will not work for oci8 driver.
		   TNS_ADMIN - Should be the path where the client credentials zip (wallet_dbname.zip) file is downloaded.
		  2. Edit Wallet location value in sqlnet.ora
		*/
        // poolDataSource.setURL("jdbc:oracle:oci8:@" + CONNECT_STRING);

        poolDataSource.setUser(DB_USER);
        poolDataSource.setPassword(DB_PASSWORD);
        poolDataSource.setConnectionPoolName("JDBC_UCP_POOL");

        isConnectionEstablished = true;
    }

    private void readCredentials() {
        String propsPath = System.getenv("DB_PROPERTIES_PATH");
        if (propsPath == null) {
            System.err.println("DB_PROPERTIES_PATH environment variable is not set");
            return;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propsPath)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("File " + propsPath + " could not be found.");
        }

        DB_USER = props.getProperty("db.user");
        DB_PASSWORD = props.getProperty("db.password");
    }

    public boolean isConnectionEstablished() { return isConnectionEstablished; }

    public boolean testConnection() {
        try {
            try (Connection conn = poolDataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
                if (rs.next()) {
                    System.out.println("Oracle Connection is working! Query result: " + rs.getInt(1));
                    // Create Oracle DatabaseMetaData object
                    DatabaseMetaData meta = conn.getMetaData();

                    // gets driver info:
                    System.out.println("JDBC driver version is " + meta.getDriverVersion());
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Could not connect to the database - SQLException occurred: " + e.getMessage());
            return false;
        }
    }
}