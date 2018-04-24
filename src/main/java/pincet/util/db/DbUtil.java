package pincet.util.db;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.*;
import java.util.List;
import java.util.Set;

import static pincet.util.db.DbUtil.KeyType.FK;
import static pincet.util.db.DbUtil.KeyType.PK;

public class DbUtil {
  private static final DbUtil o = new DbUtil();

//    private static final List<String> SYS_ORACLE_SCHEMAS = Arrays.asList("SYS", "SYSTEM", "SYSDBA", "SYSOPER");
//    private static final List<String> SYS_MYSQL_SCHEMAS = Arrays.asList("INFORMATION_SCHEMA", "PERFORMANCE_SCHEMA", "MYSQL");
//    private static final List<String> SYS_POSTGRES_SCHEMAS = Arrays.asList("INFORMATION_SCHEMA", "PG_");
//    private static final List<String> SYS_MSSQL_SCHEMAS = Arrays.asList("INFORMATION_SCHEMA", "PG_");

  private DbUtil() {
  }

  public static DbUtil get() {
    return o;
  }

  private static void _printResultSet(ResultSet resultSet) throws SQLException {
    ResultSetMetaData m = resultSet.getMetaData();
    int len = m.getColumnCount();
    while (resultSet.next()) {
      for (int i = 1; i <= len; i++) {
        System.out.println(i + ":" + m.getColumnName(i) + ":" + resultSet.getString(i));
      }
      System.out.println();
    }
  }

  /**
   * {@link DbUtil#tables(DataSource, String, String)}
   */
  public List<TableDefine> tables(DataSource dataSource) throws SQLException {
    return tables(dataSource, null, null);
  }

  /**
   * {@link DbUtil#tables(DataSource, String, String)}
   */
  public List<TableDefine> tables(DataSource dataSource, String tablePattern) throws SQLException {
    return tables(dataSource, null, tablePattern);
  }

  /**
   * 주어진 DataSource 객체를 사용하여 테이블과 스키마 패턴에 해당하는 이름을 가진 테이블의 정보를 반환
   * !import -> *허용 - 패턴에 * 를 기입할 경우 자동으로 %로 변환
   *
   * @param dataSource    - java DataSource object
   * @param schemaPattern - schema sql expression ex) EPO%
   * @param tablePattern  - table sql expression ex) EPO%
   * @return - 테이블 정보
   * @throws SQLException
   * @see DataSource
   * @see TableDefine
   * @see ColumnDefine
   */
  public List<TableDefine> tables(DataSource dataSource, @Nullable String schemaPattern, @Nullable String tablePattern) throws SQLException {
    List<TableDefine> result = Lists.newArrayList();
    if (dataSource == null) throw new SQLException("DataSource is null!");
    if (tablePattern != null && (tablePattern.startsWith("%") || tablePattern.startsWith("SYS_") || tablePattern.startsWith("PG_")))
      throw new SQLException("table pattern argument can not starts with 'PG_', 'SYS_' or '%'.");

    if (schemaPattern != null && schemaPattern.contains("*"))
      schemaPattern = schemaPattern.replaceAll("\\*", "\\%");
    if (tablePattern != null && tablePattern.contains("*"))
      tablePattern = tablePattern.replaceAll("\\*", "\\%");
    Connection conn = dataSource.getConnection();
    try {
      DatabaseMetaData metaData = conn.getMetaData();
      ResultSet metaDataTables = metaData.getTables(null, schemaPattern, tablePattern == null ? "%" : tablePattern, new String[]{"TABLE"});
      while (metaDataTables != null && metaDataTables.next()) {
        String tableName = metaDataTables.getString(3);
        String schema = metaDataTables.getString(2);

        List<ColumnDefine> columnDefines = Lists.newArrayList();
        Statement stmt = null;
        ResultSet r = null;
        try {
          stmt = conn.createStatement();
          r = stmt.executeQuery("SELECT * FROM " + schema + "." + tableName + " WHERE 1<0");
          ResultSetMetaData m = r.getMetaData();
          int c = m.getColumnCount();

          for (int i = 1; i <= c; i++) {
            String javaType = m.getColumnClassName(i);
            String colName = m.getColumnName(i);
            String dbType = m.getColumnTypeName(i);
            int length = m.getColumnDisplaySize(i);
            boolean isNullable = m.isNullable(i) == 1;
            ColumnDefine define = new ColumnDefine(
                colName, dbType, javaType, length, isNullable
            );
            columnDefines.add(define);
          }
          result.add(new TableDefine(schema, tableName, columnDefines));
        } finally {
          if (stmt != null) {
            stmt.close();
          }
          if (r != null) {
            r.close();
          }
        }
      }
    } finally {
      if (!conn.isClosed())
        conn.close();
    }
    return result;
  }

