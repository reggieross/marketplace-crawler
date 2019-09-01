package processor;

import constants.Site;

public interface IPageProcessFactory {
    public IPageProcessor getPageProcessor(Site page) throws Exception;
}
