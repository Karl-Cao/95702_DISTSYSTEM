import java.net.*;
import java.io.*;

public class SimpleHTTPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 7777; // the server port we are using

            // Create a new server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);
            System.out.println("Server listening on port " + serverPort);

            while (true) {
                Socket clientSocket = listenSocket.accept();
                handleClient(clientSocket);
            }

        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader inFromSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outToSocket = clientSocket.getOutputStream();

            String requestLine = inFromSocket.readLine();
            System.out.println("Received: " + requestLine);

            if (requestLine != null) {
                String filePath = parseFilePath(requestLine);
                System.out.println("Requested file: " + filePath);

                sendResponse(outToSocket, filePath);
            }

            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }

    private static String parseFilePath(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts.length > 1 ? parts[1] : "/";
    }

    private static void sendResponse(OutputStream outToSocket, String filePath) throws IOException {
        File file = new File("." + filePath);
        if (file.exists() && !file.isDirectory()) {
            // File found, send 200 OK response
            String header = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n";
            outToSocket.write(header.getBytes());

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    outToSocket.write(buffer, 0, bytesRead);
                }
            }
        } else {
            // File not found, send 404 response
            String response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\n\r\n" +
                    "<html><body><h1>404 Not Found</h1></body></html>";
            outToSocket.write(response.getBytes());
        }
        outToSocket.flush();
    }
}