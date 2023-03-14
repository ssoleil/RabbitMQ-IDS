package ring;

import com.rabbitmq.client.*;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static pingpong.MessageObj.Message.*;

public class Node implements Communication_itf {

    private final int id; //unique and used as the name of the queue
    private int nextId; //out; unique and used to define the next node in the fifo ring
    private boolean leader = false;
    private final Channel channel; //in

    private final Connection connection; //to open and close separately

    private static final String LEADER_EXCHANGE = "who_is_leader";

    public Node(int id, int nextId) {
        this.id = id;
        this.nextId = nextId;

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.channel.queueDeclare(String.valueOf(id), false, false, false, null);

            //set up broadcast
            channel.exchangeDeclare(LEADER_EXCHANGE, "fanout");
            channel.queueBind(String.valueOf(id), LEADER_EXCHANGE, "");

            processMsg();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public int getId() {
        return id;
    }

    public void processMsg() {

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            byte[] byteMsg = delivery.getBody();
            MessageObj message;
            try {
                message = (MessageObj) parseMsg(byteMsg);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            receiveMsg(message);
        };
        boolean autoAck = true; // acknowledgment is covered below
        try {
            channel.basicConsume(String.valueOf(id), autoAck, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            //todo: change?
            throw new RuntimeException(e);
        }
    }

    private Object parseMsg(byte[] byteMsg) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(byteMsg);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ois.readObject();
    }

    private byte[] getByteArray(MessageObj msg) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    @Override
    public void sendMsg(MessageObj msg) {

        byte[] byteMsg = getByteArray(msg);

        try {
            channel.basicPublish("", String.valueOf(nextId),
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    byteMsg);
            System.out.println(this.id + " sent leader " + msg.getLeader());
        } catch (IOException e) {
            //todo: change?
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendBroadcast(MessageObj msg) {
        byte[] byteMsg = getByteArray(msg);

        try {
            channel.basicPublish(LEADER_EXCHANGE, "", null, byteMsg);
            System.out.println(this.id + " broadcast the final leader " + msg.getLeader());
        } catch (IOException e) {
            //todo: change?
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveMsg(MessageObj msg) {

        switch (msg.getMsg()) {
            case ELECT: {
                System.out.println(this.id + " received leader " + msg.getLeader());

                if (this.id < msg.getLeader())
                    sendMsg(new MessageObj(this.id, MessageObj.Message.ELECT));
                else if (this.id > msg.getLeader())
                    sendMsg(new MessageObj(msg.getLeader(), MessageObj.Message.ELECT));
                else if (this.id == msg.getLeader()) {
                    this.leader = true;
                    sendBroadcast(new MessageObj(this.id, MessageObj.Message.LEADER));
                    //todo: broadcast and close all
                }
                break;
            }
            case LEADER: {
                //if we receive the leader, print and terminate
                System.out.println(this.id + " knows leader is " + msg.getLeader());
                try {
                    this.channel.close();
                    this.connection.close();
                } catch (IOException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            default:
                System.out.println(this.id + ": Unknown message");
        }
    }

}

