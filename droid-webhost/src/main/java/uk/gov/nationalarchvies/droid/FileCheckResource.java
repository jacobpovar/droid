package uk.gov.nationalarchvies.droid;

import com.google.gson.Gson;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

public class FileCheckResource         extends ServerResource
{
    @Get
    public String represent()
    {
        try
        {
            String fileName = decodeFileNameFromRequest();
            Optional<FileFormat> fileFormat = SubmissionGatewayProvider.getInstance().process(fileName);

            Gson gson = new Gson();

            return gson.toJson(fileFormat);
        }
        catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, "There is not match");
        }

        return null;
    }

    private String decodeFileNameFromRequest() throws UnsupportedEncodingException {
        String encoded = (String)getRequest().getAttributes().get("filename");
        return URLDecoder.decode(encoded, "UTF-8" );
    }
}
