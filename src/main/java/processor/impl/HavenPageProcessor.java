package processor.impl;

import constants.Site;
import domain.Price;
import domain.Product;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processor.IPageProcessor;

import java.util.ArrayList;
import java.util.List;

public class HavenPageProcessor implements IPageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HavenPageProcessor.class);
    private String URI = "https://shop.havenshop.com/collections/new-arrivals?view=all";

    @Override
    public List<Product> process() {
        return this.getProducts(URI);
    }

    @Override
    public Site getSite() {
        return Site.HAVEN;
    }

    private List<Product> getProducts(String URL) {
        List<Product> products = new ArrayList<>();
        try {
            Connection conn = Jsoup.connect(URL);
            conn.timeout(10 * 1000);
            Document document = conn.get();
            Elements productsOnPage = document.select("a.product-card");
            for (Element page : productsOnPage) {
                products.add(getProductForHTML(page));
            }
        } catch (Exception e) {
            logger.error("For page'" + URL + "': ");
            e.printStackTrace();
        }

        return products;
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

    private Price getPriceFromHTML(Element productHTML) {
        Elements amount = productHTML.select("p.product-price");
        return Price.builder()
                .site(Site.HAVEN.toString())
                .amount(amount.size() == 0 ? null : amount.first().text())
                .currency("cad")
                .build();
    }

    private String getNameFromHTML(Element productHTML) {
        Elements name = productHTML.select("p.product-card-name");
        return name.size() == 0
            ? null
            : name.first().text();
    }

    private String getBrandFromHTML(Element productHTML) {
        Elements brand = productHTML.select("p.product-card-brand");
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
        Elements metaTag = productHTML.select(".product-image-wrapper img");
        return metaTag.size() == 0
                ? null
                : metaTag.first().attr("abs:src");
    }
}
