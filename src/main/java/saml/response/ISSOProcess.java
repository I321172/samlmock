package saml.response;

public interface ISSOProcess extends SSOOperation
{
    void prepare() throws Exception;

    void fillContent() throws Exception;

    /**
     * End the operation and get the result if need here
     * 
     * @return
     * @throws Exception
     */
    Object saveContent() throws Exception;

}
