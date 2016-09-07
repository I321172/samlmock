package saml.response;

import saml.response.SAML2ResponseGenerator.OnOffEnum;

public class GenerateSAML2ResponseBean extends GenerateSAML1ResponseBean
{
    // specific for saml2
    private String                 responseIssuer;
    private AssertionAttributeBean assertionAttribute;
    private String                 subjectResponseTo     = "";
    private String                 nameIdFormat;
    private String                 nameId;
    private boolean                isSubjNeedNotBefore   = true;
    private boolean                isSubjNeedNotAfter    = true;
    private boolean                isSessionNeedNotAfter = true;
    private int                    SubjNotBefore         = 0;
    private int                    SubjNotOnOrAfter      = 0;
    private OnOffEnum              isNeedAuth            = OnOffEnum.Default;
    private String                 sessionIndex;
    private boolean                attributeEmpty        = true;
    private int                    sessNotAfter          = 0;
    private String                 audience;
    private EncryptTargetEnum      encryptTarget         = EncryptTargetEnum.Ignore;
    private String                 destination;

    public GenerateSAML2ResponseBean()
    {
        generateDefaultAssertionAttributeBean();
    }

    public String getSubjectResponseTo()
    {
        return subjectResponseTo;
    }

    public void setSubjectResponseTo(String subjectResponseTo)
    {
        this.subjectResponseTo = subjectResponseTo;
    }

    public String getNameIdFormat()
    {
        return nameIdFormat;
    }

    public void setNameIdFormat(String nameIdFormat)
    {
        this.nameIdFormat = nameIdFormat;
    }

    public boolean isSubjNeedNotBefore()
    {
        return isSubjNeedNotBefore;
    }

    public void setSubjNeedNotBefore(boolean isSubjNeedNotBefore)
    {
        this.isSubjNeedNotBefore = isSubjNeedNotBefore;
    }

    public boolean isSubjNeedNotAfter()
    {
        return isSubjNeedNotAfter;
    }

    public void setSubjNeedNotAfter(boolean isSubjNeedNotAfter)
    {
        this.isSubjNeedNotAfter = isSubjNeedNotAfter;
    }

    public int getSubjNotBefore()
    {
        return SubjNotBefore;
    }

    public void setAllTimeNeedRestriction(boolean isNeed)
    {
        super.setAllTimeNeedRestriction(isNeed);
        setSubjNeedNotAfter(isNeed);
        setSubjNeedNotBefore(isNeed);
        setSessionNeedNotAfter(isNeed);
    }

    /**
     * if set as 0 means use default gap seconds<br>
     * Actual Time=CurrentTime+ subjNotBefore, generally to be negative:
     * -SSOConstant.DEFAULT_GAP_SECONDS
     */
    public void setSubjNotBefore(int subjNotBefore)
    {
        SubjNotBefore = subjNotBefore;
    }

    public int getSubjNotOnOrAfter()
    {
        return SubjNotOnOrAfter;
    }

    /**
     * Actual Time=CurrentTime+ subjNotOnOrAfter, generally to be positive:
     * SSOConstant.DEFAULT_GAP_SECONDS
     */
    public void setSubjNotOnOrAfter(int subjNotOnOrAfter)
    {
        SubjNotOnOrAfter = subjNotOnOrAfter;
    }

    /**
     * @param isNeedRestriction
     * 
     *            <pre>
     *            setSubjNeedNotBefore(isNeedRestriction);
     *            setSubjNeedNotAfter(isNeedRestriction);
     *            </pre>
     */
    public void setSubjTimeRestriction(boolean isNeedRestriction)
    {
        setSubjNeedNotBefore(isNeedRestriction);
        setSubjNeedNotAfter(isNeedRestriction);
    }

    public String getResponseIssuer() throws Exception
    {
        return responseIssuer;
    }

    /**
     * Assertion issuer and response issuer
     * 
     * @param issuer
     */
    public void setBothIssuer(String issuer)
    {
        this.setIssuer(issuer);
        this.setResponseIssuer(issuer);
    }

    /**
     * @param responseIssuer
     *            Only for SAML 2; Usually should be set manually;<br>
     *            Has initiated for
     *            samlV2ResponseBean.setResponseIssuer(issuerV2 = "saml2_" +
     *            company;);
     * 
     *            <pre>
     *            if (StringUtils.isNullOrEmpty(bean.getResponseIssuer()))
     *                resp.getIssuer()
     *                        .setValue(getProperty(&quot;issuer&quot;) != null ? getProperty(&quot;issuer&quot;) : &quot;samltest.successfactors.com&quot;);
     *            else
     *                resp.getIssuer().setValue(bean.getIssuer());
     *            </pre>
     */
    public void setResponseIssuer(String responseIssuer)
    {
        this.responseIssuer = responseIssuer;
    }

