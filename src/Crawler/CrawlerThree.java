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
 * Time:下午10:06
 */
/**
 * 这是一个较为完善的爬虫，当然还存在一些问题
 * 1、在保存相对网址的那一段代码，应该提取当前保存路径然后接上相对路径
 * 2、还有在页面中，一些链接应处理为相对路径。
 * 解决方案：
 * 重新构造类，将个参数写成接口，以方便调用，和忘记配置。
 * */
public class CrawlerThree {
    private static final String db_name = "crawler";
    private static final String table_name = "runoobList";
    private static final String user_name = "root";
    private static final String password = "";

    private static final String URL = "http://www.runoob.com/";
    private static final String fileURL = "/home/leojay/webDownload2/";

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

    private static void search(String setRule, String page, String fileType){
        Pattern p = Pattern.compile(setRule, Pattern.DOTALL);
        Matcher m = p.matcher(page);
        while (m.find()) {
            String group = m.group();
            //提取 URL
            String replace = group.replace("\"", "");
            if (!replace.contains(URL)) {
                replace = URL + replace;
            }

            //存入数据库
            String[] titleTab = new String[]{"fileType", "urltype", "urlContent"};
            String[] values = new String[]{fileType, null, replace};
            int update = 0;
            try {
                update = open.update(table_name, titleTab, values, "urlContent = '" + replace + "'");
//                open.query(table_name, null, "urlContent = '" + replace + "'");
                if (update == 0) {
                    open.insert(table_name, titleTab, values);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setPage(String url, String fileURL){
        if (!url.isEmpty() && !url.equals("/") && !url.equals("javascript:void(0)")) {
            //数据库参数
            String fileType = null;
//            String urlType = null;
            String page = null;
            //URL分析
            char c = url.charAt(url.toCharArray().length - 1);
            if (c == '/') {
                url += "index.html";
            }

            String[] test = {"html", "css"};
            for (String s : test) {
                if (url.contains("." + s)) {
                    fileType = s;
                    break;
                }
            }


            //URL存入数据库
            String[] titleTab = new String[]{"fileType", "urltype", "urlContent"};
            try {
                ResultSet query = open.query(table_name, null, "urlContent = '" + url + "'");
                if (!query.first()) {
                    open.insert(table_name, titleTab, new String[]{fileType, null, url});
                }
            } catch (SQLException e) {
                e.printStackTrace();

            }

            //提取URL页面
            try {
                page = httpGetPage(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //page 分析 并保存URL
            switch (fileType) {
                case "html":
                    search("\"[^\"]*.html\"", page, "html");
                    search("\"[^\"]*.css\"", page, "css");
                    break;
                case "css":
                    search("url\\(\'[^\']*\'\\)", page, "cssUrl");
                    break;
                default:
                    search("(?<=herf=)\"[^\"]*\"", page, "other");
                    break;
            }
            //页面page 写入文档
            //新建路径 和文件

            String replace = url.replace("http://", "");
            String[] split1 = replace.split("/");
            String fileURL2 = fileURL +  replace.replace(split1[split1.length - 1], "");
            try {
                File file = new File(fileURL2);
                boolean mkdir = file.mkdirs();
                file = new File(fileURL + replace);
                boolean newFile = file.createNewFile();

                BufferedWriter writer = new BufferedWriter(new FileWriter(fileURL + replace));
                writer.write(page);
                writer.flush();
                writer.close();

            } catch (IOException e) {
                System.out.println("错误： " + e.getMessage());

            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        open = new SQLOpen(db_name, user_name, password);
        open.start();
        setPage(URL, fileURL);
        int i = 102;
        while(true){
            ResultSet query = open.query(table_name, null, "id='" + i + "'");
            if (query.first()){
                String urlContent = query.getString("urlContent");
                setPage(urlContent, fileURL);
                System.out.println("完成 " + i + " 个");
                i++;
            }else {
                break;
            }
        }
        open.close();
    }
}
