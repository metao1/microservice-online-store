package com.metao.book.checkout.presentation;

// import com.metao.book.checkout.domain.OrderEntity;
// import com.metao.book.checkout.exception.CartIsEmptyException;
// import
// com.metao.book.checkout.exception.NotEnoughProductsInStockException;
// import com.metao.book.checkout.exception.UserException;
// import com.metao.book.checkout.model.CheckoutStatus;
// import com.metao.book.checkout.domain.CheckoutService;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @Slf4j
// @RestController
// @RequestMapping("/checkout")
// @RequiredArgsConstructor
// public class CheckoutController {

// private final CheckoutService checkoutService;

// @PostMapping(produces = "application/json")
// public CheckoutStatus checkout() {
// String userId = "u1001";
// CheckoutStatus checkoutStatus = new CheckoutStatus();
// try {
// OrderEntity currentOrder = checkoutService.checkout(userId);
// if (currentOrder != null) {
// checkoutStatus.setOrderNumber(currentOrder.getId());
// checkoutStatus.setStatus(CheckoutStatus.SUCCESS);
// checkoutStatus.setOrderDetails(currentOrder.getOrderDetails());
// log.debug("Order is : " + currentOrder.getId() + " Details: " +
// currentOrder.getOrderDetails());
// } else {
// checkoutStatus.setOrderNumber("");
// checkoutStatus.setStatus(CheckoutStatus.FAILURE);
// checkoutStatus.setOrderDetails("Product is Out of Stock!");
// }
// } catch (NotEnoughProductsInStockException e) {
// checkoutStatus.setOrderNumber("");
// checkoutStatus.setStatus(CheckoutStatus.FAILURE);
// return checkoutStatus;
// } catch (CartIsEmptyException | UserException e) {
// e.printStackTrace();
// }
// return checkoutStatus;
// }
// }
