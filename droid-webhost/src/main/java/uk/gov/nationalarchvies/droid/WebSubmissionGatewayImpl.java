package uk.gov.nationalarchvies.droid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import uk.gov.nationalarchives.droid.core.SignatureFileParser;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.signature.FormatCallback;
import uk.gov.nationalarchives.droid.signature.SaxSignatureFileParser;
import uk.gov.nationalarchives.droid.signature.SignatureParser;

public class WebSubmissionGatewayImpl implements WebSubmissionGateway {
    final String signatureFile = "DROID_SignatureFile_V82.xml";
    private FileFormatIdentifier fileChecker;
    private Map<String, Format> formatMap = new HashMap();
    private FFSignatureFile sigFile;

    public Optional<FileFormat> process(String fileName)
            throws Exception {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new Exception("File not found");
        }
        URI uri = file.toURI();
        RequestMetaData metaData = null;

        try {
            metaData = new RequestMetaData(Long.valueOf(file.length()), Long.valueOf(file.lastModified()), file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestIdentifier identifier = new RequestIdentifier(uri);
        identifier.setParentId(Long.valueOf(1L));

        InputStream in = null;
        IdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);

        try {
            in = new FileInputStream(file);
            request.open(in);
            IdentificationResultCollection identificationResultCollection = this.fileChecker.get(file);
            return identificationResultCollection.getResults().stream().findFirst().map(x -> x.getPuid()).map(this.sigFile::getFileFormat);

        } catch (Exception e) {
            String extension = getFileExtension(file);
            return this.sigFile.getFileFormatsForExtension(extension).stream().findFirst();

        } finally {
            request.close();
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if ((fileName.lastIndexOf(".") != -1) && (fileName.lastIndexOf(".") != 0)) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }

    public void init() throws Exception {
        File file = new File(signatureFile);
        SignatureParser sigParser = new SaxSignatureFileParser(file.toURI());

        FormatCallback callback = format -> WebSubmissionGatewayImpl.this.formatMap.put(format.getPuid(), format);
        sigParser.formats(callback);

        SignatureFileParser sigFileParser = new SignatureFileParser();
        URI signatureFileUri = new URI(signatureFile);
        this.sigFile = sigFileParser.parseSigFile(signatureFileUri.getPath());
        this.sigFile.prepareForUse();
    }

    public void setFileChecker(FileFormatIdentifier fileChecker) {
        this.fileChecker = fileChecker;
        String containerSignatureFile = "container-signature-20150327.xml";
        this.fileChecker.setFileSignaturesFileName(signatureFile);
        this.fileChecker.setContainerSignaturesFileName(containerSignatureFile);
        try {
            this.fileChecker.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
