package eu.opertusmundi.common.model;

import java.util.UUID;

import org.springframework.lang.Nullable;

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

    public UUID getUserKey() {
        return account == null ? null : account.getKey();
    }

    public UUID getUserParentKey() {
        return account == null ? null : account.getParentKey() == null ? account.getKey() : account.getParentKey();
    }

    public boolean isAuthenticated() {
        return account != null;
    }

}
