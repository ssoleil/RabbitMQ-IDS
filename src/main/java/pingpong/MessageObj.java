package pingpong;

import java.io.Serializable;

public class MessageObj implements Serializable {

    public enum Message {PING , PONG, START, ERROR, STARTED}

    private String receiver;
    private String sender;

    private Message msg;

    public MessageObj(String receiver, String sender, Message msg) {
        this.receiver = receiver;
        this.sender = sender;
        this.msg = msg;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSender() {
        return sender;
    }

    public Message getMsg() {
        return msg;
    }
}

