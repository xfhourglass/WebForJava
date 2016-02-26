package Crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import sql.SQLOpen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project:Crawler
 * <p>
 * Author:Crazy_LeoJay
 * Time:下午3:25
 */

/**
 * 半成品， 在analysisPage上有问题（正则规则）
 * 虽然能够下载网页，但资源不全，数据库不够完善
 * */
public class CrawlerTwo {

    private static final String db_name = "crawler";
    private static final String table_name = "webList";
    private static final String user_name = "root";
    private static final String password = "";

    private static final String URL = "http://www.runoob.com/";
    private static final String fileURL = "/home/leojay/webDownload/";

    private static SQLOpen open;

    private static String httpGetPage(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String responseBody = null;

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
            responseBody = httpclient.execute(httpget, responseHandler);
            /*
            //print the content of the page
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
            */
//            parsePage.parseFromString(responseBody,conn);
//            pageSet(responseBody);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.close();
            return responseBody;
        }
    }

    private static void analysisPage(String page) throws SQLException {
        String setRule1 = "\"(http://[^\"]*)\"";
        String setRule2 = "(?>href=)\"[^>]*\"";
        String setRule3 = "(?>rec=)\"[^>]*\"";
        String setRule4 = "(?<=<link).{0,24}(?<=href=)\"[^\"]*\"";
        String setRule5 = "(?<=<link)[^>]*>";
        String setRule6 = "url\\(\'[^\']*\'\\)";

        Pattern pattern = Pattern.compile(setRule1, 2 | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(page);
        while (matcher.find()) {
            String string = matcher.group();
            System.out.println(string);
            saveUrl(string);
        }

    }

    private static void saveUrl(String url) throws SQLException {
        //提取 URL
        String[] split = url.replace("\"", "").split("://");
        String urlType = split[0];
        String initUrl = split[1];// url

        //设置参数
        String[] split1 = initUrl.split("/");
        String s1 = split1[split1.length - 1];
        boolean contains = s1.replace(split1[0], "").contains(".");
            /*
            * 当 type = 0 表示错误
            * 当 type = 1 表示路径
            * 当 type = 2 表示文件
            * 当 type = 3 表示HTML文件
            * */
        int type = 0;
        if (contains) {
            type = 2;
            if (s1.contains(".html")) {
                type = 3;
            }
        } else {
            type = 1;
        }

        //存入数据库
        String[] titleTab = new String[]{"type", "urlType", "content", "state"};
        String[] values = new String[]{type + "", urlType, initUrl, 0 + ""};

//            ResultSet query = open.query(table_name, null, "content = '" + string + "'");
        int update = open.update(table_name, titleTab, values, "content = '" + initUrl + "'");
        if (update == 0) {
            try {
                open.insert(table_name, titleTab, values);
            }catch (Exception e){
                String s = e.getMessage();
                System.out.println("错误 ： " + s);
            }
        }
    }

    private static void savePage(String page, String fileURL) {
        try {
            String[] split = fileURL.split("/");
            boolean isFile = split[split.length - 1].contains(".");
            boolean isFile2 = split[split.length - 1].contains("www");
            File file;
            if (isFile && !isFile2) {
                String s = fileURL.replace(split[split.length - 1], "");
                file = new File(s);
                boolean mkdir = file.mkdirs();
                file = new File(fileURL);
                boolean newFile = file.createNewFile();

                BufferedWriter writer = new BufferedWriter(new FileWriter(fileURL));
                writer.write(page);
                writer.flush();
                writer.close();
            } else {
                file = new File(fileURL);
                boolean mkdir = file.mkdirs();
            }
        }catch (IOException e) {
            System.out.println("错误： " + e.getMessage());

        }


    }

    public static void main(String[] args) throws SQLException, IOException {
        open = new SQLOpen(db_name, user_name, password);
        open.start();

//        saveUrl(URL);
        saveUrl(URL + "index.html");

        int i = 1;
        boolean b = true;

        while (b) {
            ResultSet query = open.query(table_name, null, "id = '" + i + "'");
            if (!query.first()) {
                b = false;
            }else {
                String url = query.getString("content");
                String urlType = query.getString("urlType");
                String page = httpGetPage(urlType + "://" + url);
                analysisPage(page);
                savePage(page, fileURL + url);
                System.out.println("完成文件数：" + i);
                i++;
            }
        }
        open.close();

    }
}
