package pingpong;

import static pingpong.MessageObj.Message.*;

public class Launcher {

    public static void main(String[] args) {

        Node node1 = new Node(1);
        Node node2 = new Node(2);

        node1.start(node2);

    }
}
