package saml.response;

/**
 * @author jason<br>
 *         SSO related base
 */
public interface SSOOperation
{
    public void setBean(Object baseBean);

    public Object doSSOOperation() throws Exception;
}
