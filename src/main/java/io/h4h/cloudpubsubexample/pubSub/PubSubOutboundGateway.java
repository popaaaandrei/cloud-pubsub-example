package io.h4h.cloudpubsubexample.pubSub;

import org.springframework.integration.annotation.MessagingGateway;
import static io.h4h.cloudpubsubexample.pubSub.PubSubConfiguration.outboundMessageChannel;


@MessagingGateway(defaultRequestChannel = outboundMessageChannel)
public interface PubSubOutboundGateway {
    void sendToPubsub(String text);
}