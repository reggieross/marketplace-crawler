package service.impl;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import service.IEventService;
import com.amazonaws.services.sqs.AmazonSQS;
import domain.Event;
import tasks.ProcessProductsTasks;

@Component
public class SQSEventService implements IEventService {
    private AmazonSQS sqs;
    private String queueURL;
    private static final Logger logger = LoggerFactory.getLogger(ProcessProductsTasks.class);

    public SQSEventService(@Value("${sqs.marketplace.queuename}")String queueName) {
        this.sqs = AmazonSQSClientBuilder.standard()
            .withCredentials(new EnvironmentVariableCredentialsProvider())
            .withRegion(Regions.US_EAST_1)
            .build();
        this.queueURL = this.sqs.getQueueUrl(queueName).getQueueUrl();
        System.out.println(
            String.format(
                "initialized service with queueName: %s, and queueURL: %s",
                queueName,
                queueURL
            )
        );
    }

    public void publishEvent(Event event) {
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withQueueUrl(this.queueURL)
                .withMessageBody(event.payload)
                .withDelaySeconds(5);
        sqs.sendMessage(sendMsgRequest);
    }
}