    /**
     * @return <br>
     *         For Remind: Map the key of "password" and "username" in Mike's
     *         Map
     */
    public AssertionAttributeBean getAssertionAttribute()
    {
        return assertionAttribute;
    }

    public AssertionAttributeBean generateDefaultAssertionAttributeBean()
    {
        this.assertionAttribute = new AssertionAttributeBean();
        setAttributeEmpty(true);
        return this.assertionAttribute;
    }

    public void setAssertionAttribute(AssertionAttributeBean assertionAttribute)
    {
        this.assertionAttribute = assertionAttribute;
    }

    public String getNameId()
    {
        return nameId;
    }

    public void setNameId(String nameId)
    {
        this.nameId = nameId;
    }

    public OnOffEnum getIsNeedAuth()
    {
        return isNeedAuth;
    }

    public void setIsNeedAuth(OnOffEnum isNeedAuth)
    {
        this.isNeedAuth = isNeedAuth;
    }

    public String getSessionIndex()
    {
        return sessionIndex;
    }

    public void setSessionIndex(String sessionIndex)
    {
        this.sessionIndex = sessionIndex;
    }

    public int getSessNotAfter()
    {
        return sessNotAfter;
    }

    /**
     * Actual Time=CurrentTime+ sessNotAfter, generally to be positive:
     * SSOConstant.DEFAULT_GAP_SECONDS
     */
    public void setSessNotAfter(int sessNotAfter)
    {
        this.sessNotAfter = sessNotAfter;
    }

    public String getAudience()
    {
        return audience;
    }

    public void setAudience(String audience)
    {
        this.audience = audience;
    }

    public EncryptTargetEnum getEncryptTarget()
    {
        return encryptTarget;
    }

    /**
     * @param encryptTarget
     *            <br>
     *            Valid Options:<br>
     *            nameid;<br>
     *            assertion;<br>
     *            attribute
     */
    public void setEncryptTarget(EncryptTargetEnum encryptTargetEnum)
    {
        this.encryptTarget = encryptTargetEnum;
    }

    public void setEncryptTarget(String encryString)
    {
        setEncryptTarget(EncryptTargetEnum.valueOf(encryString));
    }

    public boolean isSkipEncrypt()
    {
        return encryptTarget == null || encryptTarget == EncryptTargetEnum.Ignore;
    }

    public String getDestination()
    {
        return destination;
    }

    /**
     * @param destination
     *            Only for SAML 2<br>
     * 
     *            <pre>
     *            if (StringUtils.isNullOrEmpty(bean.getDestination()))
     *                resp.setDestination(assertionConsumer + &quot;?company=&quot; + company);
     *            else
     *                resp.setDestination(bean.getDestination());
     *            </pre>
     */
    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public boolean isAttributeEmpty()
    {
        return attributeEmpty;
    }

    public void setAttributeEmpty(boolean attributeEmpty)
    {
        this.attributeEmpty = attributeEmpty;
    }

    public boolean isSessionNeedNotAfter()
    {
        return isSessionNeedNotAfter;
    }

    public void setSessionNeedNotAfter(boolean isSessionNeedNotAfter)
    {
        this.isSessionNeedNotAfter = isSessionNeedNotAfter;
    }

    public class AssertionAttributeBean
    {
        // field value = null stands for SAML response don't need this field;
        // field value = empty stands for set as null in SAML response;
        private String  username;
        private String  password;
        private String  companyid;
        private String  locale;
        private String  companyuuid;
        private String  zonesessionid;
        private boolean isNeedSecondAttrStat;

        public String getUsername()
        {
            return username;
        }

        private void setAttributeNotEmpty()
        {
            setAttributeEmpty(false);
        }

        public void setUsername(String username)
        {
            this.username = username;
            setAttributeNotEmpty();

        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
            setAttributeNotEmpty();
        }

        public String getCompanyid()
        {
            return companyid;
        }

        public void setCompanyid(String companyid)
        {
            this.companyid = companyid;
            setAttributeNotEmpty();
        }

        public String getLocale()
        {
            return locale;
        }

        public void setLocale(String locale)
        {
            this.locale = locale;
            setAttributeNotEmpty();
        }

        public String getCompanyuuid()
        {
            return companyuuid;
        }

        public void setCompanyuuid(String companyuuid)
        {
            this.companyuuid = companyuuid;
            setAttributeNotEmpty();
        }

        public String getZonesessionid()
        {
            return zonesessionid;
        }

        public void setZonesessionid(String zonesessionid)
        {
            this.zonesessionid = zonesessionid;
            setAttributeNotEmpty();
        }

        public boolean isNeedSecondAttrStat()
        {
            return isNeedSecondAttrStat;
        }

        public void setNeedSecondAttrStat(boolean isNeedSecondAttrStat)
        {
            this.isNeedSecondAttrStat = isNeedSecondAttrStat;
            setAttributeNotEmpty();
        }

    }

}
