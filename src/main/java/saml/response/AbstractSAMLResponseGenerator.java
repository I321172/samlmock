package saml.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import saml.response.utils.Props;
import saml.response.utils.StringUtils;

abstract class AbstractSAMLResponseGenerator implements ISSOProcess
{
    protected GenerateSAML1ResponseBean responseBean;
    protected Unmarshaller              unmarshaller;
    protected Element                   samlElement;
    private InputStream                 ksPath;
    private String                      ksPsw;
    private String                      keyPsw;
    private String                      keyName;

    private String                      marshTemplate;

    private String                      adminUser;
    private String                      ssoIssuer;
    private static String               xmlSAML11Resp2 = "<Response xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" IssueInstant=\"2012-06-19T05:26:04.343Z\" MajorVersion=\"1\" MinorVersion=\"1\" Recipient=\"http://localhost:7003/samlacs/acs\" ResponseID=\"c47f01f46368fd08cf8d85739e047c30\"><Status><StatusCode Value=\"samlp:Success\"/></Status><Assertion xmlns=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" AssertionID=\"bfbad51e229f66f2ed8e802dbeb07777\" IssueInstant=\"2012-06-19T05:26:04.328Z\" Issuer=\"http://www.bea.com/demoSAML\" MajorVersion=\"1\" MinorVersion=\"1\"><Conditions NotBefore=\"2012-06-19T05:26:04.328Z\" NotOnOrAfter=\"2012-06-19T05:28:04.328Z\"/><AuthenticationStatement AuthenticationInstant=\"2012-06-19T05:26:04.328Z\" AuthenticationMethod=\"urn:oasis:names:tc:SAML:1.0:am:unspecified\"><Subject><NameIdentifier Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\" NameQualifier=\"bea.com\">ssouser</NameIdentifier><SubjectConfirmation><ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:bearer</ConfirmationMethod></SubjectConfirmation></Subject></AuthenticationStatement></Assertion></Response>";
    private static String               xmlSAML11Resp  = "<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" IssueInstant=\"2016-09-06T07:51:55.460Z\" MajorVersion=\"1\" MinorVersion=\"1\" Recipient=\"http://pvgn50862335a:8080/saml/samllogin?company=sso007\" ResponseID=\"sfBF6212F6A0C4A7BA511010CE4903D5A2CD5DECFE\"><samlp:Status xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\"><samlp:StatusCode Value=\"samlp:Success\" xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\"/></samlp:Status><saml:Assertion xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" AssertionID=\"sf80F1F79D3A48C9EC331E13D762D9347B4B2356E3\" IssueInstant=\"2016-09-06T07:51:55.460Z\" Issuer=\"http://pvgd50861360a:8080/opensso\" MajorVersion=\"1\" MinorVersion=\"1\"><saml:Conditions NotBefore=\"2016-09-06T07:48:35.460Z\" NotOnOrAfter=\"2016-09-06T07:55:15.460Z\"/><saml:AuthenticationStatement AuthenticationInstant=\"2016-09-06T07:51:55.460Z\" AuthenticationMethod=\"urn:oasis:names:tc:SAML:1.0:am:password\"><saml:Subject><saml:NameIdentifier>feng</saml:NameIdentifier><saml:SubjectConfirmation xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\"><saml:ConfirmationMethod xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\">urn:oasis:names:tc:SAML:1.0:cm:bearer</saml:ConfirmationMethod></saml:SubjectConfirmation></saml:Subject></saml:AuthenticationStatement></saml:Assertion></samlp:Response>";
    private static String               xmlSAML20Resp  = "<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"s298d1dc43853b7981d6473de27e60565c259b3ee1\" Version=\"2.0\" IssueInstant=\"2010-10-28T16:50:18Z\" Destination=\"https://staging.successfactors.com/saml2/SAMLAssertionConsumer?company=samlTest2\"><saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">http://10.50.10.128:8080/opensso</saml:Issuer><samlp:Status xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"><samlp:StatusCode xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"></samlp:StatusCode></samlp:Status><saml:Assertion xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" Version=\"2.0\" ID=\"s2e73e4d379780aeec8b34a596e6eb818e70db6f81\" IssueInstant=\"2010-10-28T16:50:18Z\"><saml:Issuer>http://10.50.10.128:8080/opensso</saml:Issuer><saml:Subject><saml:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">athompson</saml:NameID><saml:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"><saml:SubjectConfirmationData InResponseTo=\"dummy\" Recipient=\"https://staging.successfactors.com/saml2/SAMLAssertionConsumer\" ></saml:SubjectConfirmationData></saml:SubjectConfirmation></saml:Subject><saml:Conditions NotOnOrAfter=\"2010-10-31T00:23:38Z\"><saml:AudienceRestriction><saml:Audience>https://www.successfactors.com</saml:Audience></saml:AudienceRestriction></saml:Conditions><saml:AuthnStatement AuthnInstant=\"2010-10-28T16:50:18Z\" SessionIndex=\"s2e73e4d379780aeec8b34a596e6eb818e70db6f81\"><saml:AuthnContext><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml:AuthnContextClassRef></saml:AuthnContext></saml:AuthnStatement><saml:AttributeStatement></saml:AttributeStatement></saml:Assertion></samlp:Response>";
    private static String               xmlSAML20Resp2 = "<Response xmlns=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"s298d1dc43853b7981d6473de27e60565c259b3ee1\" Version=\"2.0\" IssueInstant=\"2010-10-28T16:50:18Z\" Destination=\"https://staging.successfactors.com/saml2/SAMLAssertionConsumer?company=samlTest2\"><saml:Issuer>http://10.50.10.128:8080/opensso</saml:Issuer><Status><StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"></StatusCode></Status><saml:Assertion  Version=\"2.0\" ID=\"s2e73e4d379780aeec8b34a596e6eb818e70db6f81\" IssueInstant=\"2010-10-28T16:50:18Z\"><saml:Issuer>http://10.50.10.128:8080/opensso</saml:Issuer><saml:Subject><saml:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">athompson</saml:NameID><saml:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"><saml:SubjectConfirmationData InResponseTo=\"dummy\" Recipient=\"https://staging.successfactors.com/saml2/SAMLAssertionConsumer\" ></saml:SubjectConfirmationData></saml:SubjectConfirmation></saml:Subject><saml:Conditions NotOnOrAfter=\"2010-10-31T00:23:38Z\"><saml:AudienceRestriction><saml:Audience>https://www.successfactors.com</saml:Audience></saml:AudienceRestriction></saml:Conditions><saml:AuthnStatement AuthnInstant=\"2010-10-28T16:50:18Z\" SessionIndex=\"s2e73e4d379780aeec8b34a596e6eb818e70db6f81\"><saml:AuthnContext><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml:AuthnContextClassRef></saml:AuthnContext></saml:AuthnStatement><saml:AttributeStatement></saml:AttributeStatement></saml:Assertion></Response>";

