package saml.response;

import java.lang.reflect.Field;

import org.opensaml.Configuration;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;

import org.w3c.dom.Element;

import saml.response.GenerateSAML1ResponseBean.EncryptTargetEnum;
import saml.response.GenerateSAML2ResponseBean;
import saml.response.GenerateSAML2ResponseBean.AssertionAttributeBean;
import saml.response.utils.StringUtils;

public class SAML2ResponseGenerator extends AbstractSAMLResponseGenerator
{
    private Response  resp;
    private Signature signature;

    @Override
    public void fillContent() throws Exception
    {
        // Response is made up of its parameters and assertion
        resp = (Response) unmarshaller.unmarshall(samlElement);
        setResponse(resp);

        // assertion is made up of its parameters and condition , authentication
        Assertion assertion = resp.getAssertions().get(0);
        setAssertion(assertion);
        setAssertionAttribute(assertion);
        setAssertionAuthStatement(assertion);

        Conditions cond = assertion.getConditions();
        setCondition(cond);

        Subject subject = assertion.getSubject();
        setSubject(subject);

        SubjectConfirmation sc = subject.getSubjectConfirmations().get(0);
        setSubjectConfirmation(sc);

        // set signature
        setSignature();
        encryptSignature();
    }

    @Override
    public Object saveContent() throws Exception
    {
        // TODO Auto-generated method stub
        return printXml(Configuration.getMarshallerFactory().getMarshaller(resp).marshall(resp));
    }

    public GenerateSAML2ResponseBean getBean()
    {
        return (GenerateSAML2ResponseBean) responseBean;
    }

    /**
     * Contain a response issuer in SAML2
     * 
     * @param resp
     * @throws Exception
     */
    private void setResponse(Response resp) throws Exception
    {
        // Set Response parameter with data that bean provided
        if (!StringUtils.isNullOrEmpty(getBean().getDestination()))
        {
            resp.setDestination(getBean().getDestination());
        } else
        {
            throw new Exception("Set Destination!");
        }

        if (!StringUtils.isNullOrEmpty(getBean().getResponseId()))
        {
            resp.setID(getBean().getResponseId());
        } else
        {
            resp.setID(generateSAMLID());
        }

        if (!StringUtils.isNull(getBean().getResponseTo()))
        {
            if (StringUtils.isEmpty(getBean().getResponseTo()))
            {
                resp.setInResponseTo(generateSAMLID());
            } else
            {
                resp.setInResponseTo(getBean().getResponseTo());
            }
        } else
        {
            resp.setInResponseTo(null);
        }

        resp.setIssueInstant(getSAMLDateTime(getBean().getResponseTime(), 0));

        if (StringUtils.isNullOrEmpty(getBean().getResponseIssuer()))
        {
            resp.getIssuer().setValue(getSsoIssuer() != null ? getSsoIssuer() : "samltest.successfactors.com");
        } else
        {
            resp.getIssuer().setValue(getBean().getResponseIssuer());
        }
    }

    /**
     * Set issuer in assertion
     * 
     * @param assertion
     * @throws Exception
     */
    private void setAssertion(Assertion assertion) throws Exception
    {
        if (StringUtils.isNullOrEmpty(getBean().getIssuer()))
        {
            assertion.getIssuer().setValue(getSsoIssuer() != null ? getSsoIssuer() : "samltest.successfactors.com");
        } else
        {
            assertion.getIssuer().setValue(getBean().getIssuer());
        }
        if (StringUtils.isNullOrEmpty(getBean().getAssertionId()))
        {
            assertion.setID(generateSAMLID());
        } else
        {
            assertion.setID(getBean().getAssertionId());
        }
    }

    /**
     * this one is SAML2 only
     * 
     * @param assertion
     * @throws Exception
     */
    private void setAssertionAttribute(Assertion assertion) throws Exception
    {
        AssertionAttributeBean attribute = getBean().getAssertionAttribute();
        if (attribute != null && !getBean().isAttributeEmpty())
        {
            generateAssertionAttribute(attribute, assertion);
        } else if (attribute == null)
        {
            assertion.getAttributeStatements().clear();
        }
    }

    /**
     * SAML 2 specific
     * 
     * @param assertion
     * @throws Exception
     */
    private void setAssertionAuthStatement(Assertion assertion) throws Exception
    {
        if (getBean().getIsNeedAuth() == OnOffEnum.Off)
        {
            assertion.getAuthnStatements().clear();
        } else
        {
            AuthnStatement authStat = assertion.getAuthnStatements().get(0);
            authStat.setAuthnInstant(getSAMLDateTime(getBean().getAuthtime(), 0));
            if (!StringUtils.isNull(getBean().getSessionIndex()))
            {
                authStat.setSessionIndex(getBean().getSessionIndex());
            } else
            {
                authStat.setSessionIndex(generateSAMLID());
            }
            if (getBean().isSessionNeedNotAfter())
            {
                authStat.setSessionNotOnOrAfter(getSAMLDateTime(getBean().getSessNotAfter(), getDefaultGapSeconds()));
            }
            if (getBean().getIsNeedAuth() == OnOffEnum.On)
            {
                Element dom = Configuration.getMarshallerFactory().getMarshaller(authStat).marshall(authStat);
                AuthnStatement authStat2 = (AuthnStatement) Configuration.getUnmarshallerFactory().getUnmarshaller(dom)
                        .unmarshall(dom);
                authStat2.setSessionIndex(generateSAMLID());
                assertion.getAuthnStatements().add(authStat2);
            }
        }
    }

