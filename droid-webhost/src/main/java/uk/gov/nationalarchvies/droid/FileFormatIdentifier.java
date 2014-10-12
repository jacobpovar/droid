package uk.gov.nationalarchvies.droid;

import java.io.File;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

public interface FileFormatIdentifier {
    void init() throws Exception;

    void setFileSignaturesFileName(String paramString);

    void setContainerSignaturesFileName(String paramString);

    IdentificationResultCollection get(File paramFile) throws Exception;
}
