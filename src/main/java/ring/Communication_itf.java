package ring;

public interface Communication_itf {

    void sendMsg(MessageObj msg);
    void sendBroadcast(MessageObj msg);
    void receiveMsg(MessageObj msg);
}
