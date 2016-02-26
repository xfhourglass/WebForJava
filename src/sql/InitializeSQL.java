package sql;

import java.sql.*;

/**
 * Project:sql
 * <p>
 * Author:Crazy_LeoJay
 * Time:上午9:03
 */
public class InitializeSQL {

    private String db_name = "";
    private String userName = "";
    private String password = "";

    Statement statement = null;

    public static void main(String[] args) {
        InitializeSQL initializeSQL = new InitializeSQL("crawler", "root", "");
        try {
            System.out.println("开始写入");
            initializeSQL.query();
            System.out.println("写入完成");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据库
     *
     * @param db_name  数据库名称
     * @param userName 用户名
     * @param password 密码
     */
    public InitializeSQL(String db_name, String userName, String password) {
        this.db_name = db_name;
        this.userName = userName;
        this.password = password;
    }

    private Statement start() {
        Connection conn = null;
        Statement stmt = null;
        String sql;
        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
        // 避免中文乱码要指定useUnicode和characterEncoding
        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
        // 下面语句之前就要先创建javademo数据库
        String url = "jdbc:mysql://localhost:3306/" + db_name + "?";
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
//            sql = "create table webList(NO char(20),name varchar(20),primary key(NO))";
//            int result = stmt.executeUpdate(sql);// executeUpdate语句会返回一个受影响的行数，如果返回-1就没有成功
//            if (result != -1) {
//                System.out.println("创建数据表成功");
//                sql = "insert into student(NO,name) values('2012001','陶伟基')";
//                result = stmt.executeUpdate(sql);
//                sql = "insert into student(NO,name) values('2012002','周小俊')";
//                result = stmt.executeUpdate(sql);
//                sql = "select * from student";
//                ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值
//                System.out.println("学号\t姓名");
//                while (rs.next()) {
//                    System.out
//                            .println(rs.getString(1) + "\t" + rs.getString(2));// 入如果返回的是int类型可以用getInt()
//                }
//            }
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("数据库连接失败！");
            e.printStackTrace();
        } finally {
            return stmt;
        }
    }

    public void insert(int id, String page, String grop, String storey, String type, String content) throws SQLException {
        statement = start();
        statement.execute("INSERT INTO webList(string1)" +
                " VALUES (" + id + "," + page + "," + grop + "," + storey + "," + type + "," + content + ")");
        statement.close();
    }

    public void query(String tabe, String valus) throws SQLException {
        statement = start();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM `webList` WHERE " + tabe + "=" + valus);

        statement.close();
    }

    public void query() throws SQLException {
        statement = start();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM `webList` WHERE 1");
//        boolean next = resultSet.next();
        String s = resultSet.getArray(0).toString();
        if (resultSet.next()){
//            resultSet.getNString(1);
        }
        statement.close();
    }

    private static final String string1 = " `id`, `page`, `grop`, `storey`, `type`, `content` ";
}
