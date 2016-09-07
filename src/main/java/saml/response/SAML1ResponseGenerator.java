package saml.response;

import org.opensaml.Configuration;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.AuthenticationStatement;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.saml1.core.Response;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;

import saml.response.utils.StringUtils;

public class SAML1ResponseGenerator extends AbstractSAMLResponseGenerator
{
    private Response resp;

    @Override
    public void fillContent() throws Exception
    {
        // Response is made up of its parameters and assertion
        resp = (Response) unmarshaller.unmarshall(samlElement);

        // set response parameters
        setResponse(resp);

        // assertion is made up of its parameters and condition , authentication
        Assertion assertion = resp.getAssertions().get(0);
        setAssertion(assertion);

        Conditions cond = assertion.getConditions();
        setCondition(cond);

        AuthenticationStatement stat = assertion.getAuthenticationStatements().get(0);
        setAuthenticationStatement(stat);

        // set signature
        setSignature();
    }

    @Override
    public Object saveContent() throws Exception
    {
        // set signature
        return printXml(Configuration.getMarshallerFactory().getMarshaller(resp).marshall(resp));
    }

    private void setResponse(Response resp)
    {
        if (!StringUtils.isNullOrEmpty(responseBean.getResponseId()))
        {
            resp.setID(responseBean.getResponseId());
        } else
        {
            resp.setID(generateSAMLID());
        }

        if (!StringUtils.isNull(responseBean.getResponseTo()))
        {
            if (StringUtils.isEmpty(responseBean.getResponseTo()))
            {
                resp.setInResponseTo(generateSAMLID());
            } else
            {
                resp.setInResponseTo(responseBean.getResponseTo());
            }
        } else
        {
            resp.setInResponseTo(null);
        }

        resp.setIssueInstant(getSAMLDateTime(responseBean.getResponseTime(), 0));

        resp.setRecipient(responseBean.getRecipient());

    }

    /**
     * Set issuer here in assertion
     * 
     * @param assertion
     * @throws Exception
     */
    private void setAssertion(Assertion assertion) throws Exception
    {
        if (StringUtils.isNullOrEmpty(responseBean.getAssertionId()))
        {
            assertion.setID(generateSAMLID());
        } else
        {
            assertion.setID(responseBean.getAssertionId());
        }

        assertion.setIssueInstant(getSAMLDateTime(responseBean.getAssertionTimeGapSecs(), 0));

        if (StringUtils.isNullOrEmpty(responseBean.getIssuer()))
        {
            assertion.setIssuer(getSsoIssuer() != null ? getSsoIssuer() : "samltest.successfactors.com");
        } else
        {
            assertion.setIssuer(responseBean.getIssuer());
        }
    }

    private void setCondition(Conditions condition)
    {
        if (responseBean.isCondiNeedNotBefore())
        {
            condition.setNotBefore(getSAMLDateTime(responseBean.getCondiNotBefore(), -getDefaultGapSeconds()));
        } else
        {
            condition.setNotBefore(null);
        }
        if (responseBean.isCondiNeedNotAfter())
        {
            condition.setNotOnOrAfter(getSAMLDateTime(responseBean.getCondiNotOnOrAfter(), getDefaultGapSeconds()));
        } else
        {
            condition.setNotOnOrAfter(null);
        }
    }

    /**
     * Set User and authMethod, confirmation method
     * 
     * @param stat
     */
    private void setAuthenticationStatement(AuthenticationStatement stat)
    {
        if (StringUtils.isNull(responseBean.getUser()))
        {
            stat.getSubject().getNameIdentifier().setNameIdentifier(getAdminUser());
        } else if (StringUtils.isEmpty(responseBean.getUser()))
        {
            stat.getSubject().setNameIdentifier(null);
        } else
        {
            stat.getSubject().getNameIdentifier().setNameIdentifier(responseBean.getUser());
        }

        stat.setAuthenticationInstant(getSAMLDateTime(responseBean.getAuthtime(), 0));

        if (!StringUtils.isNullOrEmpty(responseBean.getAuthMethod()))
        {
            stat.setAuthenticationMethod(responseBean.getAuthMethod());
        }

        if (StringUtils.isNull(responseBean.getConfirmationMethod()))
        {
            stat.getSubject().getSubjectConfirmation().getConfirmationMethods().clear();
        } else
        {
            if (StringUtils.isEmpty(responseBean.getConfirmationMethod()))
            {
                stat.getSubject().getSubjectConfirmation().getConfirmationMethods().get(0)
                        .setConfirmationMethod("urn:oasis:names:tc:SAML:1.0:cm:bearer");
            } else
            {
                stat.getSubject().getSubjectConfirmation().getConfirmationMethods().get(0)
                        .setConfirmationMethod(responseBean.getConfirmationMethod());
            }
        }
    }

    private void setSignature() throws Exception
    {
        switch (responseBean.getSign())
        {
            case Neither:
                // no signature
                break;
            case Assertion:
                signAssertion();
                break;
            case Both:
                signAssertion();
                signResponse();
                break;
            case Response:
                signResponse();
                break;
            case Ignore:
                // ignore means default which is nothing
            default:
                break;
        }
    }

    private void signAssertion() throws Exception
    {
        BasicX509Credential credential = getBasicX509Credential(responseBean.isSignCertSp());
        Signature signature = generateSignatureFromCredential(credential);
        Assertion assertion = resp.getAssertions().get(0);
        assertion.setSignature(signature);
        Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);
    }

    private void signResponse() throws Exception
    {
        BasicX509Credential credential = getBasicX509Credential(responseBean.isSignCertSp());
        Signature signature = generateSignatureFromCredential(credential);
        resp.setSignature(signature);
        Configuration.getMarshallerFactory().getMarshaller(resp).marshall(resp);
        Signer.signObject(signature);
    }

}
