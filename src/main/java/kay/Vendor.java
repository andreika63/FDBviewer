package kay;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Vendor {
    private static HttpClient httpClient = HttpClientBuilder.create().build();
    public static String getVendor(String mac){
        try {
            //System.out.println("http://www.macvendorlookup.com/api/v2/"+mac);
            HttpResponse httpResponse = httpClient.execute(new HttpGet("http://api.macvendors.com/"+mac));
            BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) sb.append(s);
            //System.out.println(sb);
            return sb.toString();//.replaceAll("\",\"","\",\n\"");
//            JSONObject jo = new JSONObject(sb.toString());
//            return jo.getString("startHex")
//                    + "\n" + jo.getString("endHex")
//                    + "\n" + jo.getString("endHex")
//                    + "\n" + jo.getString("startDec")
//                    + "\n" + jo.getString("endDec")
//                    + "\n" + jo.getString("company")
//                    + "\n" + jo.getString("addressL1")
//                    + "\n" + jo.getString("addressL2")
//                    + "\n" + jo.getString("addressL3")
//                    + "\n" + jo.getString("country")
//                    + "\n" + jo.getString("type")
//                    ;

        } catch (IOException e) {
          return e.getMessage();
        }

    }
}
