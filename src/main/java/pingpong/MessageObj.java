package pingpong;

import java.io.Serializable;

public class MessageObj implements Serializable {

    public enum Message {PING, PONG, ERROR}

    private int sender;
    private int id;
    private Message msg;

    public MessageObj(int id, Message msg) {
        this.id = id;
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public int getReceiver() {
        return id;
    }

    public int getSender() {
        return sender;
    }

    public Message getMsg() {
        return msg;
    }
}

