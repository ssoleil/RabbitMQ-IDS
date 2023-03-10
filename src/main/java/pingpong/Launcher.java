package pingpong;

public class Launcher {

    public static void main(String[] args) {

        Node node1 = new Node(1);
        Node node2 = new Node(2);
        node1.sendMsg(new MessageObj("2", "1", MessageObj.Message.START));
        //test if the second START fails
        node1.sendMsg(new MessageObj("2", "1", MessageObj.Message.START));
        //test if we can run 2 ping-pongs with different started node
        node2.sendMsg(new MessageObj("1", "2", MessageObj.Message.START));
        //test if the second START fails

        //todo: if we sent ping it will work as well without start
    }
}