  /**
   * {@link DbUtil#tableNames(DataSource, String, String)}
   */
  public List<String> tableNames(DataSource dataSource) throws SQLException {
    return tableNames(dataSource, null, null);
  }

  /**
   * {@link DbUtil#tableNames(DataSource, String, String)}
   */
  public List<String> tableNames(DataSource dataSource, String tablePattern) throws SQLException {
    return tableNames(dataSource, null, tablePattern);
  }

  /**
   * 스키마 명.테이블 명을 얻어옴
   *
   * @param dataSource    - java DataSource object
   * @param schemaPattern - schema sql expression ex) EPO%
   * @param tablePattern  - table sql expression ex) EPO%
   * @return {schemaName}.{tableName}
   * @throws SQLException
   */
  public List<String> tableNames(DataSource dataSource, @Nullable String schemaPattern, @Nullable String tablePattern) throws SQLException {
    Connection conn = dataSource.getConnection();
    List<String> result = Lists.newArrayList();
    try {
      DatabaseMetaData metaData = conn.getMetaData();
      ResultSet metaDataTables = metaData.getTables(null, schemaPattern, tablePattern == null ? "%" : tablePattern, new String[]{"TABLE"});
      while (metaDataTables.next()) {
        result.add(metaDataTables.getString(3) + "." + metaDataTables.getString(2));
      }
    } finally {
      if (!conn.isClosed())
        conn.close();
    }
    return result;
  }

  /**
   * 해당 테이블의 PK 키 조회
   *
   * @param dataSource - java DataSource object
   * @param schema     - schema sql expression ex) EPO%
   * @param table      - table sql expression ex) EPO%
   * @return a set of TableKey
   * @throws SQLException
   * @see TableKey
   */
  public Set<TableKey> pk(DataSource dataSource, String schema, String table) throws SQLException {
    Set<TableKey> result = Sets.newHashSet();
    Connection conn = dataSource.getConnection();
    try {
      DatabaseMetaData metaData = conn.getMetaData();
      ResultSet primaryKeys = metaData.getPrimaryKeys(null, schema, table);

      while (primaryKeys.next()) {
        result.add(_createKey(primaryKeys, true));
      }
    } finally {
      if (!conn.isClosed())
        conn.close();
    }
    return result;
  }

  /**
   * 해당 테이블의 FK 키 및 해당하는 PK 조회
   *
   * @param dataSource - java DataSource object
   * @param schema     - schema sql expression ex) EPO%
   * @param table      - table sql expression ex) EPO%
   * @return a set of TableKey
   * @throws SQLException
   * @see TableKey
   */
  public Set<TableKey> fk(DataSource dataSource, String schema, String table) throws SQLException {
    Set<TableKey> result = Sets.newHashSet();
    Connection conn = dataSource.getConnection();
    try {
      DatabaseMetaData metaData = conn.getMetaData();
      ResultSet foreignKeys = metaData.getImportedKeys(null, schema, table);
//        _printResultSet(foreignKeys);
      while (foreignKeys.next()) {
        result.add(_createKey(foreignKeys, false));
      }
    } finally {
      if (!conn.isClosed())
        conn.close();
    }
    return result;
  }

  private TableKey _createKey(ResultSet r, boolean isPk) throws SQLException {
    if (isPk) {
      String schema = r.getString(2);//"TABLE_SCHEM"
      String tableName = r.getString(3);//"TABLE_NAME"
      String column = r.getString(4);//"COLUMN_NAME"
      String pkName = r.getString(6);//"PK_NAME"
      return new TableKey(schema, tableName, pkName, column);
    } else {
      String schema = r.getString(6);//"TABLE_SCHEM"
      String tableName = r.getString(7);//"TABLE_NAME"
      String column = r.getString(8);
      String fkName = r.getString(12);

      TableKey pkKey =
          new TableKey(r.getString(2)//"PKTABLE_SCHEM"
              , r.getString(3)//"PKTABLE_NAME"
              , r.getString(13)//"PK_NAME"
              , r.getString(4)//"PKCOLUMN_NAME"
          );
      return new TableKey(schema, tableName, fkName, column, pkKey);
    }
  }

  enum KeyType {
    PK, FK
  }

