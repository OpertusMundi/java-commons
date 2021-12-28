package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
@JsonIgnoreType
public class UserBlockedStatusCommand {

    final private UUID userKey;

    final private BlockStatusDto consumer;

    final private BlockStatusDto provider;

}