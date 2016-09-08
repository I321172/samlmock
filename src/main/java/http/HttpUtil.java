package http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    public static String getResponseId(String url, boolean needProxy) throws Exception
    {
        String body = util.fetchWeb(url, needProxy).getResponseBody();
        String samlRequest = body.replaceAll(".*SAMLRequest=(.*?)&.*", "$1");
        String xml = decode(samlRequest, true);
        String responseId = xml.replaceAll("[\\s\\S]*ID=\"(.*?)\".*", "$1");
        return responseId;
    }

    public static void main(String args[]) throws Exception
    {
        String body = getResponseId(demoSp, false);
        System.out.println(body);
    }

    public static void test() throws IOException, DataFormatException
    {
        String inStr = "Hellow World!";
        byte[] data = inStr.getBytes("UTF-8");
        byte[] output = new byte[100];
        // Compresses the data
        Deflater compresser = new Deflater();
        compresser.setInput(data);
        compresser.finish();
        int bytesAfterdeflate = compresser.deflate(output);
        System.out.println("Compressed byte number:" + bytesAfterdeflate);
        // Decompresses the data
        Inflater decompresser = new Inflater();
        decompresser.setInput(output, 0, bytesAfterdeflate);
        byte[] result = new byte[100];
        int resultLength = decompresser.inflate(result);
        decompresser.end();
        String outStr = new String(result, 0, resultLength, "UTF-8");
        System.out.println("Decompressed data: " + outStr);
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
