package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import eu.opertusmundi.common.model.payment.BrowserInfoDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BrowserInfoEmbeddable implements Cloneable {

    @Column
    @EqualsAndHashCode.Include
    private String acceptHeader;

    @Column
    @EqualsAndHashCode.Include
    private int colorDepth;

    @Column
    @EqualsAndHashCode.Include
    private boolean javaEnabled;

    @Column
    @EqualsAndHashCode.Include
    private String language;

    @Column
    @EqualsAndHashCode.Include
    private int screenHeight;

    @Column
    @EqualsAndHashCode.Include
    private int screenWidth;

    @Column
    @EqualsAndHashCode.Include
    private int timeZoneOffset;

    @Column
    @EqualsAndHashCode.Include
    private String userAgent;


    @Override
    public BrowserInfoEmbeddable clone() {
        final BrowserInfoEmbeddable a = new BrowserInfoEmbeddable();

        a.acceptHeader   = this.acceptHeader;
        a.colorDepth     = this.colorDepth;
        a.javaEnabled    = this.javaEnabled;
        a.language       = this.language;
        a.screenHeight   = this.screenHeight;
        a.screenWidth    = this.screenWidth;
        a.timeZoneOffset = this.timeZoneOffset;
        a.userAgent      = this.userAgent;

        return a;
    }

    public BrowserInfoDto toDto() {
        final BrowserInfoDto a = new BrowserInfoDto();

        a.setAcceptHeader(acceptHeader);
        a.setColorDepth(colorDepth);
        a.setJavaEnabled(javaEnabled);
        a.setLanguage(language);
        a.setScreenHeight(screenHeight);
        a.setScreenWidth(screenWidth);
        a.setTimeZoneOffset(timeZoneOffset);
        a.setUserAgent(userAgent);

        return a;
    }

    public static BrowserInfoEmbeddable from(BrowserInfoDto c) {
        final BrowserInfoEmbeddable i = new BrowserInfoEmbeddable();

        i.setAcceptHeader(c.getAcceptHeader());
        i.setColorDepth(c.getColorDepth());
        i.setJavaEnabled(c.getJavaEnabled());
        i.setLanguage(c.getLanguage());
        i.setScreenHeight(c.getScreenHeight());
        i.setScreenWidth(c.getScreenWidth());
        i.setTimeZoneOffset(c.getTimeZoneOffset());
        i.setUserAgent(c.getUserAgent());

        return i;
    }

}
