import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dani on 23/03/2017.
 */
class MessageBoard {

  // a threadSafe HashMap to store the messages.
  final ConcurrentHashMap<MessageHeader, String> messages;

  MessageBoard() {
    this.messages = new ConcurrentHashMap<>();
  }

  void SaveMessage(MessageHeader mh, String msg) {
    //if the key is already in use do nothing.
    if (!messages.containsKey(mh)) {
      //if not add a new message to the message board.
      this.messages.put(mh, msg);
    }
  }

  //returns the message, containing this particular messageHeader
  String GetMessage(MessageHeader mh) {
    return messages.get(mh);
  }

  // return a SET of all messages (MessageHeader Only) in the messageBoard
  Set<MessageHeader> ListHeaders() {
    final Set<MessageHeader> toReturn = new HashSet<>();
    messages.forEach((k, v) ->
            toReturn.add(k)
    );
    return toReturn;
  }
}
