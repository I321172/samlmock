package http;

/**
 * @author lguan Provided for record key Login related cookie name
 */
public enum LoginCookieEnum
{
    JSESSIONID("JSESSIONID"), BigIPRW("BIGipServerP-QAAUTOCAND-80"), BigIPRot(
            "BIGipServerqaautocand.sflab.ondemand.com_1080"), LoginMethod("loginMethodCookieKey"), DeepLinkCookie(
                    "deeplinkCookieKey"), AssertingParty(
                            "assertingPartyCookieKey"), BigIpGeneral("BIGip"), CompanyId("bizxCompanyId");

    private String cookieName;

    LoginCookieEnum(String cookieName)
    {
        this.cookieName = cookieName;
    }

    public String toString()
    {
        return cookieName;
    }

}
