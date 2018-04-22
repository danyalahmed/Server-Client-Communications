import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Set;

/**
 * Created by Dani on 23/03/2017.
 */
class MessageServerHandler implements Runnable {
    private final char threadCharId;
    private final Socket clientSocket;
    private final MessageBoard messageBoard;
    private DataOutputStream toClient;
    private BufferedReader fromClient;
    //a boolean to know whether we want to kill this particular thread
    private boolean killThread = false;
    private int messageID = 0;

    public void run() {
        try {
            //setting up the input output streams.
            toClient = new DataOutputStream(clientSocket.getOutputStream());
            fromClient = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.printf("Failed to create Data streams to %s%n",
                    clientSocket.getInetAddress());
            //writeLn is a method that prints out to console
            writeLn("Error: " + e.getMessage());
            System.exit(1);
        }

        do {
            String request = null;
            try {
                //trying to get the request from client
                request = fromClient.readLine();
            } catch (IOException e) {
                writeLn("Connection dropped unexpectedly.");
                //if something went wrong we don't wish to proceed so killThread becomes true.
                stopThread();
            }
            if (request != null) {
                if (request.contains(":")) {
                    //if a request contains ':' it must be GET or SEND
                    String[] strings = request.split(":");
                    ///to check whether the request matches the pattern of e.g. GET:something
                    if (strings.length != 2) {
                        try {
                            writeLn(request);
                            //writeLnToClient is a method that sends data to client
                            writeLnToClient("");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //if a request does not match the pattern, maybe it was a mistake so we don't want to finish
                        // or interrupt, we just continue without doing anything.
                        continue;
                    }
                    switch (strings[0]) {
                        case "SEND": {
                            /* if the request from client is send, we want to increment the messageID
                              * then save the message to messageBoard
                              * write the request to console
                              * and let client know that the request was processed, by simply sending an empty String
                              */
                            messageID += 1;
                            messageBoard.SaveMessage(new MessageHeader(threadCharId, messageID), strings[1]);
                            writeLn("SEND:" + messageID + ":" + strings[1]);
                            try {
                                writeLnToClient("");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;
                        }
                        case "GET": {
                            /* if the request is GET
                              * server prints the request to the console
                              * spilt the 2nd part of the request (after ':')
                              * to get the actual identifier of the message
                              */
                            writeLn(request);
                            String[] msgId = strings[1].split("\\+");
                            if (msgId.length != 2) {
                                /* if the 2nd part of request does not have string of patter A+3
                                  *then there is no such message
                                  */
                                writeLn("ERR");
                                try {
                                    //TODO
                                    writeLnToClient("");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                /* we create a new message header according to the request
                                  * check if there exist a message with the same messageHeader
                                  */
                                MessageHeader messageHeader = new MessageHeader(msgId[0].charAt(0), Integer.parseInt
                                        (msgId[1]));
                                if (messageBoard.messages.containsKey(messageHeader)) {
                                    /* if so server prints to the console the message header + the actual message
                                      * followed by OK
                                      * then finally send the message data to the client
                                      */
                                    String returnMessage = messageBoard.GetMessage(messageHeader);
                                    writeLn(messageHeader.toString() + "=" + returnMessage);
                                    writeLn("OK");
                                    try {
                                        writeLnToClient(returnMessage);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    /* if not server prints to the console the message header along with null
                                      * followed by ERR
                                      * and tells client there there is no such message
                                      */
                                    writeLn(messageHeader.toString() + "=null");
                                    writeLn("ERR");
                                    try {
                                        writeLnToClient("No such message");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            /* if the request does not match none of the above case
                              * it is surely not what server expects, so server does nothing except
                              * send a empty String to client, so that client does not stuck on receiving info from server
                              */
                            writeLn(request);
                            try {
                                writeLnToClient("");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                } else {
                  // if the request does not contain ':' then it must be either LIST or BYE
                    switch (request) {
                        case "LIST": {
                            /* if the request is LIST
                              * server print the request to console
                              * creates a new Set of all the messages from messageBoard
                              */
                            writeLn(request);
                            Set<MessageHeader> messageHeaders = messageBoard.ListHeaders();
                            if (messageHeaders.size() > 0) {
                                /* if there is more than 0 message in the SET
                                  * server creates a new STRING based on the messages
                                  * and sends that STRING to the client
                                  */
                                StringBuilder stringBuilder = new StringBuilder("");
                                for (MessageHeader messageHeader : messageHeaders) {
                                    stringBuilder.append(messageHeader.toString()).append(";");
                                }
                                try {
                                    writeLnToClient(stringBuilder);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    /* if there is no message in the messageBoard (SET)
                                      * server just sends an empty String to the client
                                      */
                                    writeLnToClient("");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        case "BYE": {
                            // if the request is BYE, this particular MessageHandler is going to stop.
                            stopThread();
                            break;
                        }
                        default: {
                            /* if the request does not match none of the above case
                              * it is surely not what server expects, so server does nothing except
                              * send a empty String to client, so that client does not stuck on receiving info from server
                              */
                            writeLn(request);
                            try {
                                writeLnToClient("");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
            //keep looping unless the killThread is set to true.
        } while (!killThread);
    }

    private void writeLnToClient(Object o) throws IOException {
        //simply sends data to the client, making sure that the line is ending ('\n')
        toClient.writeBytes(o.toString() + "\n");
        toClient.flush();
    }

    private void writeLn(Object toPrint) {
        //it just prints to the console
        System.out.println(toPrint.toString());
    }

    private void stopThread() {
        /* when stopThread is called server prints BYE to the console
          * closes the connection with client
          * and sets the Decision making variable to true
          */
        writeLn("BYE");
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.killThread = true;
    }

    MessageServerHandler(MessageBoard b, Socket cl) {
        //basic setup for MessageHandler
        this.messageBoard = b;
        this.clientSocket = cl;
        this.threadCharId = MessageServer.threadCharId;
        MessageServer.threadCharId += 1;
    }
}