package no.kristiania;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    private int statusCode;
    private Map<String, String> headers = new HashMap<>();
    private String responseBody;

    public HttpClient(final String requestTarget, final String hostName, int port) throws IOException {
        this(requestTarget, hostName, port, "GET", null);
    }

    public HttpClient(final String requestTarget, final String hostName, int port, final String httpMethod, String requestBody) throws IOException {

        Socket socket = new Socket(hostName, port);

        String contentLengthHeader = requestBody != null ? "Content-Length: " + requestBody.length() + "\r\n" : "";

        String request = httpMethod + " " + requestTarget + " HTTP/1.1\r\n" +
                "Host:" + hostName + "\r\n" + contentLengthHeader + "\r\n";


        socket.getOutputStream().write(request.getBytes());

        if (requestBody != null) {
            socket.getOutputStream().write(requestBody.getBytes());
        }

        HttpMessage response = new HttpMessage(socket);

        String responseLine = response.getStartLine();
        headers = response.getHeaders();
        responseBody = response.getBody();

        String[] responseLineParts = responseLine.split(" ");

        statusCode = Integer.parseInt(responseLineParts[1]);

    }

    public static void main(String[] args) throws IOException {

        new HttpClient("/echo?status=200&body=Hello%20world!", "urlecho.appspot.com", 80);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseHeader(String headerName) {
        return headers.get(headerName);
    }

    public String getResponseBody() {
        return responseBody;

    }
}