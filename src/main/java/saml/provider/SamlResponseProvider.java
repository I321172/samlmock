package saml.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.opensaml.DefaultBootstrap;

import saml.response.GenerateSAML1ResponseBean;
import saml.response.GenerateSAML2ResponseBean;
import saml.response.SAML1ResponseGenerator;
import saml.response.SAML2ResponseGenerator;
import saml.response.SSOOperation;
import saml.response.utils.ReflectionUtil;

public class SamlResponseProvider
{
    private static boolean isBootStrapInitiated;

    public static Object execute(boolean isSAML2, Object dataBean) throws Exception
    {
        if (!isBootStrapInitiated)
        {
            DefaultBootstrap.bootstrap(); // for opensaml
            isBootStrapInitiated = true;
        }
        SSOOperation operation = isSAML2 ? new SAML2ResponseGenerator() : new SAML1ResponseGenerator();
        operation.setBean(dataBean);
        return operation.doSSOOperation();
    }

    public static Object execute(GenerateSAML1ResponseBean saml1) throws Exception
    {
        return execute(false, saml1);
    }

    public static Object execute(GenerateSAML2ResponseBean saml2) throws Exception
    {
        return execute(true, saml2);
    }

    public static void setBootStrapInitiated(boolean isInitiated)
    {
        isBootStrapInitiated = isInitiated;
    }

    public static String execute(Map<String, String> params) throws Exception
    {
        boolean isSaml2 = params.get("type").equalsIgnoreCase("saml2");
        if (isSaml2)
            return execute(to(params, GenerateSAML2ResponseBean.class)).toString();
        else
            return execute(to(params, GenerateSAML1ResponseBean.class)).toString();
    }

    private static <T> T to(Map<String, String> params, Class<T> T)
            throws InstantiationException, IllegalAccessException

    {
        T instance = T.newInstance();

        for (String key : params.keySet())
        {
            String fieldName = String.format("%c%s", key.toLowerCase().charAt(0), key.substring(1));
            Field f = ReflectionUtil.findField(T, fieldName);
            boolean isSuccess = false;
            if (f != null)
            {
                isSuccess = ReflectionUtil.setField(f, instance, params.get(key));
            }
            if (!isSuccess)
            {
                // try to use method
                String methodName = String.format("set%c%s", key.toUpperCase().charAt(0), key.substring(1));
                Method m = ReflectionUtil.findMethod(T, methodName, String.class);
                if (m != null)
                {
                    ReflectionUtil.invokeMethod(m, instance, params.get(key));
                } else
                {
                    if (!methodName.equals("setType"))
                        System.out.println("Skip the Field :" + key);
                }
            }
        }
        return instance;

    }

    public static void main(String[] artgs) throws Exception
    {
        // GenerateSAML2ResponseBean saml2 = new GenerateSAML2ResponseBean();
        // saml2.setDestination("https://qaautocand.sflab.ondemand.com/saml2/SAMLAssertionConsumer?company=datPLT11");
        // saml2.setBothIssuer("http://idp.mergeplus.com:8080/opensso");
        // saml2.setRecipient("https://qaautocand.sflab.ondemand.com/saml2/SAMLAssertionConsumer");
        // saml2.setResponseId("testRecipientId");
        // saml2.setUser("admin");
        // saml2.setAudience("https://www.successfactors.com");
        // String saml1Response = execute(saml2).toString();
        // System.out.println(saml1Response);

        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "saml1");
        // params.put("sign", "Both");// Assertion,Response
        // params.put("encryptTarget", "Assertion");// Assertion, NameId
        params.put("destination", "https://qaautocand.sflab.ondemand.com/saml2/SAMLAssertionConsumer?company=datPLT11");
        params.put("BothIssuer", "http://idp.mergeplus.com:8080/opensso");
        params.put("Recipient", "https://qaautocand.sflab.ondemand.com/saml2/SAMLAssertionConsumer");
        // params.put("ResponseId", "testRecipientId");
        params.put("User", "admin");
        params.put("Audience", "https://www.successfactors.com");

        String resp = execute(params);
        System.out.println(resp);
    }

}
