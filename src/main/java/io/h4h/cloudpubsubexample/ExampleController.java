package io.h4h.cloudpubsubexample;


import io.h4h.cloudpubsubexample.pubSub.PubSubOutboundGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class ExampleController {


    @Autowired
    private PubSubOutboundGateway messagingGateway;


    /**
     * curl -X POST --data 'AAAAA' localhost:8080/publish
     * */
    @PostMapping("/publish")
    public ResponseEntity<String> publishMessage(@RequestBody String body) {
        System.out.println("Sending " + body);
        messagingGateway.sendToPubsub(body);
        return ResponseEntity.ok("published");
    }


}
