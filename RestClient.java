import java.net.*;
import java.io.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

public class RestClient{
   
   public static void main(String[] args){
   
      try{
         //set up
         String baseUrl = "http://simon.ist.rit.edu:8080/AreaDemo/resources/AreaCalculator/";
         String rectangleResource = "Rectangle?width=420&length=69";
         String circleResource = "Circle?radius=3.146";
         String helloResource = "Hello";
         String helloName = "Hello/Clay";
        
         //conect via httpurlconnection
         URL url = new URL(baseUrl + circleResource);
         //URL url = new URL(baseUrl + rectangleResource);
         HttpURLConnection con = (HttpURLConnection)url.openConnection();
         con.setRequestMethod("GET");
         con.addRequestProperty("Content-Type","text/plain");
         //con.addRequestProperty("Content-Type","application/xml");
         con.connect();
         
         //read
         InputStream in = con.getInputStream();
         
         //process
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         System.out.println(br.readLine());
         
         //cleanup
         in.close();
         con.disconnect();
         
         //connect using http connection
         HttpClient client = new HttpClient();
         GetMethod method = new GetMethod(baseUrl + rectangleResource);
         
         //send request
         int statusCode = client.executeMethod(method);
         
         if(statusCode != HttpStatus.SC_OK)
            System.err.println("Mthod Failed:"+method.getStatusLine());
         else{
            InputStream rstream = null;
            
            //get response body
            rstream = method.getResponseBodyAsStream();
            
            //process
            br = new BufferedReader(new InputStreamReader(rstream));
            String line;
            while((line=br.readLine())!=null){
               System.out.println(line);
            }
            br.close();
         }
         
         
      }catch(Exception e){
         e.printStackTrace();
      }
      
   }//main
   
}//class