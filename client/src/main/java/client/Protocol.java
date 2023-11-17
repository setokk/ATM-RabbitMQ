package client;

public class Protocol {
    public static final String REQUEST_QUEUE_NAME = "request_queue";
    public static final String REPLY_QUEUE_NAME = "reply_queue";
    public static final String REQUEST_EXCHANGE_NAME = "request_exchange";
    public static final String REPLY_EXCHANGE_NAME = "reply_exchange";

    // Codes
    public static final int WITHDRAW = 0;
    public static final int DEPOSIT = 1;
    public static final int BALANCE = 2;
}
