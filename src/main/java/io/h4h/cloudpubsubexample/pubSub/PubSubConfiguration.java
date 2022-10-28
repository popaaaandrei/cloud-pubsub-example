package io.h4h.cloudpubsubexample.pubSub;


import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;


/**
 * A message channel may follow either point-to-point or publish-subscribe semantics. With a point-to-point channel, no more than one consumer can receive each message sent to the channel. Publish-subscribe channels, on the other hand, attempt to broadcast each message to all subscribers on the channel. Spring Integration supports both of these models.
 * */
@Slf4j
@Configuration
public class PubSubConfiguration {



    // =================================================================
    // Receiving messages (Subscribe)
    // =================================================================

    public static final String inputMessageChannel = "inputMessageChannel";

    private static final String subscriptionName = "ehealth-events-in-testing-subscription-1";


    /**
     * An inbound channel adapter listens to messages from a Google Cloud Pub/Sub subscription and sends them to a Spring channel in an application. Create an inbound channel adapter to listen to the subscription and send messages to the channel.
     * */
    @Bean
    public MessageChannel inputMessageChannel() {
        // channel.setDatatypes(Number.class);
        // channel.setDatatypes(String.class, Number.class);
        return new PublishSubscribeChannel();
    }


    /**
     * A channel that broadcasts Messages to each of its subscribers. Create a PublishSubscribeChannel that will invoke the handlers in the message sender's thread.
     * */
    @Bean
    public PubSubInboundChannelAdapter inboundChannelAdapter(
        @Qualifier(inputMessageChannel) MessageChannel messageChannel,
        PubSubTemplate pubSubTemplate
    ) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
        adapter.setOutputChannel(messageChannel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }


    /**
     * Define what happens to the messages arriving in the message channel.
     * */
    @ServiceActivator(inputChannel = inputMessageChannel)
    public void messageReceiver(
        String payload,
        @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message
    ) {
        log.info("Pub/Sub Message arrived! MessageId: {}, Payload: {}",
            message.getPubsubMessage().getMessageId(),
            payload);

        message.ack();
    }


    // =================================================================
    // Sending (Publishing) messages
    // =================================================================

    public static final String outboundMessageChannel = "outputMessageChannel";

    private static final String publishTopicName = "ehealth-events-out-testing";


    /**
     * Create an outbound channel adapter to send messages from the input message channel to the topic. An outbound channel adapter listens to new messages from a Spring channel and publishes them to a Google Cloud Pub/Sub topic.
     * */
    @Bean
    @ServiceActivator(inputChannel = outboundMessageChannel)
    public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
        PubSubMessageHandler adapter = new PubSubMessageHandler(pubsubTemplate, publishTopicName);

        adapter.setSuccessCallback(((ackId, message) ->
            log.info("Message was sent via the outbound channel adapter to topic: {}, ack {}!", publishTopicName, ackId))
        );

        adapter.setFailureCallback((cause, message) ->
            log.info("Error sending " + message + " due to " + cause)
        );

        return adapter;
    }




}
