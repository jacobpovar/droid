package uk.gov.nationalarchvies.droid;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.container.Ole2ContainerContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.ZipContainerContentIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.TriggerPuid;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ResultsGenerator {
    private static final String R_SLASH = "/";
    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";
    private static final String SPACE = " ";
    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private List<TriggerPuid> triggerPuids;
    private IdentificationRequestFactory requestFactory;
    private String path;
    private String slash;
    private String slash1;
    private String wrongSlash;
    private boolean archives;
    private final String OLE2_CONTAINER = "OLE2";
    private final String ZIP_CONTAINER = "ZIP";
    private final String ZIP_ARCHIVE = "x-fmt/263";
    private final String JIP_ARCHIVE = "x-fmt/412";
    private final String TAR_ARCHIVE = "x-fmt/265";
    private final String GZIP_ARCHIVE = "x-fmt/266";

    public ResultsGenerator(BinarySignatureIdentifier binarySignatureIdentifier, ContainerSignatureDefinitions containerSignatureDefinitions, String path, String slash, String slash1) {
        this.binarySignatureIdentifier = binarySignatureIdentifier;
        this.containerSignatureDefinitions = containerSignatureDefinitions;
        this.path = path;
        this.slash = slash;
        this.slash1 = slash1;
        this.wrongSlash = (this.slash.equals("/") ? "\\" : "/");

        if (containerSignatureDefinitions != null) {
            this.triggerPuids = containerSignatureDefinitions.getTiggerPuids();
        }
    }

    public IdentificationResultCollection get(IdentificationResultCollection results, IdentificationRequest request)
            throws CommandExecutionException {
        String fileName = (this.path + request.getFileName()).replace(this.wrongSlash, this.slash);
        IdentificationResultCollection containerResults = getContainerResults(results, request, fileName);

        IdentificationResultCollection finalResults = new IdentificationResultCollection(request);

        if (containerResults.getResults().size() > 0) {
            finalResults = containerResults;
        } else if (results.getResults().size() > 0) {
            finalResults = results;
        }

        if (finalResults.getResults().size() > 0) {
            this.binarySignatureIdentifier.removeLowerPriorityHits(finalResults);
        }

        return finalResults;
    }

    private IdentificationResultCollection getContainerResults(IdentificationResultCollection results, IdentificationRequest request, String fileName)
            throws CommandExecutionException {
        IdentificationResultCollection containerResults = new IdentificationResultCollection(request);

        if ((results.getResults().size() > 0) && (this.containerSignatureDefinitions != null)) {
            for (IdentificationResult identResult : results.getResults()) {
                String filePuid = identResult.getPuid();
                if (filePuid != null) {
                    Optional<TriggerPuid> containerPuid = getTriggerPuidByPuid(filePuid);

                    if (containerPuid.isPresent()) {
                        this.requestFactory = new ContainerFileIdentificationRequestFactory();
                        String containerType = containerPuid.get().getContainerType();

                        if ("OLE2".equals(containerType)) {
                            try {
                                Ole2ContainerContentIdentifier ole2Identifier = new Ole2ContainerContentIdentifier();

                                ole2Identifier.init(this.containerSignatureDefinitions, containerType);
                                Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
                                ole2IdentifierEngine.setRequestFactory(this.requestFactory);
                                ole2Identifier.setIdentifierEngine(ole2IdentifierEngine);
                                containerResults = ole2Identifier.process(request.getSourceInputStream(), containerResults);
                            } catch (IOException e) {
                                System.err.println(e + " " + "(" + fileName + ")");
                            }
                        } else if ("ZIP".equals(containerType)) {
                            try {
                                ZipContainerContentIdentifier zipIdentifier = new ZipContainerContentIdentifier();

                                zipIdentifier.init(this.containerSignatureDefinitions, containerType);
                                ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
                                zipIdentifierEngine.setRequestFactory(this.requestFactory);
                                zipIdentifier.setIdentifierEngine(zipIdentifierEngine);
                                containerResults = zipIdentifier.process(request.getSourceInputStream(), containerResults);
                            } catch (IOException e) {
                                System.err.println(e + " " + "(" + fileName + ")");
                            }
                        } else {
                            throw new CommandExecutionException("Unknown container type: " + containerPuid);
                        }
                    }
                }
            }
        }

        return containerResults;
    }

    private Optional<TriggerPuid> getTriggerPuidByPuid(String puid) {
        return this.triggerPuids.stream().filter(tp -> tp.getPuid().equals(puid)).findFirst();
    }
}
