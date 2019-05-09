package unimelb.bitbox;

import org.json.JSONObject;

public class prettyPrinter
{
    //this class simply prints json objects to our output very nicely indented
    static void print(String jsonString)
    {
        JSONObject json = new JSONObject(jsonString); 
        System.out.println(json.toString(4));
    }

}
