package http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;

public class HttpUtil
{
    private static HttpClientUtil util         = new HttpClientUtil();
    private static String         cf07         = "http://dc13qaautocandcfapp07.lab.od.sap.biz:8080/login?company=datPLT11&username=admin&password=pwd";
    private static String         provisioning = "https://qacand.sflab.ondemand.com/provisioning_login?server=qacand";
    private static String         demoSp       = "http://pvgn50862335a:8080/saml2/Login?company=sso008&RelayState=/login?company=sso008";

    /**
     * Response Id,JessionId,BigId
     * 
     * @param url
     * @param needProxy
     * @return
     * @throws Exception
     */
    public static String[] getResponse(String url, boolean needProxy) throws Exception
    {
        HttpClientBean httpBean = util.fetchWeb(url, needProxy);
        String samlRequest = httpBean.getResponseBody().replaceAll(".*SAMLRequest=(.*?)&.*", "$1");
        String xml = decode(samlRequest, true);
        String responseId = xml.replaceAll("[\\s\\S]*ID=\"(.*?)\".*", "$1");
        String cookies = httpBean.getLoginCookie();
        String arr[] = cookies.split(";");
        return new String[] { responseId, arr[0], arr.length == 2 ? arr[1] : "" };
    }

    public static void main(String args[]) throws Exception
    {
        String[] body = getResponse(demoSp, false);
        System.out.println(body[0]);
    }

    public static byte[] inflateDataBlock(byte[] data)
    {
        ByteArrayInputStream bai = new ByteArrayInputStream(data);
        InflaterInputStream ifis = new InflaterInputStream(bai, new Inflater(true));
        byte[] buf = new byte[1024]; // suppose the length should be enough
        ByteArrayOutputStream bts = new ByteArrayOutputStream();
        byte[] ret = null;
        try
        {
            int size = ifis.read(buf);
            while (size > 0)
            {
                bts.write(buf, 0, size);
                size = ifis.read(buf);
            }
        } catch (IOException e)
        {
            // do nothing
            e = null;
        } finally
        {
            ret = bts.toByteArray();
            try
            {
                bts.close();
                ifis.close();
                bai.close();
            } catch (Exception e)
            {
                // do nothing
            }
        }
        return ret;
    }

    public static String decompress(byte[] origin) throws DataFormatException, UnsupportedEncodingException
    {
        byte[] a = inflateDataBlock(origin);
        return new String(a, "utf-8");
    }

    private static String decode(String origin, boolean urlDecode) throws Exception
    {
        if (urlDecode)
            origin = URLDecoder.decode(origin, "utf-8");
        byte[] saml = Base64.decodeBase64(origin);
        String result1 = decompress(saml);
        return result1;
    }

}
