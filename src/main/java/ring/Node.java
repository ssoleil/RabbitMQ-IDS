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

    public Node(int id, int nextId) {
        this.id = id;
        this.nextId = nextId;

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.channel.queueDeclare(String.valueOf(id), false, false, false, null);
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
            channel.basicConsume(String.valueOf(id), autoAck, deliverCallback, consumerTag -> { });
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
    public void receiveMsg(MessageObj msg) {

        System.out.println(this.id + " received leader " + msg.getLeader());

        if (msg.getMsg() == MessageObj.Message.ELECT) {
            if (this.id < msg.getLeader())
                sendMsg(new MessageObj(this.id, MessageObj.Message.ELECT));
            else if (this.id > msg.getLeader())
                sendMsg(new MessageObj(msg.getLeader(), MessageObj.Message.ELECT));
            else
                this.leader = true;
                //todo: broadcast and close all
        }
    }
}



