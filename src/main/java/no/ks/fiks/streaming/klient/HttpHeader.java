package no.ks.fiks.streaming.klient;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpHeader {

    private String headerName;
    private String headerValue;

}
