package eu.opertusmundi.common.model.payment;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.entities.subentities.BrowserInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Browser information required by 3DS2 integration
 */
@NoArgsConstructor
@Getter
@Setter
public class BrowserInfoDto {

    @Schema(description = "Exact content of the HTTP accept headers as sent to the merchant from the shopper’s browser")
    @JsonIgnore
    private String acceptHeader;

    @NotNull
    @Schema(description = "Value representing the bit depth of the colour palette for displaying images, in bits per pixel")
    private Integer colorDepth;

    @NotNull
    @Schema(description = "Whether the user browser has Java enabled")
    private Boolean javaEnabled;

    @NotEmpty
    @Schema(description = "Language of the browser of the user")
    private String language;

    @NotNull
    @Schema(description = "The height of the screen in pixels")
    private Integer screenHeight;

    @NotNull
    @Schema(description = "The width of the screen in pixels")
    private Integer screenWidth;

    @NotNull
    @Schema(description = " UTC time offset in minutes")
    private Integer timeZoneOffset;

    @NotEmpty
    @Schema(description = "Exact content of the HTTP user-agent header")
    private String userAgent;

    public BrowserInfo toMangoPayBrowserInfo() {
        final BrowserInfo i = new BrowserInfo();

        i.setAcceptHeader(acceptHeader);
        i.setColorDepth(colorDepth);
        i.setJavaEnabled(javaEnabled);
        i.setJavascriptEnabled(true);
        i.setLanguage(language);
        i.setScreenHeight(screenHeight);
        i.setScreenWidth(screenWidth);
        i.setTimeZoneOffset(Integer.toString(timeZoneOffset));
        i.setUserAgent(userAgent);

        return i;
    }

}
