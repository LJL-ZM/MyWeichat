package myweixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;

public class SqlManager {
    private static final String URL = "jdbc:mysql://localhost:3306/java_demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PWD = "YOUR_DB_PASSWORD"; 

    public static List<String> getTableColumns(String tableName) {
        List<String> columns = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, "java_demo", tableName, null);
            while (rs.next()) {
                String column = rs.getString("COLUMN_NAME");
                if (!"id".equalsIgnoreCase(column)) {
                    columns.add(column);
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columns;
    }

    public static int insert(String insertSql) {
        int insertRows = -1;
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             Statement statement = conn.createStatement()) {
            insertRows = statement.executeUpdate(insertSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return insertRows;
    }

    public static int delete(String tableName, String judge) {
        int deleteRows = -1;
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             Statement statement = conn.createStatement()) {
            String deleteSql = "DELETE FROM " + tableName + " WHERE " + judge;
            deleteRows = statement.executeUpdate(deleteSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deleteRows;
    }

    public interface ResultSetHandler {
        void handle(ResultSet rs) throws SQLException;
    }

    public static void queryAll(String tableName, ResultSetHandler handler) {
        String sql = "SELECT * FROM " + tableName;
        executeQueryOld(sql, handler);
    }

    public static void queryIf(String tableName, String condition, ResultSetHandler handler) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + condition;
        executeQueryOld(sql, handler);
    }

    private static void executeQueryOld(String sql, ResultSetHandler handler) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            handler.handle(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> listAll(String tableName) {
        String sql = "SELECT * FROM " + tableName;
        return executeQueryNew(sql);
    }

    public static List<Map<String, Object>> listByCondition(String tableName, String whereCondition) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + whereCondition;
        return executeQueryNew(sql);
    }

    public static List<Map<String,Object>> listBySql(String sql){
        return executeQueryNew(sql);
    }

    private static List<Map<String, Object>> executeQueryNew(String sql) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object columnValue = rs.getObject(i);
                    rowMap.put(columnName, columnValue);
                }
                resultList.add(rowMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public static int update(String updateSql) {
        int updateRows = -1;
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             Statement statement = conn.createStatement()) {
            updateRows = statement.executeUpdate(updateSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateRows;
    }

    public static int insertReturnKey(String insertSql) {
        int generatedId = -1;
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             Statement statement = conn.createStatement()) {
            statement.executeUpdate(insertSql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public interface TransactionHandler {
        void execute(Connection conn) throws SQLException, Exception;
    }

    public static boolean executeTransaction(TransactionHandler handler) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PWD);
            conn.setAutoCommit(false);
            handler.execute(conn);
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}