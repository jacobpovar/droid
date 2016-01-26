package uk.gov.nationalarchvies.droid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.xml.bind.JAXBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

public class FileFormatIdentifierImpl
        implements FileFormatIdentifier
{
    private static final String FORWARD_SLASH = "/";
    private static final String BACKWARD_SLASH = "\\";
    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ResultsGenerator resultsGenerator;
    private String fileSignaturesFileName;
    private String containerSignaturesFileName;
    private int maxBytesToScan = -1;
    private Log log = LogFactory.getLog(getClass());
    private String path;
    private String slash;
    private String slash1;
    private ContainerSignatureDefinitions containerSignatureDefinitions;

    public void init()
            throws Exception
    {
        this.binarySignatureIdentifier = new BinarySignatureIdentifier();

        File fileSignaturesFile = new File(this.fileSignaturesFileName);
        if (!fileSignaturesFile.exists())
            throw new Exception("Signature file not found");

        this.binarySignatureIdentifier.setSignatureFile(this.fileSignaturesFileName);
        try {
            this.binarySignatureIdentifier.init();
        } catch (SignatureParseException e)         {
            throw new Exception("Can't parse signature file");
        }

        this.binarySignatureIdentifier.setMaxBytesToScan(this.maxBytesToScan);

        this.path = fileSignaturesFile.getAbsolutePath();
        this.slash = (this.path.contains("/") ? "/" : "\\");
        this.slash1 = this.slash;

        this.containerSignatureDefinitions = null;
        if (this.containerSignaturesFileName != null)         {
            File containerSignaturesFile = new File(this.containerSignaturesFileName);
            InputStream in = null;
            if (!containerSignaturesFile.exists()) {
                throw new CommandExecutionException("Container signature file not found");
            }

            try {
                in = new FileInputStream(this.containerSignaturesFileName);
                ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
                this.containerSignatureDefinitions = parser.parse(in);
                if (in != null) {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        throw new Exception("Error closing InputStream on signature file");
                    }
                }
                this.resultsGenerator = new ResultsGenerator(this.binarySignatureIdentifier, this.containerSignatureDefinitions, this.path, this.slash, this.slash1);
            }
            catch (SignatureParseException e)
            {
                throw new Exception("Can't parse container signature file");
            }
            catch (IOException ioe)
            {
                throw new Exception(ioe);
            }
            catch (JAXBException jaxbe)
            {
                throw new Exception(jaxbe);
            }
            finally
            {
                if (in != null) {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        throw new Exception("Error closing InputStream on signature file");
                    }
                }
            }
        }
    }

    public void setFileSignaturesFileName(String fileSignaturesFileName)
    {
        this.fileSignaturesFileName = fileSignaturesFileName;
    }

    public void setContainerSignaturesFileName(String containerSignaturesFileName)
    {
        this.containerSignaturesFileName = containerSignaturesFileName;
    }

    public IdentificationResultCollection get(File file)
            throws Exception
    {
        this.path = "";
        String fileName;
        try
        {
            fileName = file.getCanonicalPath();
        }
        catch (IOException e)
        {
            throw new CommandExecutionException(e);
        }
        URI uri = file.toURI();
        RequestMetaData metaData = new RequestMetaData(Long.valueOf(file.length()), Long.valueOf(file.lastModified()), fileName);

        RequestIdentifier identifier = new RequestIdentifier(uri);
        identifier.setParentId(Long.valueOf(1L));

        InputStream in = null;
        IdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);
        try
        {
            in = new FileInputStream(file);
            request.open(in);
            IdentificationResultCollection results = this.binarySignatureIdentifier.matchBinarySignatures(request);
            return this.resultsGenerator.get(results, request);
        }
        catch (FileNotFoundException fnfe)
        {
            this.log.error("error processing files", fnfe);
            throw new CommandExecutionException(fnfe);
        }
        catch (IOException e)
        {
            throw new CommandExecutionException(e);
        }
        finally
        {
            if (in != null) {
                try
                {
                    request.close();
                    in.close();
                }
                catch (IOException e)
                {
                    throw new CommandExecutionException(e);
                }
            }
        }
    }

    public void setSignatureFile(String signatureFile)
    {
        this.fileSignaturesFileName = signatureFile;
    }

    public void setContainerSignatureFile(String containerSignatureFile)
    {
        this.containerSignaturesFileName = containerSignatureFile;
    }
}
