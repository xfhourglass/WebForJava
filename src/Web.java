import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Project:PACKAGE_NAME
 * <p>
 * Author:Crazy_LeoJay
 * Time:上午11:18
 */
public class Web {

    private static final String URLString = "http://tool.oschina.net/apidocs";

    private static final String fileName = "/home/leojay/runoob";

    public static void main(String[] args){
        InputStream is;
        BufferedReader r;
        BufferedWriter w;

        try {
            URL url = new URL(URLString);
            is = url.openStream();
            r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            w = new BufferedWriter(new FileWriter(fileName));
            String line;
            while ((line = r.readLine()) != null){
                w.write(line);
                w.newLine();
            }
            w.flush();
        }catch (MalformedURLException e){

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("end");
    }
}