    public AbstractSAMLResponseGenerator()
    {

    }

    @Override
    public Object doSSOOperation() throws Exception
    {
        prepare();
        fillContent();
        return saveContent();
    }

    @Override
    public void prepare() throws Exception
    {
        // here to do some import configuration stuff
        getDocumentFromTemplateFile(responseBean);
    }

    @Override
    public void setBean(Object baseBean)
    {
        // TODO Auto-generated method stub
        this.responseBean = (GenerateSAML1ResponseBean) baseBean;
    }

    /**
     * @param responseBean
     * @throws Exception
     * @return Unmarshaller and element which is the approach to generate string
     *         response
     */
    private void getDocumentFromTemplateFile(GenerateSAML1ResponseBean responseBean) throws Exception
    {
        BasicParserPool parser = new BasicParserPool();
        parser.setNamespaceAware(true);
        // UnmarshallerFactory unmarshallerFactory =
        // Configuration.getUnmarshallerFactory();
        boolean isSaml2 = responseBean.getClass() == GenerateSAML2ResponseBean.class;
        String defaultTemplate = isSaml2 ? xmlSAML20Resp : xmlSAML11Resp;
        ByteArrayInputStream bas = new ByteArrayInputStream(this.getMarshTemplate(defaultTemplate).getBytes());
        Document doc = parser.parse(bas);
        samlElement = doc.getDocumentElement();
        unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(samlElement);
    }

