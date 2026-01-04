package com.metao.book.product.domain.model.valueobject;

import com.metao.book.shared.domain.base.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Product image URL value object
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ImageUrl implements ValueObject {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "(http(s?):)([/|.\\w])*\\.(?:jpg|gif|png)",
        Pattern.CASE_INSENSITIVE
    );

    private final String value;

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
    @NotNull
    public String toString() {
        return value;
    }
}