package no.ks.fiks.streaming.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;

import java.nio.charset.Charset;
import java.util.List;

public class MultipartContentProviderBuilder {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final MultiPartContentProvider contentProvider;

  public MultipartContentProviderBuilder() {
    contentProvider = new MultiPartContentProvider();
  }

  public void addFieldPart(Object metadata, String name, String contentType, Charset charset) {
    contentProvider.addFieldPart(name, new StringContentProvider(contentType, serialiser(metadata), charset), null);
  }

  public void addFieldPart(Object metadata, String name) {
    contentProvider.addFieldPart(name, new StringContentProvider(serialiser(metadata)), null);
  }

  public void addFileData(List<FilForOpplasting<Object>> filer) {

    int fileId = 0;
    for (FilForOpplasting dokument : filer) {
      contentProvider.addFieldPart(String.format("metadata:%s", fileId), new StringContentProvider(serialiser(dokument.getMetadata())), null);
      contentProvider.addFilePart(String.format("dokument:%s", fileId), dokument.getFilnavn(), new InputStreamContentProvider(dokument.getData()), null);
      fileId++;
    }
  }

  public void addFileData(List<FilForOpplasting<Object>> filer, String metadataContentType, Charset charset) {

    int fileId = 0;
    for (FilForOpplasting dokument : filer) {
      contentProvider.addFieldPart(String.format("metadata:%s", fileId), new StringContentProvider(metadataContentType, serialiser(dokument.getMetadata()), charset), null);
      contentProvider.addFilePart(String.format("dokument:%s", fileId), dokument.getFilnavn(), new InputStreamContentProvider(dokument.getData()), null);
      fileId++;
    }
  }

  public void addFileData(FilForOpplasting<Object> fil) {
    contentProvider.addFieldPart("metadata", new StringContentProvider(serialiser(fil.getMetadata())), null);
    contentProvider.addFilePart("dokument", fil.getFilnavn(), new InputStreamContentProvider(fil.getData()), null);
  }

  public void addFileData(FilForOpplasting<Object> fil, String metadataContentType, Charset charset) {
    contentProvider.addFieldPart("metadata", new StringContentProvider(metadataContentType, serialiser(fil.getMetadata()), charset), null);
    contentProvider.addFilePart("dokument", fil.getFilnavn(), new InputStreamContentProvider(fil.getData()), null);
  }

  public MultiPartContentProvider build() {
    contentProvider.close();
    return contentProvider;
  }

  private String serialiser(@NonNull Object metadata) {
    try {
      return objectMapper.writeValueAsString(metadata);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Feil under serialisering av metadata", e);
    }
  }

}