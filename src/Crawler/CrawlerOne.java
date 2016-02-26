package Crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project:PACKAGE_NAME
 * <p>
 * Author:Crazy_LeoJay
 * Time:下午10:15
 */
public class CrawlerOne {

    private static Statement statement;
    private static final String urlString = "http://www.runoob.com/";
    private static final String saveFile = "/home/leojay/web/runoob";
    private static final int SIZE = 50;
    private static final int MIN_WAY = 5;

    public static void main(String[] args) throws Exception {

        statement = startSQL();
        saveURL(urlString);
        saveURL(urlString + "index.html");


        String s = queryURL(3);

        httpGet(s);

        System.out.println("总共 : " + 3);
        statement.close();
    }


    private static void httpGet(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            HttpGet httpget = new HttpGet(url);
            System.out.println("executing request " + httpget.getURI());

            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            /*
            //print the content of the page
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
            */
//            parsePage.parseFromString(responseBody,conn);
            pageSet(responseBody);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.close();
        }
    }

    private static void pageSet(String s) {
        String setRule = "\"(http://[^\"]*)\"";
        Pattern pattern = Pattern.compile(setRule, 2 | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            String string = matcher.group();
            System.out.println(string);
            saveURL(string);
        }
    }

    private static Statement startSQL() {
        Statement statement = null;
        String url = "jdbc:mysql://localhost:3306/crawler?";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("成功加载MySQL驱动程序");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 一个Connection代表一个数据库连接
        try {
            Connection root = DriverManager.getConnection(url, "root", "");
            statement = root.createStatement();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    private static void saveURL(String s) {
        String[] split = s.replace("\"", "").split("://");
        String urlType = split[0];
        String initUrl = split[1];

        String[] split1 = initUrl.split("/");
        String s1 = split1[split1.length - 1];
        boolean contains = s1.replace(split1[0], "").contains(".");
        /*
        * 当 type = 0 表示错误
        * 当 type = 1 表示路径
        * 当 type = 2 表示文件
        * */
        int type = 0;
        if (contains) {
            type = 2;
            if (s1.contains(".html")){
                type = 3;
            }
        } else {
            type = 1;
        }

//        statement = startSQL();
        String query_sql = "SELECT * FROM `webList` " +
                "WHERE content = '" + initUrl + "'";

        String insert_sql = "INSERT INTO `webList`(`type`, `urlType`, `content`) " +
                "VALUES ('" + type + "','" + urlType + "','" + initUrl + "');";

        String update_sql = "UPDATE `webList` SET " +
                "`type`= '" + type + "' WHERE content = '" + initUrl + "';";
        try {
            ResultSet resultSet = statement.executeQuery(query_sql);
            if (!resultSet.next()) {
                statement.execute(insert_sql);
            }else{
                statement.execute(update_sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        finally {
//            try {
//                statement.close();
//                System.out.println("成功关闭MySQL驱动程序");
//            } catch (SQLException e) {
//                e.printStackTrace();
//                System.out.println("成功关闭MySQL驱动程序");
//            }
//        }
    }

    private static String queryURL(int type) {
        String content = null;
        String query_sql = "SELECT * FROM `webList` WHERE type = '" + type + "'";
        try {
            ResultSet resultSet = statement.executeQuery(query_sql);
            resultSet.next();
            content = resultSet.getString("content");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return content;
    }

}
