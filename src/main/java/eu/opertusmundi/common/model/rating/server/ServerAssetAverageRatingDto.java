package eu.opertusmundi.common.model.rating.server;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerAssetAverageRatingDto {

    private String pid;

    private BigDecimal value;

}
