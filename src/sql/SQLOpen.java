package sql;

import java.sql.*;

/**
 * Project:sql
 * <p>
 * Author:Crazy_LeoJay
 * Time:下午10:23
 */
public class SQLOpen {
    private String db_name = "";
    private String userName = "";
    private String password = "";
    private String url = null;

    private static Statement stmt = null;
    private static boolean isOpen = false;

    /**
     * 初始化
     *
     * @param db_name  数据库名称
     * @param userName 用户名
     * @param password 密码
     */
    public SQLOpen(String db_name, String userName, String password) {
        this.db_name = db_name;
        this.userName = userName;
        this.password = password;
        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
        this.url = "jdbc:mysql://localhost:3306/" + db_name + "?";
    }

    /**
     * 链接数据库
     */
    public Statement start() {
        if (!isOpen) {
            Connection conn = null;
            try {
                // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
                // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
                Class.forName("com.mysql.jdbc.Driver");
//             or:
//             com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
//             or：
//             new com.mysql.jdbc.Driver();
                System.out.println("成功加载MySQL驱动程序");
                // 一个Connection代表一个数据库连接

                conn = DriverManager.getConnection(url, userName, password);

                // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
                stmt = conn.createStatement();
                isOpen = true;
            } catch (ClassNotFoundException e) {
                System.out.println("找不到驱动程序类 ，加载驱动失败！");
                e.printStackTrace();
                isOpen = false;
            } catch (SQLException e) {
                System.out.println("数据库连接失败！");
                e.printStackTrace();
                isOpen = false;
            }
        }
        return stmt;
    }

    /**
     * 关闭数据库
     */
    public void close() throws SQLException {
        stmt.close();
        isOpen = false;
    }

    /**
     * 插入一条信息
     *
     * @param tableName 表名称
     * @param titleTab  列名
     * @param values    列对应值
     */
    public int insert(String tableName, String[] titleTab, String[] values) throws SQLException {

        String insert_sql = "INSERT INTO `" + tableName + "` (" + initStringD(titleTab) + " )" +
                " VALUES (" + initString(values) + ")";
        return stmt.executeUpdate(insert_sql);

    }

    /**
     * 删除信息
     *
     * @param tableName 表名
     * @param where     条件 如果 where == null 则删除表
     */
    public void delete(String tableName, String where) throws SQLException {
        String delete_sql = null;
        if (where == null || where.isEmpty()) {
            delete_sql = "DELETE FROM `" + tableName + "`;";
        } else {
            delete_sql = "DELETE FROM `" + tableName + "` WHERE " + where + ";";
        }
        stmt.executeUpdate(delete_sql);
    }

    /**
     * 修改数据
     *
     * @param tableName 表名
     * @param titleTab  表头
     * @param values    表头对应值
     * @param where     条件
     */
    public int update(String tableName, String[] titleTab, String[] values, String where) throws SQLException {
        String update_sql = "UPDATE `" + tableName + "` SET " + initString(titleTab, values) + " WHERE " + where + ";";
        return stmt.executeUpdate(update_sql);
    }

    /**
     * 查询
     *
     * @param tableName 表名
     * @param titleTab  查询表头
     * @param where     条件
     * @return 返回 resultSet 对象
     */
    public ResultSet query(String tableName, String[] titleTab, String where) throws SQLException {
        String string = null;
        if (titleTab == null || titleTab.equals("")) {
            string = " * ";
        } else {
            string = initString(titleTab);
        }
        String query_sql = "SELECT " + string + " FROM `" + tableName + "` WHERE " + where + ";";

        return stmt.executeQuery(query_sql);
    }

    private static String initString(String[] strings) {
        String s = "";
        int length = strings.length - 1;
        for (int i = 0; i < length; i++) {
            s += "'" + strings[i] + "', ";
        }
        s += "'" + strings[length] + "'";
        return s;
    }

    private static String initStringD(String[] strings) {
        String s = "";
        int length = strings.length - 1;
        for (int i = 0; i < length; i++) {
            s += "`" + strings[i] + "`, ";
        }
        s += "`" + strings[length] + "`";
        return s;
    }

    private static String initString(String[] tab, String[] values) {
        String s = "";
        int length = tab.length - 1;
        for (int i = 0; i < length; i++) {
            s += "`" + tab[i] + "` = '" + values[i] + "', ";
        }
        s += "`" + tab[length] + "` = '" + values[length] + "'";
        return s;
    }

    public static void main(String[] args) {
        String s = initString(new String[]{"hello", "xiangduole", "word"}, new String[]{"1", "2", "3"});
        System.out.println(s);
    }

}
