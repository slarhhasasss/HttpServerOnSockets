import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Server {

    private static int BUFFER_SIZE = 256;
    private AsynchronousServerSocketChannel server;
    private final HttpHandler handler;

    Server(HttpHandler handler) {
        this.handler = handler;
    }

    private void handleClient(Future<AsynchronousSocketChannel> futureAccept)
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        System.out.println("New client!");

        //Через 30 сек выключается
        AsynchronousSocketChannel clientChannel = futureAccept.get(30, TimeUnit.SECONDS);

        while (clientChannel != null && clientChannel.isOpen()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder builder = new StringBuilder();
            boolean keepRead = true;

            while (keepRead) {
                int readRes = clientChannel.read(buffer).get();
                keepRead = readRes == BUFFER_SIZE;
                buffer.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
                builder.append(charBuffer);
                buffer.clear();
            }

            HttpReq request = new HttpReq(builder.toString());
            HttpResp response = new HttpResp();

            if (handler != null) {
                try {
                    String body = this.handler.handle(request, response);

                    if (body != null && !body.isBlank()) {
                        if (response.getHeaders().get("Content-Type") == null) {
                            response.addHeader("Content-Type", "text/html; charset=utf-8");
                        }
                        response.setBody(body);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    response.setStatusCode(500);
                    response.setStatus("Internal server error");
                    response.addHeader("Content-Type", "text/html; charset=utf-8");
                    response.setBody("<html><body><h1>Error on server!</h1></body></html>");
                }

            } else {
                response.setStatusCode(404);
                response.setStatus("Not found");
                response.addHeader("Content-Type", "text/html; charset=utf-8");
                response.setBody("<html><body><h1>Resource not found</h1></body></html>");
            }

            ByteBuffer resp = ByteBuffer.wrap(response.getBytes());

            clientChannel.write(resp);

            clientChannel.close();
        }
    }

    public void bootstrap() {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("127.0.0.1", 8088));

            while (true) {
                Future<AsynchronousSocketChannel> futureAccept = server.accept();
                handleClient(futureAccept);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

}
