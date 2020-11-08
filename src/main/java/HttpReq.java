import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpReq {

    private final String message;
    private final static String DEL = "\r\n\r\n";
    private final static String NEW_LINE = "\r\n";
    private final static String HEADER_DEL = ":";

    private final HttpMethod method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;


    public HttpReq(String message) {
        this.message = message;

        String[] parts = message.split(DEL);

        String head = parts[0];

        String[] headers = head.split(NEW_LINE);

        String[] firstLine = headers[0].split(" ");

        method = HttpMethod.valueOf(firstLine[0]);
        url = firstLine[1];

        this.headers = Collections.unmodifiableMap(
          new HashMap<String, String>(){{
              for (int i = 1; i < headers.length; i++) {
                  String[] headerPair = headers[i].split(HEADER_DEL, 2);
                  put(headerPair[0].trim(), headerPair[1].trim());
              }
          }}
        );

        String bodyLen = this.headers.get("Content-Length");
        int len = bodyLen != null ? Integer.parseInt(bodyLen) : 0;
        this.body = parts.length > 1 ? parts[1].trim().substring(0, len) : "";
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
