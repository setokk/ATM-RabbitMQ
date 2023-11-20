# ATM-RabbitMQ
ATM-RabbitMQ for Distributed Systems course, University of Macedonia (UoM).

### What is it
This is a project demonstrating the usage of RabbitMQ in client-server applications.

### How it works
It uses 2 queues to achieve this client-server functionality:
- request_queue (client=producer, server=consumer)
- reply_queue (client=consumer, server=producer)

Both queues are binded to their corresponding **exchanges** (binding keys):
- request_exchange (used by the client to forward messages to the request_queue).
- reply_exchange (used by the server to forward messages to the reply_queue).

Both exchanges are of <b><i>DIRECT</i></b> type. That means that for a message to be received by a consumer,
the binding key of the consumer must be equal to the routing key of the message.

### How does the server know where to send the reply
The server does not know (RabbitMQ broker is responsible), but on top of the request body (userId, code, amount) it also receives the ip address of the client.
<br>
Therefore request body looks like this: (userId, code, amount, ipAddress).
<br>
The server then sets the client's ip address as a routing key when sending the reply message (the client has binded the request queue with their ip address).
<br>
This way, RabbitMQ knows where to route the message and the clients receive their corresponding replies.

### How to run
1. ```sudo docker-compose up -d --build```
2. ```sudo docker start -a -i rabbitmq-atm-client-1```
