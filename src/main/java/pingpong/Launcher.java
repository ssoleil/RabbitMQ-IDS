package pingpong;

public class Launcher {

    public static void main(String[] args) {

        Node node1 = new Node(1);
        Node node2 = new Node(2);
        node1.sendMsg(new MessageObj("2", "1", MessageObj.Message.PING));
    }
}
