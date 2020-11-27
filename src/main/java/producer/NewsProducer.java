package producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class NewsProducer {
    private static final String EXCHANGE_NAME = "news_exchange";
    private static final String HELP = "/help - print this help\n" +
            "/put <THEME> <TEXT> - publish article with theme and text\n" +
            "/exit - close app";

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
            String baseRoutingKey = "programming.";

            System.out.println(HELP);

            boolean isRun = true;
            while (isRun) {
                System.out.println("Wait command");
                String command = reader.readLine();
                String[] splitCommand = command.split("\\s+", 2);
                switch (splitCommand[0]) {
                    case "/help":
                        System.out.println(HELP);
                        break;
                    case "/put":
                        String[] putSplit = splitCommand[1].split("\\s+", 2);
                        String routingKey = baseRoutingKey+putSplit[0];
                        String text = putSplit[1];
                        channel.basicPublish(EXCHANGE_NAME, routingKey, null, String.format("%s %n theme %s", text, routingKey).getBytes(StandardCharsets.UTF_8));
                        System.out.println("[X] putting to exchange");
                        break;
                    case "/exit":
                        isRun = false;
                        break;
                }
            }
        }
    }
}
