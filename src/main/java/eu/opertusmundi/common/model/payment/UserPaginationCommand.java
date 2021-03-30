package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@JsonIgnoreType
public class UserPaginationCommand {

    private UUID userKey;

    private int page;

    private int size;

}
