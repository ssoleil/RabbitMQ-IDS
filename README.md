# RabbitMQ
##### Polina Rohoza, UGA - MOSIG M1, Grenoble
## 
This repository represents the RabbitMQ case study and implements several distributed algorithms such as Ping Pong and Election on a Ring. In addition, RabbitMQ samples were discovered during the process so they are also presented here.

## How to run

The main IDE for this project implementation and testing is **IntelliJ Idea**. Moreover, **12 Oracle OpenJDK version 19.0.1** was used to write, build and run the program. 

**Before running the code it's necessary to start RabbitMQ Service.** It's possible to run ```rabbitmq-service.bat``` on Windows.

**For Election on a Ring it's recommended to restart the RabbitMQ Service** (stop and run again) to prevent the usage of the created queues and, hence, errors.

## Ping Pong

The main idea of this distributed algorithm is to provide endless communication between two nodes. After the start of two nodes, one of them sends a PING message and another one receives it and sends PONG to reply. There are several implemented rules:

1. We can **start the nodes** by sending them a start signal. If the node is not started yet we try to start the partner node. If we did it successfully, we send the PING message from the first node to the second one.
2. If the node **received the PING** message with id, it should check whether this id is less than the node's id. If it's the case, the node sends PONG signal to the received id.
3. If the node **received the PONG** message with id, it should check whether this id is bigger than the node's id. If it's the case, the node sends PING signal to the received id.

Different scenarios of the game are presented below:

![Ping-pong scenarious](/assets/scenarious.jpg "Ping-pong scenarious")

The **start** function is implemented as **synchronized** one which allows us to be sure only one thread will run it at one moment of time. In addition to the standard type of messages (PING and PONG) we have an ERROR message if we want to inform the node its request is failed.

The flow implementation looks like this:

![Ping-pong flow](/assets/pingpong.jpg "Ping-pong flow")

## Election on a Ring
The purpose of this algorithm is to choose one leader among the nodes that are connected by the ring topology. All nodes have randomly assigned ids, so the leader is the one with the smallest id. All nodes also know the destination point of the message they want to send because the ring works like a one-directional FIFO. 

The rules for the election are:
1. If the **node receives the start signal**, it sends the <ELECT, this node's id> message with to the next node.
2. If the **node receives <ELECT, id>** message and 
    - the id is less than the current node id, the receiver should forward the <ELECT, id> message to the next node.
    - the id is bigger than the current node id, the receiver should forward the <ELECT, this node's id> message to the next node.
    - the id is equal to the current node id, we set up the leader = true and send a broadcast.

The election flow looks like the following: 

![Ring](/assets/ring.jpg "Ring")

In the current implementation, **the leader node sends the broadcast** for everyone else and each node in the ring will be notified and will terminate itself to finish the election. So the additional rule is if the node receives the LEADER message it will print the short notification and terminate its work.