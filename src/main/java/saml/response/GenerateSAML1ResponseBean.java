package saml.response;

import saml.response.AbstractSAMLResponseGenerator.RequireMandatorySignatureEnum;

public class GenerateSAML1ResponseBean
{
    private RequireMandatorySignatureEnum sign                 = RequireMandatorySignatureEnum.Ignore;
    private String                        user;
    // issuer stands for assertion issuer
    private String                        issuer;
    // responseTo for response
    private String                        responseId;
    // "" stands for using default response id
    private String                        responseTo;
    private boolean                       isSpecialSystem;
    private String                        recipient;
    private String                        assertionId;
    private boolean                       isSignCertSp         = false;

    private boolean                       isEncryptSignCertSp  = true;

    private boolean                       isCondiNeedNotBefore = true;
    private boolean                       isCondiNeedNotAfter  = true;
    private int                           CondiNotBefore       = 0;
    private int                           CondiNotOnOrAfter    = 0;

    private int                           responseTime         = 0;
    private int                           assertionTimeGapSecs = 0;
    private int                           authtime             = 0;

    public GenerateSAML1ResponseBean()
    {

    }

    public void setAllTimeNeedRestriction(boolean isNeed)
    {
        setCondiNeedNotAfter(isNeed);
        setCondiNeedNotBefore(isNeed);
    }

    public boolean isCondiNeedNotBefore()
    {
        return isCondiNeedNotBefore;
    }

    /**
     * @param isCondiNeedNotBefore
     */
    public void setCondiNeedNotBefore(boolean isCondiNeedNotBefore)
    {
        this.isCondiNeedNotBefore = isCondiNeedNotBefore;
    }

    public boolean isCondiNeedNotAfter()
    {
        return isCondiNeedNotAfter;
    }

    /**
     * @param isCondiNeedNotAfter
     */
    public void setCondiNeedNotAfter(boolean isCondiNeedNotAfter)
    {
        this.isCondiNeedNotAfter = isCondiNeedNotAfter;
    }

    public int getCondiNotBefore()
    {
        return CondiNotBefore;
    }

    /**
     * Actual Time=CurrentTime+ condiNotBefore, generally to be
     * negative:-SSOConstant.DEFAULT_GAP_SECONDS
     */
    public void setCondiNotBefore(int condiNotBefore)
    {
        CondiNotBefore = condiNotBefore;
    }

    public int getCondiNotOnOrAfter()
    {
        return CondiNotOnOrAfter;
    }

    /**
     * @param isNeedRestriction
     * 
     *            <pre>
     *            setCondiNeedNotBefore(isNeedRestriction);
     *            setCondiNeedNotAfter(isNeedRestriction);
     *            </pre>
     */
    public void setCondiTimeRestriction(boolean isNeedRestriction)
    {
        setCondiNeedNotBefore(isNeedRestriction);
        setCondiNeedNotAfter(isNeedRestriction);
    }

    /**
     * Actual Time=CurrentTime+ condiNotOnOrAfter, generally to be positive:
     * SSOConstant.DEFAULT_GAP_SECONDS
     */
    public void setCondiNotOnOrAfter(int condiNotOnOrAfter)
    {
        CondiNotOnOrAfter = condiNotOnOrAfter;
    }

    private String authMethod;
    private String confirmationMethod = "";

    public RequireMandatorySignatureEnum getSign()
    {
        return sign;
    }

    public void setSign(RequireMandatorySignatureEnum signatureEnum)
    {
        this.sign = signatureEnum;
    }

    /**
     * Refection purpose
     * 
     * @param signString
     */
    public void setSign(String signString)
    {
        setSign(RequireMandatorySignatureEnum.valueOf(signString));
    }

    public String getUser()
    {
        return user;
    }

    /**
     * @param user
     *            <br>
     *            This user is set in Assertion part. If not set, it will be
     *            admin by default.<br>
     *            For SAML 2 <blockquote>
     * 
     *            <pre>
     *            if (!StringUtils.isNullOrEmpty(bean.getUser()))
     *            {
     *                subject.getNameID().setValue(bean.getUser());
     *            } else if (!StringUtils.isNullOrEmpty(bean.getNameId()))
     *                subject.getNameID().setValue(bean.getNameId());
     *            else
     *                subject.getNameID().setValue(getProperty(&quot;superAdminUserLoginName&quot;));
     *            </pre>
     * 
     *            </blockquote> For SAML 1<blockquote>
     * 
     *            <pre>
     *            if (StringUtils.isNull(bean.getUser()))
     *            {
     *                stat.getSubject().setNameIdentifier.setNameIdentifier(adminUser);
     *            } else
     *            {
     *                if (StringUtils.isEmpty(bean.getUser()))
     *                {
     *                    stat.getSubject().setNameIdentifier(null);
     *                } else
     *                {
     *                    stat.getSubject().getNameIdentifier().setNameIdentifier(bean.getUser());
     *                }
     *            }
     *            </pre>
     * 
     *            </blockquote>
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    public String getIssuer() throws Exception
    {
        return issuer;
    }

    /**
     * For assertion issuer<br>
     * Usually should be set manually;<br>
     * Has been initiated for samlV11ResponseBean & samlV2ResponseBean;<br>
     * samlV11ResponseBean.setIssuer(issuerV11); issuerV11 = "samltestm2_" +
     * company;<br>
     * samlV2ResponseBean.setIssuer(issuerV2); issuerV2 = "saml2_" + company;
     */
    public void setIssuer(String issuer)
    {
        this.issuer = issuer;
    }

