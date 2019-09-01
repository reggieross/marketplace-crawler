package processor.impl;

import constants.Site;
import domain.Price;
import domain.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processor.IPageProcessor;
import tasks.ProcessProductsTasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SSensePageProcessor implements IPageProcessor {
    private HashSet<String> links;
    private static final Logger logger = LoggerFactory.getLogger(SSensePageProcessor.class);
    public List<Product> process() {
        return this.getProducts("https://www.ssense.com/en-us/men");
    }

    private List<Product> getProducts(String URL) {
        List<Product> products = new ArrayList<>();
        try {
            Document document = Jsoup.connect(URL).get();
            Elements productsOnPage = document.select("figure");
            for (Element page : productsOnPage) {
                products.add(getProductForHTML(page));
            }
        } catch (IOException e) {
            logger.error("For '" + URL + "': " + e.getMessage());
        }

        logger.info(String.format("%s products processed from SSense", products.size()));

        return products.stream()
            .filter(product -> product != null)
            .collect(Collectors.toList());
    }

    private Product getProductForHTML(Element productHTML) {
        try {
            return Product.builder()
                .site(Site.SSENSE)
                .name(getNameFromHTML(productHTML))
                .brand(getBrandFromHTML(productHTML))
                .url(getURLFromHTML(productHTML))
                .price(getPriceFromHTML(productHTML))
                .build();
        } catch (Exception e) {
            logger.error(
                    String.format("Error from element you may need to check the structure of the web page. Element: %s",
                            productHTML.html()
                    )
            );
        }

        return null;
    }

    private Price getPriceFromHTML(Element productHTML) {
        Elements amount = productHTML.select("p .price");
        Elements currency = productHTML.select("meta[itemprop='currency']");
        return Price.builder()
            .amount(amount.size() == 0 ? null : amount.first().text())
            .amount(currency.size() == 0 ? null : amount.first().text()).build();
    }

    private String getNameFromHTML(Element productHTML) {
        Elements name = productHTML.select("p[itemprop='name']");
        return name.size() == 0
            ? null
            : name.first().text();
    }

    private String getBrandFromHTML(Element productHTML) {
        Elements brand = productHTML.select("p[itemprop='brand']");
        return brand.size() == 0
                ? null
                : brand.first().text();
    }

    private String getURLFromHTML(Element productHTML) {
        Elements brand = productHTML.select("a");
        return brand.size() == 0
                ? null
                : brand.first().attr("abs:href");
    }
}
