package com.metao.product.retails.exception;

import com.metao.product.models.ProductApiErrorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;

@ControllerAdvice
@RequestMapping(produces = "application/vnd.error+jsonhateoas")
public class ProductExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class})
    public ResponseEntity productNotFoundException(final ProductNotFoundException ex) {
        return Optional.ofNullable(ProductApiErrorDTO
                .builder()
                .message("Product not found:" + ex.getMessage())
                .build())
                .map(code -> ResponseEntity.notFound().build()).get();

    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity productIllegalArgumentException(final IllegalArgumentException ex) {
        return Optional.ofNullable(ProductApiErrorDTO
                .builder()
                .message(ex.getMessage())
                .build())
                .map(code -> ResponseEntity.badRequest().build()).get();

    }
}
