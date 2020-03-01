package processor.impl;

import constants.Site;
import org.springframework.stereotype.Component;
import processor.IPageProcessFactory;
import processor.IPageProcessor;

@Component
public class PageProcessorFactory implements IPageProcessFactory {

    public IPageProcessor getPageProcessor(Site page) throws Exception {
        switch (page) {
//            case SSENSE:
//                return new SSensePageProcessor();
            case HAVEN:
                return new HavenPageProcessor();
        }

        throw new Exception("The page you want has not been implemented yet");
    }
}
