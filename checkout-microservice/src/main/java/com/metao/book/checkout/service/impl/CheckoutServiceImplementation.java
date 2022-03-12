package com.metao.book.checkout.service.impl;

// import com.metao.book.checkout.clients.ProductCatalogRestClient;
// import com.metao.book.checkout.clients.ShoppingCartRestClient;
// import com.metao.book.checkout.domain.OrderEntity;
// import com.metao.book.checkout.domain.ProductInventoryEntity;
// import com.metao.book.checkout.exception.CartIsEmptyException;
// import
// com.metao.book.checkout.exception.NotEnoughProductsInStockException;
// import com.metao.book.checkout.exception.UserException;
// import com.metao.book.checkout.repository.ProductInventoryRepository;
// import com.metao.book.checkout.service.CheckoutService;
// import com.metao.book.retail.models.ProductDTO;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.logging.log4j.util.Strings;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.PlatformTransactionManager;
// import org.springframework.transaction.TransactionDefinition;
// import org.springframework.transaction.TransactionStatus;
// import org.springframework.transaction.annotation.Transactional;
// import
// org.springframework.transaction.support.TransactionCallbackWithoutResult;
// import org.springframework.transaction.support.TransactionTemplate;

// import javax.persistence.EntityManager;
// import javax.validation.constraints.NotNull;
// import java.util.Map;

// @Slf4j
// @Service
// @Transactional
// @RequiredArgsConstructor(onConstructor = @__(@Autowired))
// public class CheckoutServiceImplementation implements CheckoutService {

// private final ShoppingCartRestClient shoppingCartRestClient;
// private final ProductCatalogRestClient productCatalogRestClient;
// private final ProductInventoryRepository productInventoryRepository;
// private final PriceCalculatorService priceCalculatorService;
// private TransactionTemplate transactionTemplate;
// private final EntityManager entityManager;

// @Autowired
// public void setTransactionManager(PlatformTransactionManager
// transactionManager) {
// transactionTemplate = new TransactionTemplate(transactionManager);
// }

// public OrderEntity checkout(final String userId) throws
// NotEnoughProductsInStockException, CartIsEmptyException, UserException {
// if (Strings.isEmpty(userId)) throw new UserException("userId can't be null or
// empty");
// Map<String, Integer> products =
// shoppingCartRestClient.getProductsInCart(userId);
// log.debug("*** In Checkout products ***");
// StringBuilder updateCartPreparedStatement = new StringBuilder();
// StringBuilder orderDetails = new StringBuilder();
// orderDetails.append("Customer bought these Items: ");
// OrderEntity currentOrder;
// if (!products.isEmpty()) {
// for (Map.Entry<String, Integer> entry : products.entrySet()) {
// // Refresh quantity for every product before checking
// log.debug("*** Checking out product for user:*** " + entry.getKey());
// final ProductInventoryEntity productInventory =
// productInventoryRepository.findById(entry.getKey()).orElseThrow(() -> new
// NotEnoughProductsInStockException(entry.getKey()));

// final ProductDTO productDetails =
// productCatalogRestClient.getProductDetails(entry.getKey());

// if (productInventory.getQuantity() < entry.getValue()) {
// throw new NotEnoughProductsInStockException(productDetails.getTitle(),
// productInventory.getQuantity());
// }
// updateCartPreparedStatement.append("UPDATE product_inventory SET quantity =
// quantity - ")
// .append(entry.getValue()).append(" where asin =
// '").append(entry.getKey()).append("' ;");
// orderDetails.append(" Product: ").append(productDetails.getTitle()).
// append(", Quantity: ")
// .append(entry.getValue()).append(";");
// }
// double orderTotal =
// priceCalculatorService.calculateTotalPriceInCart(products);
// orderDetails.append(" orderEntity: ").append(orderTotal);
// currentOrder = createOrder(userId, orderDetails.toString(), orderTotal);
// updateCartPreparedStatement
// .append(" INSERT INTO orders (order_id, user_id, order_details, order_time,
// order_total) VALUES ("
// + "'" + currentOrder.getId() + "', " + "'1'" + ", '" +
// currentOrder.getOrderDetails()
// + "', '" + currentOrder.getOrderTime() + "'," + currentOrder.getOrderTotal()
// + ");");
// log.debug("Statement is " + updateCartPreparedStatement);
// transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
// transactionTemplate.execute(new TransactionCallbackWithoutResult() {
// @Override
// protected void doInTransactionWithoutResult(@NotNull TransactionStatus
// transactionStatus) {
// int updated =
// entityManager.createNativeQuery(updateCartPreparedStatement.toString()).executeUpdate();
// if (updated == 0) {
// String s = shoppingCartRestClient.clearCart(userId);
// products.clear();
// if (transactionStatus != null) {
// transactionStatus.flush();
// }
// log.debug("*** Checkout complete successfully, cart cleared ***");
// }
// }
// });
// } else {
// throw new CartIsEmptyException(userId);
// }
// return currentOrder;
// }

// }