  public static final class TableKey implements Serializable {
    private static final long serialVersionUID = -3676251465152062878L;
    final KeyType type;
    final String schema;
    final String table;
    final String key;
    final String column;
    Set<TableKey> pk;

    TableKey(String schema, String table, String key, String column) {
      this.schema = schema;
      this.table = table;
      this.key = key;
      this.column = column;
      this.type = PK;
    }

    TableKey(String schema, String table, String key, String column, TableKey pkKey) {
      this.schema = schema;
      this.table = table;
      this.key = key;
      this.column = column;
      this.pk = Sets.newHashSet();
      this.pk.add(pkKey);
      this.type = FK;
    }

    public String schema() {
      return schema;
    }

    public String table() {
      return table;
    }

    public String key() {
      return key;
    }

    public String column() {
      return column;
    }

    public boolean isPk() {
      return type.equals(PK);
    }

    public Set<TableKey> pk() {
      return pk;
    }

    public KeyType type() {
      return type;
    }

    @Override
    public String toString() {
      return "TableKey{" +
          "type='" + type + '\'' +
          ", schema='" + schema + '\'' +
          ", table='" + table + '\'' +
          ", key='" + key + '\'' +
          ", column='" + column + '\'' +
          ", pk=" + pk +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TableKey tableKey = (TableKey) o;
      return Objects.equal(schema, tableKey.schema) &&
          Objects.equal(table, tableKey.table) &&
          Objects.equal(key, tableKey.key) &&
          Objects.equal(column, tableKey.column) &&
          Objects.equal(type, tableKey.type);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(schema, table, key, column, type);
    }
  }

  public static final class ColumnDefine implements Serializable {
    private static final long serialVersionUID = -8541140583291365330L;
    final String columnName;
    final String dbType;
    final String javaType;
    final int length;
    final boolean nullable;
    boolean isPk;

    ColumnDefine(String columnName, String dbType, String javaType, int length, boolean isNullable) {
      this.columnName = columnName;
      this.dbType = dbType;
      this.javaType = javaType;
      this.length = length;
      this.nullable = isNullable;
    }

    public String name() {
      return columnName;
    }

    public String dbType() {
      return dbType;
    }

    public Class<?> javaType() throws ClassNotFoundException {
      return Class.forName(javaType);
    }

    public int length() {
      return length;
    }

    public boolean nullable() {
      return nullable;
    }

    public boolean isPk() {
      return isPk;
    }

    @Override
    public String toString() {
      return "ColumnDefine{" +
          "columnName='" + columnName + '\'' +
          ", dbType='" + dbType + '\'' +
          ", javaType=" + javaType +
          ", length=" + length +
          ", nullable=" + nullable +
          ", isPk=" + isPk +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ColumnDefine that = (ColumnDefine) o;
      return length == that.length &&
          nullable == that.nullable &&
          isPk == that.isPk &&
          Objects.equal(columnName, that.columnName) &&
          Objects.equal(dbType, that.dbType) &&
          Objects.equal(javaType, that.javaType);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(columnName, dbType, javaType, length, nullable, isPk);
    }
  }

  public static final class TableDefine implements Serializable {
    private static final long serialVersionUID = -7481452617398123869L;
    final String schema;
    final String name;
    final List<ColumnDefine> columns;
    Set<TableKey> keys;

    TableDefine(String schema, String name, List<ColumnDefine> columnDefines) {
      this.schema = schema;
      this.name = name;
      this.columns = columnDefines;
    }

    public String schema() {
      return schema;
    }

    public String name() {
      return name;
    }

    public List<ColumnDefine> columns() {
      return columns;
    }

    public ColumnDefine column(String colName) {
      for (ColumnDefine c : columns) {
        if (c.columnName.equals(colName)) {
          return c;
        }
      }
      return null;
    }

    public void applyKey(TableKey key) {
      for (ColumnDefine d : columns) {
        if ((key.schema.equals(schema) && key.table.equals(name))
            && key.column.equals(d.columnName)) {
          if (key.isPk()) d.isPk = true;
          if (keys == null) keys = Sets.newHashSet();
          keys.add(key);
          break;
        }
      }
    }

    @Override
    public String toString() {
      return "TableDefine{" +
          "schema='" + schema + '\'' +
          ", name='" + name + '\'' +
          ", columns=" + columns +
          ", keys=" + keys +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TableDefine that = (TableDefine) o;
      return Objects.equal(schema, that.schema) &&
          Objects.equal(name, that.name) &&
          Objects.equal(columns, that.columns) &&
          Objects.equal(keys, that.keys);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(schema, name, columns, keys);
    }
  }
}
