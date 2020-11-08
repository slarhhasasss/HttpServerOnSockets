public class Main {
    public static void main(String[] args) {
        new Server(((httpReq, httpResp) -> "<html> <body> Hello, Test server! </body> </html>")).bootstrap();
    }
}