    /**
     * Assertion part<br>
     * NameID==User
     */
    private void setSubject(Subject subject)
    {
        if (!StringUtils.isNullOrEmpty(getBean().getUser()))
        {
            subject.getNameID().setValue(getBean().getUser());
        } else if (!StringUtils.isNullOrEmpty(getBean().getNameId()))
        {
            subject.getNameID().setValue(getBean().getNameId());
        } else
        {
            subject.getNameID().setValue(getAdminUser());
        }

        // ADD: nameid format support, this should be ok
        if (!StringUtils.isNullOrEmpty(getBean().getNameIdFormat()))
        {
            subject.getNameID().setFormat(getBean().getNameIdFormat());
        } else
        {
            // urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified
            // urn:oasis:names:tc:SAML:1.0:am:unspecified
            // urn:oasis:names:tc:SAML:2.0:nameid-format:persistent
            // urn:oasis:names:tc:SAML:2.0:nameid-format:transient
            subject.getNameID().setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        }
    }

    /**
     * Subject time condition, responseTo, recipient
     * 
     * @param sc
     * @throws Exception
     */
    private void setSubjectConfirmation(SubjectConfirmation sc) throws Exception
    {
        if (!StringUtils.isNullOrEmpty(getBean().getConfirmationMethod()))
        {
            sc.setMethod(getBean().getConfirmationMethod());
        }
        SubjectConfirmationData scd = sc.getSubjectConfirmationData();

        // subject datetime
        if (!getBean().isSubjNeedNotBefore())
        {
            scd.setNotBefore(null);
        } else
        {
            scd.setNotBefore(getSAMLDateTime(getBean().getSubjNotBefore(), -getDefaultGapSeconds()));
        }

        if (!getBean().isSubjNeedNotAfter())
        {
            scd.setNotOnOrAfter(null);
        } else
        {
            scd.setNotOnOrAfter(getSAMLDateTime(getBean().getSubjNotOnOrAfter(), getDefaultGapSeconds()));
        }

        if (StringUtils.isNull(getBean().getSubjectResponseTo()))
        {
            scd.setInResponseTo(null);
        } else if (StringUtils.isEmpty(getBean().getSubjectResponseTo()))
        {
            scd.setInResponseTo(generateSAMLID());
        } else
        {
            scd.setInResponseTo(getBean().getSubjectResponseTo());
        }

        if (StringUtils.isNullOrEmpty(getBean().getRecipient()))
        {
            // SCD.SETRECIPIENT(GETPROPERTY("SERVERURL") +
            // SSOCONFIG.GETPROPERTY("SAML2.ASSERTION.CONSUMER.SUFFIX")
            // + "?COMPANY=" + GETPROPERTY("COMPANY"));
            throw new Exception("set recipient");
        } else
        {
            scd.setRecipient(getBean().getRecipient());
        }

    }

    /**
     * Assertion condition<br>
     * Not only time condition but also audience
     * 
     * @param cond
     * @throws Exception
     */
    private void setCondition(Conditions cond) throws Exception
    {
        if (!getBean().isCondiNeedNotBefore())
        {
            cond.setNotBefore(null);
        } else
        {
            cond.setNotBefore(getSAMLDateTime(getBean().getCondiNotBefore(), -getDefaultGapSeconds()));
        }

        if (!getBean().isCondiNeedNotAfter())
        {
            cond.setNotOnOrAfter(null);
        } else
        {
            cond.setNotOnOrAfter(getSAMLDateTime(getBean().getCondiNotOnOrAfter(), getDefaultGapSeconds()));
        }

        String audience = getBean().getAudience();
        if (audience == null)
        {
            throw new Exception("Set Audience!");
        }
        cond.getAudienceRestrictions().get(0).getAudiences().get(0).setAudienceURI(audience);
    }

    private void setSignature() throws Exception
    {
        switch (getBean().getSign())
        {
            case Neither:
                // no signature
                break;
            case Assertion:
                signAssertion();
                break;
            case Both:
                signAssertion();
                signResponse(getBean().isSkipEncrypt());
                break;
            case Response:
                signResponse(getBean().isSkipEncrypt());
                break;
            case Ignore:
                // ignore
            default:
                break;
        }
    }

