package tasks;

import constants.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import processor.IPageProcessor;
import runners.ProcessProductRunner;
import service.IEventService;
import org.springframework.scheduling.annotation.Scheduled;
import processor.IPageProcessFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ExecutorService executor = Executors.newFixedThreadPool(30);
        for (Site site: Site.values() ){
            try {
                IPageProcessor pageProcessor = pageProcessFactory.getPageProcessor(site);
                Runnable worker = new ProcessProductRunner(pageProcessor, this.sqsEventService);
                executor.execute(worker);
            } catch (Exception e){
                logger.debug(
                    String.format(
                        "The page processor for %s not been implemented yet",
                        site
                    )
                );
            }
        }
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {
        }

        System.out.println("\nFinished processing all sites");
    }
}
