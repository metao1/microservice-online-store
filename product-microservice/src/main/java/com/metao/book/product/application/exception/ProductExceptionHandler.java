package com.metao.book.product.application.exception;

/*
 * @ControllerAdvice
 *
 * @RequestMapping(produces = "application/vnd.error+jsonhateoas")
 * public class ProductExceptionHandler extends ResponseEntityResultHandler {
 *
 * @ExceptionHandler({ProductNotFoundException.class})
 * public ResponseEntity productNotFoundException(final ProductNotFoundException
 * ex) {
 * return Optional.ofNullable(ProductApiErrorDTO
 * .builder()
 * .message("Product not found:" + ex.getMessage())
 * .build())
 * .map(code -> ResponseEntity.notFound().build()).get();
 *
 * }
 *
 * @ExceptionHandler({IllegalArgumentException.class})
 * public ResponseEntity productIllegalArgumentException(final
 * IllegalArgumentException ex) {
 * return Optional.ofNullable(ProductApiErrorDTO
 * .builder()
 * .message(ex.getMessage())
 * .build())
 * .map(code -> ResponseEntity.badRequest().build()).get();
 *
 * }
 * }
 */
