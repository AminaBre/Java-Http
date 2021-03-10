package no.kristiania;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpClientTest {

    @Test
    void shouldReturnSuccessfullStatusCode() throws IOException{
        HttpClient client = createEchoRequest("/echo");
    }

    @Test
    void shouldReturnErrorStatusCode() throws IOException{
        HttpClient client = createEchoRequest("/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldReadResponseHeader() throws IOException{
        HttpClient client = createEchoRequest("/echo?body=Kristiania");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }

    @Test
    void shouldReadResponseBody() throws IOException{
        HttpClient client = createEchoRequest("/echo?body=Kristiania");
        assertEquals("Kristiania", client.getResponseBody());
    }

    private HttpClient createEchoRequest(String requestTarget) throws IOException {
        return new HttpClient(requestTarget, "urlecho.appspot.com", 80);
    }
}
