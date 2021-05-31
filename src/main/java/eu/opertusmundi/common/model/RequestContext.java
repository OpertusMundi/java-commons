package eu.opertusmundi.common.model;

import javax.annotation.Nullable;

import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.location.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestContext {

    private AccountDto account;

    private String ip;

    private Location location;

    private boolean ignoreLogging;

    public static RequestContext of(String ip, @Nullable AccountDto account, @Nullable Location location, boolean ignoreLogging) {
        final RequestContext ctx = new RequestContext();
        ctx.setAccount(account);
        ctx.setIgnoreLogging(ignoreLogging);
        ctx.setIp(ip);
        ctx.setLocation(location);
        return ctx;
    }
}
