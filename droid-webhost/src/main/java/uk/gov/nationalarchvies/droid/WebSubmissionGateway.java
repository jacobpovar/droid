package uk.gov.nationalarchvies.droid;

import uk.gov.nationalarchives.droid.core.signature.FileFormat;

public interface WebSubmissionGateway {
    public FileFormat process(String paramString) throws Exception;

    public abstract void setFileChecker(FileFormatIdentifier paramFileFormatIdentifier);
}
