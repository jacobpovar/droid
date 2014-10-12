package uk.gov.nationalarchvies.droid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    final String signatureFile = "DROID_SignatureFile_V78.xml";
    private FileFormatIdentifier fileChecker;
    private Map<String, Format> formatMap = new HashMap();
    private FFSignatureFile sigFile;

    public FileFormat process(String fileName)
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

            List<IdentificationResult> listResults = identificationResultCollection.getResults();
            if (listResults.isEmpty()) {
                throw new Exception("Failed to check file");
            }
            return getFirstMatchedResultOf(identificationResultCollection);
        } catch (IOException e) {
            try {
                String extension = getFileExtension(file);
                return this.sigFile.getFileFormatsForExtension(extension).get(0);
            } catch (Exception ex) {
                e.printStackTrace();
                throw new Exception("Failed to check file");
            }
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

    private FileFormat getFirstMatchedResultOf(IdentificationResultCollection identificationResultCollection) {
        IdentificationResult firstResult = identificationResultCollection.getResults().get(0);
        return this.sigFile.getFileFormat(firstResult.getPuid());
    }

    public void setFileChecker(FileFormatIdentifier fileChecker) {
        this.fileChecker = fileChecker;
        String containerSignatureFile = "container-signature-20140922.xml";
        this.fileChecker.setFileSignaturesFileName(signatureFile);
        this.fileChecker.setContainerSignaturesFileName(containerSignatureFile);
        try {
            this.fileChecker.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
