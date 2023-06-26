package no.ks.fiks.streaming.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.client.util.*;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

public class MultipartContentProviderBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MultiPartRequestContent content;

    public MultipartContentProviderBuilder() {
        content = new MultiPartRequestContent();
    }

    public void addFieldPart(Object metadata, String name, String contentType, Charset charset) {
        content.addFieldPart(name, new StringRequestContent(contentType, toString(metadata), charset), null);
    }

    public void addFieldPart(Object metadata, String name) {
        content.addFieldPart(name, new StringRequestContent(toString(metadata)), null);
    }

    public void addFileData(List<FilForOpplasting<Object>> filer) {
        int fileId = 0;
        for (FilForOpplasting<?> dokument : filer) {
            content.addFieldPart(String.format("metadata:%s", fileId), new StringRequestContent(toString(dokument.metadata())), null);
            content.addFilePart(String.format("dokument:%s", fileId), dokument.filnavn(), new InputStreamRequestContent(dokument.data()), null);
            fileId++;
        }
    }

    public void addFileData(List<FilForOpplasting<Object>> filer, String metadataContentType, Charset charset) {
        int fileId = 0;
        for (FilForOpplasting<?> dokument : filer) {
            content.addFieldPart(String.format("metadata:%s", fileId), new StringRequestContent(metadataContentType, toString(dokument.metadata()), charset), null);
            content.addFilePart(String.format("dokument:%s", fileId), dokument.filnavn(), new InputStreamRequestContent(dokument.data()), null);
            fileId++;
        }
    }

    public void addFileData(FilForOpplasting<Object> fil) {
        content.addFieldPart("metadata", new StringRequestContent(toString(fil.metadata())), null);
        content.addFilePart("dokument", fil.filnavn(), new InputStreamRequestContent(fil.data()), null);
    }

    public void addFileData(FilForOpplasting<Object> fil, String metadataContentType, Charset charset) {
        content.addFieldPart("metadata", new StringRequestContent(metadataContentType, toString(fil.metadata()), charset), null);
        content.addFilePart("dokument", fil.filnavn(), new InputStreamRequestContent(fil.data()), null);
    }

    public MultiPartRequestContent build() {
        content.close();
        return content;
    }

    private String toString(Object metadata) {
        if (metadata instanceof String) {
            return (String) metadata;
        }
        try {
            return objectMapper.writeValueAsString(Objects.requireNonNull(metadata));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Feil under serialisering av metadata", e);
        }
    }

}