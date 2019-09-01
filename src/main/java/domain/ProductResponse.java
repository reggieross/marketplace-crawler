package domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ProductResponse {
    public List<Product> products;
}
