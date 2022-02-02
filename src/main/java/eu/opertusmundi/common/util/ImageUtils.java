package eu.opertusmundi.common.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ImageUtils {

    public static final int DEFAULT_SIZE = 192;

    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    public byte[] resizeImage(@Nullable byte[] image, @Nullable String mimeType) {
        return this.resizeImage(image, mimeType, DEFAULT_SIZE);
    }

    public byte[] resizeImage(@Nullable byte[] image, @Nullable String mimeType, int size) {
        if (image == null || StringUtils.isBlank(mimeType)) {
            return image;
        }
        final String format = mimeType.split("/").length == 1 ? mimeType : mimeType.split("/")[1];

        try (
            final InputStream           in  = new ByteArrayInputStream(image);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            final BufferedImage buf    = ImageIO.read(in);
            int                 width  = buf.getWidth();
            int                 height = buf.getHeight();

            if (width >= height && width > size) {
                height = height * size / width;
                width  = size;
            } else if (height > size) {
                width  = width * size / height;
                height = size;
            } else {
                return image;
            }

            Thumbnails.of(buf).size(width, height).outputFormat(format).toOutputStream(out);

            return out.toByteArray();
        } catch (final IOException ex) {
            logger.error("Failed to resize image [message={}]", ex.getMessage());
        }

        return image;
    }

}
