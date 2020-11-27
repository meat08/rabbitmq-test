package consumer;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SubscriberConsumer {
    private static final String EXCHANGE_NAME = "news_exchange";
    private static final String HELP = "/help - print this help\n" +
            "/subscribe <THEME> - subscribe to theme\n" +
            "/unsubscribe <THEME> - unsubscribe from theme\n" +
            "/exit - close app";

    interface Callback{
        void run();
    }

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String queueName = channel.queueDeclare().getQueue();

        String defaultRoutingKey = "#.java";
        channel.queueBind(queueName, EXCHANGE_NAME, defaultRoutingKey);

        listenCommand(channel, queueName, () -> System.exit(0));

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.printf("New article: %n%s", message);
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    private static void listenCommand(Channel channel, String queueName, Callback callback) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        new Thread(() -> {
            try {
                boolean isRun = true;
                System.out.println(HELP);
                while (isRun) {
                    String command = reader.readLine();
                    String[] splitCommand = command.split("\\s+", 2);
                    switch (splitCommand[0]) {
                        case "/subscribe":
                            channel.queueBind(queueName, EXCHANGE_NAME, "#."+splitCommand[1]);
                            System.out.println("You subscribed to theme " + splitCommand[1]);
                            break;
                        case "/unsubscribe":
                            channel.queueUnbind(queueName, EXCHANGE_NAME, "#."+splitCommand[1]);
                            System.out.println("You unsubscribed from theme " + splitCommand[1]);
                            break;
                        case "/exit":
                            isRun = false;
                            channel.queueDelete(queueName);
                            break;
                    }
                }
                callback.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
