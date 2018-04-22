import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Dani on 23/03/2017.
 */
class MessageClient {

    public static void main(String[] args) {
        // making sure that at least the required parameters are given
        if (args.length < 2) {
            System.out.println("Make sure to provide these two arguments!\n " +
                    "\t:host address of the server\n " +
                    "\t:port number on the server");
            System.exit(1);
        }
        // fetching the serverAddress and port from parameters
        String serverAddress = null;
        int port = 0;
        try {
            serverAddress = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid port");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        try (Socket server = new Socket(serverAddress, port)) {
            //setting up data stream for I/O
            BufferedReader fromServer = new BufferedReader(
                    new InputStreamReader(server.getInputStream(), "UTF-8"));

            DataOutputStream toServer = new DataOutputStream(server.getOutputStream());

            BufferedReader userInput = new BufferedReader(
                    new InputStreamReader(System.in));

            String request, response;
            System.out.println("Here is what you can do:" +
                    "\nSend:Hello will send a message to server, on SEND server will increment ID for message" +
                    " based on user" +
                    "\nGet:A+1 will return the message corresponding to that client and message ID" +
                    "\nList will show all the messages sent so far ..." +
                    "\nCommands, like SEND:, GET: and LIST must be in capital" +
                    "\nto drop the connection, just type BYE");
            do {
                do {
                    /*
                      * printing the '?' for user to know that program is ready for the request
                      * read the request
                      * and keep looping until there is a request
                      * (in case user just hits 'ENTER' without any request,
                      * we don't want to send that to server)
                     */
                    System.out.print("? ");
                    request = userInput.readLine();
                } while (request.isEmpty());

                //writing data to server (sending the request)
                toServer.writeBytes(request + "\n");
                toServer.flush();

                if (request.equals("BYE")) {
                    //on request being BYE we exit
                    System.exit(0);
                }
                //getting response from server
                response = fromServer.readLine();
                if (response != null && !response.equals("")) {
                    /*
                      * if response is not null, or ""
                      * we deal with it accordingly
                     */
                    if (request.equals("LIST")) {
                        /*
                          * if the request was of LIST, we will get the response as a String
                          * (SET, converted into a string)
                          * then we need to replace the separators ';' to '\n'
                          * so that every message appears on a new line
                         */
                        response = response.substring(0, response.length() - 1).replace(";", "\n");
                    }
                    writeLn(response);
                }
            //as we only kill the process when request is BYE
            // we don t need any condition here
            } while (true);
        } catch (UnknownHostException e) {
            writeLn("Unknown host: " + serverAddress + "\nHere is what went wrong!\n-----" + e
                    .getMessage() + "------");
            System.exit(1);
        } catch (IOException e) {
//            writeLn("Could not communicate with Server: " + serverAddress + "\nHere is what went wrong!\n-----" + e
//                    .getMessage() + "------");
            writeLn("Server closed connection");
            System.exit(1);
        }
    }

    private static void writeLn(Object toPrint) {
        //simply writes to console
        System.out.println(toPrint.toString());
    }
}
