import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Dani on 23/03/2017.
 */
class MessageServer {

    static char threadCharId = 'A';

    public static void main(String[] args) {
        //to check whether we have enough arguments to start the server
        if (args.length < 1) {
            System.out.println("Make sure to provide the port number!");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        System.out.println("Starting Message server on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            MessageBoard messageBoard = new MessageBoard();
            //for every client, there is a new Handler responsible, but all using the same MessageBoard
            ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            //the while loop is not supposed to finish, it is required to eun indefinitely unless interrupted.
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("connection : " + clientSocket.getInetAddress());
                executorService.execute(new MessageServerHandler(messageBoard, clientSocket));
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
