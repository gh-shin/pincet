/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.util.net;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by SteveShin on 2015-09-15.
 * <p>
 * Available DB url classes
 * Apache Derby    Derby	org.apache.derby.jdbc.ClientDataSource
 * Firebird    Jaybird	org.firebirdsql.pool.FBSimpleDataSource
 * H2	H2	org.h2.jdbcx.JdbcDataSource
 * IBM AS400	IBM	com.ibm.as400.access.AS400JDBCDriver
 * HSQLDB	HSQLDB	org.hsqldb.jdbc.JDBCDataSource
 * IBM AS400	IBM	com.ibm.as400.access.AS400JDBCDriver
 * IBM DB2	DB2	com.ibm.db2.jcc.DB2SimpleDataSource
 * MariaDB, MySQL	MariaDB	org.mariadb.jdbc.MySQLDataSource
 * MySQL	Connector/J	com.mysql.jdbc.jdbc2.optional.MysqlDataSource
 * MS SQL Server	Microsoft	com.microsoft.sqlserver.jdbc.SQLServerDataSource
 * Oracle	Oracle	oracle.jdbc.pool.OracleDataSource
 * PostgreSQL	pgjdbc-ng	com.impossibl.postgres.jdbc.PGDataSource
 * PostgreSQL	PostgreSQL	org.postgresql.ds.PGSimpleDataSource
 * SAP MaxDB	SAP	com.sap.dbtech.jdbc.DriverSapDB
 * SyBase	jConnect	com.sybase.jdbc4.jdbc.SybDataSource
 * </p>
 */
public class ConnectionPool {
  private volatile HikariDataSource dataSource;

  public static Builder builder() {
    return new Builder();
  }

  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private ConnectionPool(Builder builder) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(builder.url);
    if (builder.driverClassName != null) {
      config.setDriverClassName(builder.driverClassName);
    }
    config.setUsername(builder.id);
    config.setPassword(builder.pw);
    config.setMaximumPoolSize(builder.maxPoolSize);
    config.setReadOnly(builder.readOnly);
    config.setAutoCommit(builder.autoCommit);
    config.setConnectionTimeout(builder.connectionTimeout);
    config.setIdleTimeout(builder.idleTimeout);
    config.setMinimumIdle(builder.minIdle);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    this.dataSource = new HikariDataSource(config);
  }

  public static class Builder {
    private String id;
    private String pw;
    private String url;
    private int maxPoolSize = 20;
    private boolean readOnly = true;
    private boolean autoCommit = false;
    private long connectionTimeout = 30 * 1000;//30 sec
    private long idleTimeout = 10 * 60 * 1000;//10 min
    private int minIdle = 3;
    private String driverClassName;

    public synchronized final Builder id(String id) {
      this.id = id;
      return this;
    }

    public synchronized final Builder pw(String pw) {
      this.pw = pw;
      return this;
    }

    public synchronized final Builder url(String url) {
      this.url = url;
      return this;
    }

    public synchronized final Builder maxPoolSize(int max) {
      this.maxPoolSize = max;
      return this;
    }

    public synchronized final Builder readOnly(boolean readOnly) {
      this.readOnly = readOnly;
      return this;
    }

    public synchronized final Builder autoCommit(boolean autoCommit) {
      this.autoCommit = autoCommit;
      return this;
    }

    public synchronized final Builder connectionTimeout(long connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
      return this;
    }

    public synchronized final Builder idleTimeout(long idleTimeout) {
      this.idleTimeout = idleTimeout;
      return this;
    }

    public synchronized final Builder minIdle(int minIdle) {
      this.minIdle = minIdle;
      return this;
    }

    public synchronized final Builder driverClassname(String driverClassname) {
      this.driverClassName = driverClassname;
      return this;
    }

    public synchronized final ConnectionPool build() {
      if (url == null || id == null || pw == null)
        throw new NullPointerException("ConnectionPool.Builder parameter is not enough. url, id, pw check it.");
      return new ConnectionPool(this);
    }
  }
}
