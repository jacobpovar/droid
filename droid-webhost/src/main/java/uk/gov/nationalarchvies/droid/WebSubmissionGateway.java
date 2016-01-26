package uk.gov.nationalarchvies.droid;

import uk.gov.nationalarchives.droid.core.signature.FileFormat;

import java.util.Optional;

public interface WebSubmissionGateway {
    public Optional<FileFormat> process(String paramString) throws Exception;

    public abstract void setFileChecker(FileFormatIdentifier paramFileFormatIdentifier);
}
