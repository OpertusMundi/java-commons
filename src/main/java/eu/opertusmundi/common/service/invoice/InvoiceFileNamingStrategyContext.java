package eu.opertusmundi.common.service.invoice;

import eu.opertusmundi.common.model.file.FileNamingStrategyContext;
import io.jsonwebtoken.lang.Assert;
import lombok.Getter;

@Getter
public class InvoiceFileNamingStrategyContext extends FileNamingStrategyContext {

    protected InvoiceFileNamingStrategyContext(Integer userId, String payInReferenceNumber) {
        super(true);

        this.userId    = userId;
        this.payInReferenceNumber  = payInReferenceNumber;
    }

    private final Integer userId;
    private final String  payInReferenceNumber;


    public static InvoiceFileNamingStrategyContext of(Integer userId, String payInReferenceNumber) {
        Assert.notNull(userId, "Expected a non-null user identifier");
        Assert.notNull(payInReferenceNumber, "Expected a non-null pay in reference number");

        return new InvoiceFileNamingStrategyContext(userId, payInReferenceNumber);
    }

}
