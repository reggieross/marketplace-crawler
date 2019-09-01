package domain;

import constants.Site;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Product {
    private Site site;
    private String name;
    private String brand;
    private String url;
    private Price price;
}
