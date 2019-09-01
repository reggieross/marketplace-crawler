package tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Site;
import domain.Event;
import domain.Product;
import domain.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import processor.IPageProcessor;
import service.IEventService;
import org.springframework.scheduling.annotation.Scheduled;
import processor.IPageProcessFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessProductsTasks {

    private static final Logger logger = LoggerFactory.getLogger(ProcessProductsTasks.class);
    private IEventService sqsEventService;
    private IPageProcessFactory pageProcessFactory;

    public ProcessProductsTasks(
        @Autowired IEventService SQSEventService,
        @Autowired IPageProcessFactory pageProcessFactory
    ) {
        this.sqsEventService = SQSEventService;
        this.pageProcessFactory = pageProcessFactory;
    }

    @Scheduled(fixedRate = 86400000)
    public void processNewProducts() {
        List<Product> productResponses = Arrays.stream(
            Site.values()
        ).map(siteEnum -> {
            try {
                IPageProcessor pageProcessor = pageProcessFactory.getPageProcessor(siteEnum);
                return pageProcessor.process();
            } catch (Exception e){
                logger.debug(
                    String.format(
                        "The %s you were trying to use has not been implemented yet",
                        siteEnum
                    )
                );
                return new ArrayList<Product>();
            }
        }).flatMap(Collection::stream)
        .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        ProductResponse response = ProductResponse.builder().products(productResponses).build();
        String responseString = "";
        try {
            responseString = objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("We were unable to serialize the product list");
        }

        this.sqsEventService.publishEvent(
                Event.builder()
                    .payload(responseString).build()
        );
    }
}
