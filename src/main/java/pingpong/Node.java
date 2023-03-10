package pingpong;

import com.rabbitmq.client.*;

import java.io.*;
import java.util.concurrent.TimeoutException;

import static pingpong.MessageObj.Message.*;

public class Node implements Communication_itf{

    private final int id; //unique and used as the name of the queue
    private boolean started = false;
    private final Channel channel;

    public Node(int id) {
        this.id = id;

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
            channel.basicPublish("", msg.getReceiver(),
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    byteMsg);
            System.out.println(this.id + " sent " + msg.getMsg());
        } catch (IOException e) {
            //todo: change?
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveMsg(MessageObj msg) {

        System.out.println(this.id + " received " + msg.getMsg());
        switch(msg.getMsg()) {
            //1st rule: start the PingPong
            case START -> {
                if (!started) {
                    this.started = true;
                    sendMsg(new MessageObj(msg.getSender(), msg.getReceiver(), PING));
                } else
                    sendMsg(new MessageObj(msg.getSender(), msg.getReceiver(), STARTED));
            }

            //2nd rule: receive a PING
            case PING -> sendMsg(new MessageObj(msg.getSender(), msg.getReceiver(), PONG));

            //3rd rule: receive a PONG
            case PONG -> sendMsg(new MessageObj(msg.getSender(), msg.getReceiver(), PING));

            case ERROR -> System.out.println("Request failed");
            case STARTED -> System.out.println("Node is already started");
            default -> sendMsg(new MessageObj(msg.getSender(), msg.getReceiver(), ERROR));
        }
    }
}



