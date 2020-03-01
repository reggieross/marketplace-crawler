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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SSensePageProcessor implements IPageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SSensePageProcessor.class);
    private String URI = "https://www.ssense.com/en-us/men";

    public List<Product> process() {
        return this.getProducts(URI);
    }

    public Site getSite() {
        return Site.SSENSE;
    }

    private List<Product> getProducts(String URL) {
        List<Product> products = new ArrayList<>();
        try {
            Document document = Jsoup.connect(URL).get();
            int totalPages = getTotalPageCount(document);
            System.out.println("Total pages: "+ totalPages);
            int index = 1;
            while (index <= totalPages) {
                products.addAll(this.getProductsForPage(URL, index));
                index++;
            }
        }  catch (Exception e) {
            logger.error("For page'" + URL + "': ");
            e.printStackTrace();
        }

        logger.info(String.format("%s products processed from SSense", products.size()));

        return products;
    }

    private List<Product> getProductsForPage(String URL, Integer pageNumber) {
        List<Product> products = new ArrayList<>();
        try {
            Document document = pageNumber > 1
                ? Jsoup.connect(URL+"?page=" + pageNumber).get()
                : Jsoup.connect(URL).get();

            Elements productsOnPage = document.select("figure");
            for (Element page : productsOnPage) {
                products.add(getProductForHTML(page));
            }
        } catch (IOException e) {
            logger.error("For '" + URL + "': " );
            e.printStackTrace();
        }

        return products.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Product getProductForHTML(Element productHTML) {
        try {
            return Product.builder()
                .name(getNameFromHTML(productHTML))
                .brand(getBrandFromHTML(productHTML))
                .uri(getURIFromHTML(productHTML))
                .imageURI(getImageURIFromHTML(productHTML))
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

    private int getTotalPageCount(Document doc) {
        Elements lastPage = doc.select("nav[aria-label='Pagination'] li:nth-last-of-type(2)");
        return lastPage.size() > 0
            ? Integer.parseInt(lastPage.first().text())
            : 1;
    }

    private Price getPriceFromHTML(Element productHTML) {
        Elements amount = productHTML.select("span.price");
        Elements currency = productHTML.select("meta[itemprop='currency']");
        return Price.builder()
            .site(Site.SSENSE.toString())
            .amount(amount.size() == 0 ? null : amount.first().text())
            .currency(currency.size() == 0 ? null : currency.first().text())
            .build();
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

    private String getURIFromHTML(Element productHTML) {
        Elements url = productHTML.select("a");
        return url.size() == 0
                ? null
                : url.first().attr("abs:href");
    }

    private String getImageURIFromHTML(Element productHTML) {
        Elements metaTag = productHTML.select("meta[itemprop='image']");
        return metaTag.size() == 0
                ? null
                : metaTag.first().attr("abs:content");
    }
}
