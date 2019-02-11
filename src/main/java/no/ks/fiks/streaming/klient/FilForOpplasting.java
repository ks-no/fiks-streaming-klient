package no.ks.fiks.streaming.klient;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@Data
@Builder
public class FilForOpplasting<T> {

  private String filnavn;
  private T metadata;
  private InputStream data;

}
