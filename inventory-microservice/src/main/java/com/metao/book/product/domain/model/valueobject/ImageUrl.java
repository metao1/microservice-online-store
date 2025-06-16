package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import java.util.regex.Pattern;
import lombok.NonNull;

/**
 * Product image URL value object
 */
public record ImageUrl(String value) implements ValueObject {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "(http(s?):)([/|.\\w])*\\.(?:jpg|gif|png)",
        Pattern.CASE_INSENSITIVE
    );

    public ImageUrl(@NonNull String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("Image URL cannot exceed 255 characters");
        }
        if (!URL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid image URL format. Must be http(s) and end with jpg, gif, or png");
        }
        this.value = value.trim();
    }

    public static ImageUrl of(String value) {
        return new ImageUrl(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
