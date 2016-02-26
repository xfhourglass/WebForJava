package Crawler;

import org.apache.http.HttpEntity;
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
 * Time:下午8:02
 */

/**
 * 此类 在上一个类的基础上进行改进，对于路径的修复已经完成，在相对路径的修改上还有不足，在整个程序的逻辑上略有问题。
 * */
public class CrawLerFour {
    private onSetPage setPage;
    private SQLOpen open;

    public interface onSetPage {
        String setBeginUrl();

        String setSaveLocal();

        String setDbName();

        String setTableName();

        String userName();

        String password();
    }

    public void onStart() {
        open = new SQLOpen(setPage.setDbName(), setPage.userName(), setPage.password());
        open.start();
        String beginUrl = setPage.setBeginUrl();
        saveUrl(beginUrl, "html");
        int i = 102;
        String fgx = "------------------------------------";
        while (true) {
            try {
                System.out.println(fgx);
                ResultSet query = open.query(setPage.setTableName(), null, "id='" + (++i) + "'");
                if (query.first()) {
                    setSinglePage(query.getString("urlContent"));
                    System.out.println("第" + i + "个");
                    System.out.println(fgx);
                } else {
                    break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            setSinglePage(beginUrl);
        }
        try {
            open.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对单个页面的处理
     *
     * @param url 页面路径
     */
    private void setSinglePage(final String url) {

        if (!url.isEmpty() && !url.equals("/") && !url.equals("javascript:void(0)")) {
            //设置参数 fileType
            String fileType = setFileType(url);
            //URL存入数据库
//            saveUrl(url, fileType);
            //提取URL页面
            String page = null;
            try {
                page = httpGetPage(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //page 分析 并保存URL
            if (page != null && !page.isEmpty()) {
                switch (fileType) {
                    case "html":
                        search("\"[^\"]*.html\"", url, page, "html");
                        search("\"[^\"]*.css\"", url, page, "css");
                        break;
                    case "css":
                        search("url\\(\'[^\']*\'\\)", url, page, "cssUrl");
                        break;
                    default:
                        search("(?<=herf=)\"[^\"]*\"", url, page, "other");
                        break;
                }
                //页面page 写入文档
                //新建路径 和文件
                writeFile(url, page);
            }
        }
    }

    /**
     * @param url 路径
     * @return 文件类型
     */
    private String setFileType(String url) {
        String fileType = null;
        //类型库
        String[] test = {"html", "css"};
        //URL分析
        char c = url.charAt(url.toCharArray().length - 1);
        if (c == '/') {
            url += "index.html";
        }
        for (String s : test) {
            if (url.contains("." + s)) {
                fileType = s;
                break;
            }
        }
        return fileType;
    }

    /**
     * 保存Url
     *
     * @param url      路径
     * @param fileType 文件类型
     */
    private void saveUrl(String url, String fileType) {
        String table_name = setPage.setTableName();
        String[] titleTab = new String[]{"fileType", "urltype", "urlContent"};
        try {
            ResultSet query = open.query(table_name, null, "urlContent = '" + url + "'");
            if (!query.first()) {
                open.insert(table_name, titleTab, new String[]{fileType, null, url});
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    /**
     * @param url 路径
     * @return 返回网络检索内容
     */
    private String httpGetPage(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String responseBody = null;

        try {
            HttpGet httpget = new HttpGet(url);
            System.out.println("executing request " + httpget.getURI());

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    try {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            };
            responseBody = httpclient.execute(httpget, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpclient.close();
        return responseBody;

    }

    /**
     * 检索出网页（page）中所有指定 规则 的链接,并存入数据库以供检索使用
     *
     * @param setRule  检索规则
     * @param url      page路径
     * @param page     page内容
     * @param fileType 文件类型
     */
    private void search(String setRule, final String url, String page, String fileType) {
        String beginUrl = setPage.setBeginUrl();
        Pattern p = Pattern.compile(setRule, Pattern.DOTALL);
        Matcher m = p.matcher(page);
        String[] split = url.split("/");
        String url2 = url.replace(split[split.length - 1], "");

        while (m.find()) {
            String group = m.group();
            //提取 URL
            String finalUrl = group.replace("\"", "");
            if (!finalUrl.contains(beginUrl)) {
                if (!finalUrl.contains("http") && finalUrl.charAt(0) == '/') {
                    char[] chars = finalUrl.toCharArray();
                    String s = "" + chars[1] + chars[2] + chars[3];
                    finalUrl = finalUrl.replace("/" + s, s);
                }
                finalUrl = url2 + finalUrl;
            }
            //存入数据库
            saveUrl(finalUrl, fileType);

        }
    }

    /**
     * 将检索到的网页写入文件夹
     *
     * @param url  路径
     * @param page 网页内容
     */
    private void writeFile(String url, String page) {
        String saveLocal = setPage.setSaveLocal();
        String deleteHead = url.replace("http://", "");
        String[] split1 = deleteHead.split("/");
        String fileLocal = saveLocal + deleteHead.replace(split1[split1.length - 1], "");
        String docLocal = saveLocal + deleteHead;

        //修改绝对路径为相对路径
        String[] s1 = url.split("/");
        String s2 = url.replace(s1[s1.length - 1], "");
        page = page.replace(s2, "/");

        int sum = 0;
        try {
            File file = new File(fileLocal);
            if (file.mkdirs()) {
                sum += 1;
            }
            file = new File(docLocal);
            if (file.createNewFile()) {
                sum += 2;
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(docLocal));
            writer.write(page);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("错误： " + e.getMessage());

        } finally {
            String s = null;
            switch (sum) {
                case 0:
                    s = "文件夹和文件都建立失败";
                    break;
                case 1:
                    s = "文件夹建立成功，文件建立失败";
                    break;
                case 2:
                    s = "文件夹建立失败，文件建立成功";
                    break;
                case 3:
                    s = "文件夹和文件都建立成功";
                    break;
                default:
                    s = "程序异常!";
                    break;
            }
            System.out.println("文件及文件夹建立：" + s +
                    "\n         文件夹路径：" + fileLocal +
                    "\n         文件路径：" + docLocal);
        }
    }

    public void setOnSetPage(onSetPage page) {
        this.setPage = page;

    }
}