    /**
     * For reflection
     * 
     * @param issuer
     */
    public void setBothIssuer(String issuer)
    {
        this.setIssuer(issuer);
    }

    public String getResponseTo()
    {
        return responseTo;
    }

    /**
     * @param responseTo
     *            <br>
     *            In saml response: resp.setInResponseTo() <br>
     *            null by default
     */
    public void setResponseTo(String responseTo)
    {
        this.responseTo = responseTo;
    }

    public String getResponseId()
    {
        return responseId;
    }

    public void setResponseId(String responseId)
    {
        this.responseId = responseId;
    }

    public boolean isSpecialSystem()
    {
        return isSpecialSystem;
    }

    public void setSpecialSystem(boolean isSpecialSystem)
    {
        this.isSpecialSystem = isSpecialSystem;
    }

    public int getResponseTime()
    {
        return responseTime;
    }

    /**
     * @param responseTime
     *            Actual Time=CurrentTime+ responseTime;default to be 0
     */
    public void setResponseTime(int responseTime)
    {
        this.responseTime = responseTime;
    }

    public String getRecipient()
    {
        return recipient;
    }

    /**
     * @param recipient
     *            <br>
     *            For SAML 2: assertionConsumer= getServerUrl() +
     *            "/saml2/SAMLAssertionConsumer";<br>
     * 
     *            <pre>
     *            if (StringUtils.isNullOrEmpty(bean.getRecipient()))
     *                scd.setRecipient(assertionConsumer);
     *            else
     *                scd.setRecipient(bean.getRecipient());
     *            </pre>
     * 
     *            <br>
     *            For SAML 1:assertionHost = getServerUrl() + "/saml/samllogin";
     * 
     *            <pre>
     *            if (StringUtils.isNullOrEmpty(bean.getRecipient()))
     *                resp.setRecipient(assertionHost + &quot;?company=&quot; + company);
     *            else
     *                resp.setRecipient(bean.getRecipient());
     *            </pre>
     */
    public void setRecipient(String recipient)
    {
        this.recipient = recipient;
    }

    public String getAssertionId()
    {
        return assertionId;
    }

    public void setAssertionId(String assertionId)
    {
        this.assertionId = assertionId;
    }

    public int getAssertionTimeGapSecs()
    {
        return assertionTimeGapSecs;
    }

    public void setAssertionTimeGapSecs(int assertionTimeGapSecs)
    {
        this.assertionTimeGapSecs = assertionTimeGapSecs;
    }

    public int getAuthtime()
    {
        return authtime;
    }

    public void setAuthtime(int authtime)
    {
        this.authtime = authtime;
    }

    public String getAuthMethod()
    {
        return authMethod;
    }

    public void setAuthMethod(String authMethod)
    {
        this.authMethod = authMethod;
    }

    public String getConfirmationMethod()
    {
        return confirmationMethod;
    }

    public void setConfirmationMethod(String confirmationMethod)
    {
        this.confirmationMethod = confirmationMethod;
    }

    public boolean isSignCertSp()
    {
        return isSignCertSp;
    }

    /**
     * @param isSignCertSp
     *            <br>
     *            Set as false by default;<br>
     *            true stands for using SP<br>
     *            false stands for using IDP Determine whether SAML response
     *            uses SP certification<br>
     */
    public void setSignCertSp(boolean isSignCertSp)
    {
        this.isSignCertSp = isSignCertSp;
    }

    public boolean isEncryptSignCertSp()
    {
        return isEncryptSignCertSp;
    }

    /**
     * @param isEncryptSignCertSp
     *            <br>
     *            Set as true by default <br>
     *            true stands for using SP<br>
     *            false stands for using IDP<br>
     *            Determine whether Encryption part is SP certification
     */
    public void setEncryptSignCertSp(boolean isEncryptSignCertSp)
    {
        this.isEncryptSignCertSp = isEncryptSignCertSp;
    }

    public enum EncryptTargetEnum
    {
        Assertion, NameId, Ignore

    }

}
