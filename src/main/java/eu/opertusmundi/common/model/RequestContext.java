package eu.opertusmundi.common.model;

import javax.annotation.Nullable;

import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.location.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestContext {

    private AccountDto account;

    private String ip;

    private Location location;

    public static RequestContext of(String ip, @Nullable AccountDto account, @Nullable Location location) {
        final RequestContext ctx = new RequestContext();
        ctx.setAccount(account);
        ctx.setIp(ip);
        ctx.setLocation(location);
        return ctx;
    }
}
