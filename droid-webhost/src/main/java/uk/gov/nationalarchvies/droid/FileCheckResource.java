package uk.gov.nationalarchvies.droid;

import com.google.gson.Gson;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;

public class FileCheckResource
        extends ServerResource
{
    @Get
    public String represent()
    {
        try
        {
            String fileName = decodeFileNameFromRequest();
            FileFormat fileFormat = SubmissionGatewayProvider.getInstance().process(fileName);

            Gson gson = new Gson();

            return gson.toJson(fileFormat);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, "There is not match");
        }
        return null;
    }

    private String decodeFileNameFromRequest()
            throws Base64DecodingException
    {
        String fileNameInBase64 = (String)getRequest().getAttributes().get("filename");
        byte[] bytes = Base64.decode(fileNameInBase64);
        return new String(bytes);
    }
}
