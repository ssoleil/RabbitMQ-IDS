package ring;

import java.util.*;

public class Launcher {

    private static final int NUM_NODES = 30;
    static Integer[] nodeIds;
    static List<Node> ring = new ArrayList<>(NUM_NODES);

    public static void main(String[] args) {

        nodeIds = generateIds();

        //check guarantee all ids are unique
        //checkIds();

        //building the ring topology
        int i;
        for (i = 0; i < NUM_NODES - 1; i++) {
            ring.add(i, new Node(nodeIds[i], nodeIds[i+1]));
        }
        ring.add(i, new Node(nodeIds[i], nodeIds[0]));

        //check the topology
        checkTopology();

        //start the election
        Node starter = ring.get(NUM_NODES / 2);
        starter.sendMsg(new MessageObj(starter.getId(), MessageObj.Message.ELECT));

    }

    private static void checkTopology() {
        System.out.println("TOPOLOGY");
        for (Node n: ring) {
            System.out.print(n.getId() + " -> ");
        }
        System.out.println();
    }

    private static void checkIds() {

        System.out.print("Unique ids: ");
        for (int i = 0; i < nodeIds.length; i++) {
            if (i % 10 == 0)
                System.out.println();
            System.out.print(nodeIds[i] + " ");
        }
        System.out.println();
    }

    private static Integer[] generateIds() {
        Set<Integer> nodeIds = new HashSet<>();
        Random r = new Random();
        for (int i = 0; i < NUM_NODES; i++)
            while (!nodeIds.add(r.nextInt(-100, 100)));
        return nodeIds.toArray(new Integer[NUM_NODES]);
    }
}
