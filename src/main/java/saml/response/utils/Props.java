package saml.response.utils;

import java.io.IOException;
import java.util.Properties;

public class Props
{
    private static Properties p;

    public static void load()
    {
        if (p != null)
            return;
        p = new Properties();
        try
        {
            p.load(Props.class.getResourceAsStream("/properties/keystore.properties"));
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getProperty(String key)
    {
        return get(key) != null ? get(key).toString() : null;
    }

    public static Object get(String key)
    {
        load();
        return p.get(key);
    }

    public static void main(String args[])
    {
        Props.get("ks.pwd");
    }

}
