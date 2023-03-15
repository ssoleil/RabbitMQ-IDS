package pingpong;

import static pingpong.MessageObj.Message.*;

public class Launcher {

    public static void main(String[] args) {

        Node node1 = new Node(1);
        Node node2 = new Node(2);

        //without this set up nodes will not know
        //its partner to send a msg
        //this set up is NOT required for start() method
        node1.setPartner(String.valueOf(node2.getId()));
        node2.setPartner(String.valueOf(node1.getId()));

        //try to run each separate line, and
        //it will give the @Request failed@ output
//        node1.sendMsg(new MessageObj(node1.getId(), PING));
//        node1.sendMsg(new MessageObj(node1.getId(), PONG));
//
//        node2.sendMsg(new MessageObj(node1.getId(), PING));
//        node2.sendMsg(new MessageObj(node1.getId(), PONG));

        //working ping-pong version
        //node1.start(node2);

        //test if the second START from both nodes is impossible
//        node1.start(node2);
//        node2.start(node1);

        //we cant start ping-pong from the bigger node
        //according to the rules
        //node2.start(node1);

    }
}
