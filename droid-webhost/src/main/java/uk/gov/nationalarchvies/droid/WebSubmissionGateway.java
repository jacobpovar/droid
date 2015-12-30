package uk.gov.nationalarchvies.droid;

import uk.gov.nationalarchives.droid.core.signature.FileFormat;

import java.util.Optional;

public interface WebSubmissionGateway {
    Optional<FileFormat> process(String paramString) throws Exception;

    void setFileChecker(FileFormatIdentifier paramFileFormatIdentifier);
}
