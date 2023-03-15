package pingpong;

import com.rabbitmq.client.*;

import java.io.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static pingpong.MessageObj.Message.*;

public class Node implements Communication_itf {

    private final int id; //unique and used as the name of the queue
    private boolean started = false;
    private final Connection connection;
    private final Channel channel;
    private String partner; //queue name for out

    public Node(int id) {
        this.id = id;

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            this.connection = factory.newConnection();
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

    public void setPartner(String partner) {
        this.partner = partner;
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

    public synchronized void start(Node node) {

        //1st rule: start the ping-pong
        if (!this.started) {
            if (node.isReady(this)) {
                this.started = true;
                this.setPartner(String.valueOf(node.getId()));
                sendMsg(new MessageObj(this.id, PING));
            } else
                System.out.println(node.getId() + " is already started");
        } else
            System.out.println(this.getId() + " is already started");
    }

    private boolean isReady(Node node) {
        if (!this.started) {
            this.setPartner(String.valueOf(node.getId()));
            this.started = true;
            return true;
        } else
            return false;
    }

    @Override
    public void sendMsg(MessageObj msg) {

        byte[] byteMsg = getByteArray(msg);

        try {
            channel.basicPublish("", this.partner,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    byteMsg);
            System.out.println(this.id + " sent " + msg.getMsg());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveMsg(MessageObj msg) {

        System.out.println(this.id + " received " + msg.getMsg());
        switch(msg.getMsg()) {

            //2nd rule: receive a PING
            case PING -> {
                //we need to check if the node is started here
                //to prevent the usage of sendMsg(PING) from the Launcher
                if (this.started && msg.getId() < this.id)
                    sendMsg(new MessageObj(this.id, PONG));
                else
                    sendMsg(new MessageObj(msg.getId(), ERROR));
            }

            //3rd rule: receive a PONG
            case PONG -> {
                //we need to check if the node is started here
                //to prevent the usage of sendMsg(PONG) from the Launcher
                if (this.started && msg.getId() > this.id)
                    sendMsg(new MessageObj(this.id, PING));
                else
                    sendMsg(new MessageObj(msg.getId(), ERROR));
            }

            case ERROR -> {
                System.out.println("Request failed");
                try {
                    this.channel.close();
                    this.connection.close();
                } catch (IOException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
                System.exit(-1);
            }
            default -> sendMsg(new MessageObj(msg.getId(), ERROR));
        }
    }
}



