package no.ks.fiks.streaming.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.client.InputStreamRequestContent;
import org.eclipse.jetty.client.MultiPartRequestContent;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.MultiPart;

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
        content.addPart(new MultiPart.ContentSourcePart(name, null, null, new StringRequestContent(toString(metadata), contentType, charset)));
    }

    public void addFieldPart(Object metadata, String name) {
        content.addPart(new MultiPart.ContentSourcePart(name, null, null, new StringRequestContent(toString(metadata))));
    }

    public void addFileData(List<FilForOpplasting<Object>> filer) {
        int fileId = 0;
        for (FilForOpplasting<?> dokument : filer) {
            content.addPart(new MultiPart.ContentSourcePart(String.format("metadata:%s", fileId),
                    null,
                    null,
                    new StringRequestContent(toString(dokument.metadata()))));
            content.addPart(new MultiPart.ContentSourcePart(String.format("dokument:%s", fileId),
                    dokument.filnavn(),
                    null,
                    new InputStreamRequestContent(dokument.data())
            ));
            fileId++;
        }
    }

    public void addFileData(List<FilForOpplasting<Object>> filer, String metadataContentType, Charset charset) {
        int fileId = 0;
        for (FilForOpplasting<?> dokument : filer) {
            content.addPart(new MultiPart.ContentSourcePart(String.format("metadata:%s", fileId),
                    null,
                    null,
                    new StringRequestContent(metadataContentType, toString(dokument.metadata()), charset)));
            content.addPart(new MultiPart.ContentSourcePart(String.format("dokument:%s", fileId),
                    dokument.filnavn(),
                    null,
                    new InputStreamRequestContent(dokument.data())
            ));
            fileId++;
        }
    }

    public void addFileData(FilForOpplasting<Object> fil) {
        content.addPart(new MultiPart.ContentSourcePart("metadata", null, null, new StringRequestContent(toString(fil.metadata()))));
        content.addPart(new MultiPart.ContentSourcePart("dokument", fil.filnavn(), null, new InputStreamRequestContent(fil.data())));
    }

    public void addFileData(FilForOpplasting<Object> fil, String metadataContentType, Charset charset) {
        content.addPart(new MultiPart.ContentSourcePart("metadata", null, null, new StringRequestContent(metadataContentType, toString(fil.metadata()), charset)));
        content.addPart(new MultiPart.ContentSourcePart("dokument", fil.filnavn(), null, new InputStreamRequestContent(fil.data())));
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