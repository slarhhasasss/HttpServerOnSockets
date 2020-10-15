import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Handler extends Thread {

    private static final Map<String, String> contentTypes = new HashMap<>() {{
        put("jpg", "image/jpeg");
        put("html", "text/html");
        put("json", "application/json");
        put("txt", "text/plain");
        put("", "text/plain");
        put("docx", "text/docx");
        put("pdf", "text/pdf");
    }};
    private static final String NOT_FOUND_MESSAGE = "NOT FOUND";

    private Socket socket;
    private String dir;


    public Handler(Socket socket, String dir) {
        this.socket = socket;
        this.dir = dir;
    }

    @Override
    public void run() {
        try(InputStream is = this.socket.getInputStream();
            OutputStream os = this.socket.getOutputStream()) {

            String url = getRequestUrl(is);

            var filePath = "-1".equals(url) ? Path.of(dir) : Path.of(dir, url);

            if (!socket.isConnected() || socket.isClosed())  {
                return;
            }

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String extension = getFileExtension(filePath);
                String type = contentTypes.get(extension);

                byte[] fileBytes = Files.readAllBytes(filePath);
                sendHeader(os, 200, "OK", type, fileBytes.length);
                os.write(fileBytes);

                System.out.println("sent file: " + filePath.toString());

            } else {
                String type = contentTypes.get("text");
                sendHeader(os, 404, NOT_FOUND_MESSAGE, type, NOT_FOUND_MESSAGE.length());
                os.write(NOT_FOUND_MESSAGE.getBytes());

                System.out.println("sent file: " + NOT_FOUND_MESSAGE);
            }

        }catch (SocketException ex) {
            System.out.println("Socket ex: " + ex.getMessage());
        } catch (IOException | NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    private String getRequestUrl(InputStream is) {
        //выставляем разделитель
        Scanner reader = new Scanner(is).useDelimiter("\r\n");
        //Эта строка по стандарту имеет вид "METHOD /url HTTP/1.1"
        String line = reader.next();
        String[] args = line.split(" ");
        return args[1];
    }

    private void sendHeader(OutputStream os, int statusCode, String statusText, String type, long length) {
        PrintStream ps = new PrintStream(os);

        //записываем headers
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", length);
    }

    private String getFileExtension(Path path) {
        String name = path.getFileName().toString();
        int extStart = name.lastIndexOf(".");
        //возвращаем расширение файла
        return extStart == -1 ? "" : name.substring(extStart + 1, name.length() - 1);
    }

}