    protected String generateSAMLID()
    {
        byte[] bts = new byte[20];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(bts);
        byte[] bts2 = new byte[40];
        int val = 0;
        for (int i = 0; i < 20; i++)
        {
            val = (bts[i] & 0x0000000F);
            bts2[i * 2] = (byte) (val < 10 ? val + 48 : 55 + val);
            val = (bts[i] & 0x000000FF) >> 4;
            bts2[i * 2 + 1] = (byte) (val < 10 ? val + 48 : 55 + val);
        }
        return "sf" + new String(bts2);
    }

    protected DateTime getSAMLDateTime(int gapSecs, int defVal)
    {
        int gap = gapSecs != 0 ? gapSecs : defVal;
        Date dt = new Date();
        if (gap != 0)
            dt.setTime(dt.getTime() + gap * 1000);
        return new DateTime(dt);
    }

    protected String printXml(Node element) throws Exception
    {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        // FIX: remove ident formatting
        // tr.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes")
        // tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
        // "3")
        ByteArrayOutputStream brOs = new ByteArrayOutputStream(2000);
        tr.transform(new DOMSource(element), new StreamResult(brOs));
        return brOs.toString();
    }

    /**
     * @param isSp
     * @return
     * @throws Exception
     *             <br>
     *             Get Credential which contains a public key (certificate) and
     *             private key from template file<br>
     *             true from SP file: sp.jks;<br>
     *             false from IDP file: opensso.jks
     */
    protected BasicX509Credential getBasicX509Credential(boolean isSp) throws Exception
    {
        getSamlKeyRelatedInfo(isSp);
        return getBasicX509Credential();
    }

    protected BasicX509Credential getBasicX509Credential() throws Exception
    {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(ksPath, ksPsw.toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate(keyName);
        PrivateKey privateKey = (PrivateKey) ks.getKey(keyName, keyPsw.toCharArray());
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(cert);
        credential.setPrivateKey(privateKey);
        return credential;
    }

    private void getSamlKeyRelatedInfo(boolean sp)
    {
        String ksSuffix = sp ? ".sp" : "";
        String fileName = Props.getProperty("ks.path" + ksSuffix);
        ksPath = this.getClass().getResourceAsStream("/key/" + fileName);
        ksPsw = Props.getProperty("ks.pwd" + ksSuffix);
        keyPsw = Props.getProperty("key.pwd" + ksSuffix);
        keyName = Props.getProperty("key.name" + ksSuffix);
    }

    protected Signature generateSignatureFromCredential(BasicX509Credential credential)
    {
        Signature signature = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(credential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        // signature.setCanonicalizationAlgorithm(org.opensaml.xml.signature.SignatureConstants.ALGO_ID_C14N_OMIT_COMMENTS)
        KeyInfo keyinfo = (KeyInfo) Configuration.getBuilderFactory().getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME)
                .buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        KeyName keyn = (KeyName) Configuration.getBuilderFactory().getBuilder(KeyName.DEFAULT_ELEMENT_NAME)
                .buildObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyn.setValue(keyName);
        keyinfo.getKeyNames().add(keyn);
        signature.setKeyInfo(keyinfo);
        return signature;
    }

    public String getSsoIssuer()
    {
        return ssoIssuer;
    }

    public void setSsoIssuer(String ssoIssuer)
    {
        this.ssoIssuer = ssoIssuer;
    }

    protected int getDefaultGapSeconds()
    {
        return 600;
    }

    protected String getAdminUser()
    {
        return this.adminUser;
    }

    public String getMarshTemplate()
    {
        return marshTemplate;
    }

    public String getMarshTemplate(String defaultValue)
    {
        return StringUtils.isNullOrEmpty(marshTemplate) ? defaultValue : marshTemplate;
    }

    public void setMarshTemplate(String marshTemplate)
    {
        this.marshTemplate = marshTemplate;
    }

    public enum RequireMandatorySignatureEnum
    {
        Assertion("Assertion"), Response("Response(Customer Generated/IdP/AP)"), Both("Both"), Neither(
                "Neither"), Ignore("");
        private String option;

        RequireMandatorySignatureEnum(String option)
        {
            setOption(option);
        }

        public String getOption()
        {
            return option;
        }

        public void setOption(String option)
        {
            this.option = option;
        }
    }

}
