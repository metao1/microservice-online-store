package com.metao.product.retails.exception;

import com.metao.product.retails.model.ProductApiErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RequestMapping(produces = "application/vnd.error+jsonhateoas")
public class ProductExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class})
    public ResponseEntity<ProductApiErrorDTO> productNotFoundException(final ProductNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ProductApiErrorDTO
                        .builder()
                        .message("Product not found:" + ex.getMessage())
                        .build());

    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ProductApiErrorDTO> productIllegalArgumentException(final IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ProductApiErrorDTO
                        .builder()
                        .message(ex.getMessage())
                        .build());

    }
}
