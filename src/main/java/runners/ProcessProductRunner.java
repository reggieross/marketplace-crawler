package runners;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Event;
import domain.Product;
import domain.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processor.IPageProcessor;
import service.IEventService;

import java.util.List;

public class ProcessProductRunner implements Runnable {
    private final IPageProcessor pageProcessor;
    private final IEventService sqsEventService;
    private static final Logger logger = LoggerFactory.getLogger(ProcessProductRunner.class);


    public ProcessProductRunner(
        IPageProcessor pageProcessor,
        IEventService sqsEventService
    ) {
        this.pageProcessor = pageProcessor;
        this.sqsEventService = sqsEventService;
    }

    @Override
    public void run() {
        logger.info( String.format("====== Processing products for %s ======", this.pageProcessor.getSite()));
        List<Product> productList = pageProcessor.process();

        logger.info(String.format("%s products found on %s", productList.size(), this.pageProcessor.getSite()));
        for (Product product: productList) {
            boolean isValid = this.validateProduct(product);
            if (isValid) {
                this.submitProductsToQueue(product);
            }
        }

        logger.info(String.format("====== Successfully submitted products for %s to queue ======", this.pageProcessor.getSite()));
    }

    private void submitProductsToQueue(Product product) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductResponse response = ProductResponse.builder().product(product).build();
        String responseString = "";
        try {
            responseString = objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("We were unable to serialize the product list");
        }

        this.sqsEventService.publishEvent(
            Event.builder()
                .payload(responseString)
                .build()
        );
    }

    private boolean validateProduct(Product product) {
        return product.getName() != null && product.getPrice() != null;
    }
}
