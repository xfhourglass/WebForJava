package testRegularExpression;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project:testRegularExpression
 * <p>
 * Author:Crazy_LeoJay
 * Time:下午10:45
 */
public class RegularExpressionHTML {
    private static String HTML_url = "/home/leojay/webDownload/www.runoob.com/java/java-tutorial.html";
    private static String css_url = "/home/leojay/webDownload/apps.bdimg.com/libs/fontawesome/4.2.0/css" +
            "/font-awesome.min.css";

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(HTML_url));
//        BufferedReader reader = new BufferedReader(new FileReader(css_url));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(HTML_url));

        String setRule = "\"(http://[^\"]*)\"";
        String setRule2 = "(?<=href=)\"[^\"]*\"";
        String setRule3 = "(?<=<link).{0,24}(?<=href=)\"[^\"]*\"";
        String setRule4 = "(?<=<link)[^>]*>";
        String setRule5 = "url\\(\'[^\']*\'\\)";
        String setRule6 = "(?<=herf=)\"([^\"]|[^/.css]|[^/.html])*\"";
        //url('../fonts/fontawesome-webfont.eot?#iefix&v=4.2.0')
        Pattern pattern = Pattern.compile(setRule2, Pattern.DOTALL);
        Matcher matcher;
        String s;
        while ((s = reader.readLine()) != null) {
            matcher = pattern.matcher(s);
            try {
                while (matcher.find()) {
                    String string = matcher.group();
                    System.out.println(string);
                }
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

    }
}
