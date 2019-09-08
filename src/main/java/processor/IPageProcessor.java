package processor;

import constants.Site;
import domain.Product;
import java.util.List;

public interface IPageProcessor {
    public List<Product> process();
    public Site getSite();
}
