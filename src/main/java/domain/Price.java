package domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Price {
    private String currency;
    private String amount;
}
