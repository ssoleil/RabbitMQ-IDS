package ring;

import java.io.Serializable;

public class MessageObj implements Serializable {

    public enum Message {ELECT}

    private int leader;

    private Message msg;

    public MessageObj(int leader, Message msg) {
        this.leader = leader;
        this.msg = msg;
    }

    public int getLeader() {
        return leader;
    }


    public Message getMsg() {
        return msg;
    }
}

