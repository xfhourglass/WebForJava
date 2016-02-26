package testRegularExpression;

import Crawler.CrawLerFour;

/**
 * Project:testRegularExpression
 * <p>
 * Author:Crazy_LeoJay
 * Time:下午6:09
 */
public class Test {
    private static final String db_name = "crawler";
    private static final String table_name = "runoobList";
    private static final String user_name = "root";
    private static final String password = "";

    private static final String URL = "http://www.runoob.com/index.html";
    private static final String fileURL = "/home/leojay/webDownload3/";

    public static void main(String[] args){
        CrawLerFour cf = new CrawLerFour();
        cf.setOnSetPage(new CrawLerFour.onSetPage() {
            @Override
            public String setBeginUrl() {
                return URL;
            }

            @Override
            public String setSaveLocal() {
                return fileURL;
            }

            @Override
            public String setDbName() {
                return db_name;
            }

            @Override
            public String setTableName() {
                return table_name;
            }

            @Override
            public String userName() {
                return user_name;
            }

            @Override
            public String password() {
                return password;
            }
        });
        cf.onStart();
    }
}