    private void encryptSignature() throws Exception
    {
        if (!getBean().isSkipEncrypt())
        {
            EncryptTargetEnum encrypt = getBean().getEncryptTarget();
            BasicX509Credential credential = getBasicX509Credential(getBean().isEncryptSignCertSp());
            Encrypter samlEncrypter = generateEncrypterFromCredential(credential);
            Assertion assertion = resp.getAssertions().get(0);
            if (encrypt == EncryptTargetEnum.Assertion)
            {
                EncryptedAssertion encryptedAssertion = samlEncrypter.encrypt(assertion);
                resp.getAssertions().clear();
                resp.getEncryptedAssertions().add(encryptedAssertion);
            } else if (encrypt == EncryptTargetEnum.NameId)
            {
                EncryptedID encryptedNameId = samlEncrypter.encrypt(assertion.getSubject().getNameID());
                assertion.getSubject().setNameID(null);
                assertion.getSubject().setEncryptedID(encryptedNameId);
            }

            // if sign response or sign both
            if (getBean().getSign() == RequireMandatorySignatureEnum.Response
                    || getBean().getSign() == RequireMandatorySignatureEnum.Both)
            {
                resp.setSignature(signature);
                Configuration.getMarshallerFactory().getMarshaller(resp).marshall(resp);
                Signer.signObject(signature);
            }
        }
    }

    private void generateAssertionAttribute(AssertionAttributeBean attribute, Assertion assertion) throws Exception
    {
        AttributeStatement attrstat = assertion.getAttributeStatements().get(0);
        Field[] fields = attribute.getClass().getDeclaredFields();
        String fieldName;
        String fieldValue;
        boolean isNeedSecondAttrStat = false;
        for (Field field : fields)
        {
            field.setAccessible(true);
            fieldName = field.getName();
            if (field.getType() == String.class)
            {
                fieldValue = (String) field.get(attribute);
            } else
            {
                fieldValue = null;
            }
            if (!StringUtils.isNull(fieldValue))
            {
                Attribute attr = (Attribute) Configuration.getBuilderFactory()
                        .getBuilder(Attribute.DEFAULT_ELEMENT_NAME).buildObject(Attribute.DEFAULT_ELEMENT_NAME);
                attr.setName(fieldName);
                attr.setFriendlyName(fieldName);
                attr.setNameFormat(Attribute.BASIC);

                XSString attrv = (XSString) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME)
                        .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                if (StringUtils.isEmpty(fieldValue))
                {
                    fieldValue = null;
                }
                attrv.setValue(fieldValue);
                attr.getAttributeValues().add(attrv);
                attrstat.getAttributes().add(attr);
                if (fieldName.equals("username") || fieldName.equals("password"))
                {
                    isNeedSecondAttrStat = true;
                }
            } ;
        }

        if (attribute.isNeedSecondAttrStat() && isNeedSecondAttrStat)
        {
            Element dom = Configuration.getMarshallerFactory().getMarshaller(attrstat).marshall(attrstat);
            AttributeStatement attrStat2 = (AttributeStatement) Configuration.getUnmarshallerFactory()
                    .getUnmarshaller(dom).unmarshall(dom);
            attrStat2.getAttributes().get(0).setName("unknown");
            attrStat2.getAttributes().get(0).setFriendlyName("unknown");
            assertion.getAttributeStatements().add(attrStat2);
        }
    }

    /**
     * Here not call Signer.signObject as may encrypt later
     * 
     * @throws Exception
     */
    private void signResponse(boolean isSign) throws Exception
    {
        BasicX509Credential credential = getBasicX509Credential(getBean().isSignCertSp());
        signature = generateSignatureFromCredential(credential);
        if (isSign)
        {
            resp.setSignature(signature);
            Configuration.getMarshallerFactory().getMarshaller(resp).marshall(resp);
            Signer.signObject(signature);
        }
    }

    private void signAssertion() throws Exception
    {
        BasicX509Credential credential = getBasicX509Credential(getBean().isSignCertSp());
        signature = generateSignatureFromCredential(credential);
        Assertion assertion = resp.getAssertions().get(0);
        assertion.setSignature(signature);
        Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);
    }

    private Encrypter generateEncrypterFromCredential(BasicX509Credential credential)
    {
        EncryptionParameters encParams = new EncryptionParameters();
        encParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);

        KeyEncryptionParameters kekParams = new KeyEncryptionParameters();
        kekParams.setEncryptionCredential(credential);
        kekParams.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        KeyInfoGeneratorFactory kigf = Configuration.getGlobalSecurityConfiguration().getKeyInfoGeneratorManager()
                .getDefaultManager().getFactory(credential);
        kekParams.setKeyInfoGenerator(kigf.newInstance());

        Encrypter samlEncrypter = new Encrypter(encParams, kekParams);
        samlEncrypter.setKeyPlacement(Encrypter.KeyPlacement.PEER);
        return samlEncrypter;
    }

    public enum OnOffEnum
    {
        On, Default, Off;
        public boolean toBoolean()
        {
            if (name().equals(On.toString()))
            {
                return true;
            } else
            {
                return false;
            }
        }
    }
}
