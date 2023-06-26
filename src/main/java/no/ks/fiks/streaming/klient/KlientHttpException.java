package no.ks.fiks.streaming.klient;

public class KlientHttpException extends RuntimeException {
    private final int status;
    private final String response;

    public KlientHttpException(String message, int status, String response) {
        super(message);
        this.status = status;
        this.response = response;
    }

    public int getStatus() {
        return status;
    }

    public String getResponse() {
        return response;
    }
}
