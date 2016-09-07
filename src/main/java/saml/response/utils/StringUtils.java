package saml.response.utils;

public class StringUtils
{
    public static boolean isNullOrEmpty(String text)
    {
        return text == null || text.equals("");
    }

    public static boolean isNull(String text)
    {
        return text == null;
    }

    public static boolean isEmpty(String text)
    {
        return text.equals("");
    }

}
