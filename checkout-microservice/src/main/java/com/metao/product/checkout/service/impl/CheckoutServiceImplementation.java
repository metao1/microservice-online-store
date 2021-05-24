package com.metao.product.checkout.service.impl;

import com.metao.product.checkout.clients.ProductCatalogRestClient;
import com.metao.product.checkout.clients.ShoppingCartRestClient;
import com.metao.product.checkout.domain.OrderEntity;
import com.metao.product.checkout.domain.ProductInventoryEntity;
import com.metao.product.checkout.exception.CartIsEmptyException;
import com.metao.product.checkout.exception.NotEnoughProductsInStockException;
import com.metao.product.checkout.exception.UserException;
import com.metao.product.checkout.model.ProductDTO;
import com.metao.product.checkout.repository.ProductInventoryRepository;
import com.metao.product.checkout.service.CheckoutService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CheckoutServiceImplementation implements CheckoutService {

	private final ShoppingCartRestClient shoppingCartRestClient;
	private final ProductCatalogRestClient productCatalogRestClient;
	private final ProductInventoryRepository productInventoryRepository;
	private final PriceCalculatorService priceCalculatorService;
	private TransactionTemplate transactionTemplate;
	private final EntityManager entityManager;

	@Autowired
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public OrderEntity checkout(final String userId) throws NotEnoughProductsInStockException, CartIsEmptyException, UserException {
		if (Strings.isEmpty(userId)) throw new UserException("userId can't be null or empty");
		Map<String, Integer> products = shoppingCartRestClient.getProductsInCart(userId);
		log.debug("*** In Checkout products ***");
		StringBuilder updateCartPreparedStatement = new StringBuilder();
		OrderEntity currentOrder;
		StringBuilder orderDetails = new StringBuilder();
		orderDetails.append("Customer bought these Items: ");
		if (!products.isEmpty()) {
			for (Map.Entry<String, Integer> entry : products.entrySet()) {
				// Refresh quantity for every product before checking
				log.debug("*** Checking out product for user:*** " + entry.getKey());
				final ProductInventoryEntity productInventory = productInventoryRepository.findById(entry.getKey()).orElse(null);

				if (productInventory == null) {
					throw new NotEnoughProductsInStockException(entry.getKey());
				}

				final ProductDTO productDetails = productCatalogRestClient.getProductDetails(entry.getKey());

				if (productInventory.getQuantity() < entry.getValue()) {
					throw new NotEnoughProductsInStockException(productDetails.getTitle(), productInventory.getQuantity());
				}
				updateCartPreparedStatement.append("UPDATE product_inventory SET quantity = quantity - ")
						.append(entry.getValue()).append(" where asin = '").append(entry.getKey()).append("' ;");
				orderDetails.append(" Product: ").append(productDetails.getTitle()).
						append(", Quantity: ")
						.append(entry.getValue()).append(";");
			}
			double orderTotal = priceCalculatorService.calculateTotalPriceInCart(products);
			orderDetails.append(" orderEntity: ").append(orderTotal);
			currentOrder = createOrder(userId, orderDetails.toString(), orderTotal);
			updateCartPreparedStatement
					.append(" INSERT INTO orders (order_id, user_id, order_details, order_time, order_total) VALUES ("
							+ "'" + currentOrder.getId() + "', " + "'1'" + ", '" + currentOrder.getOrderDetails()
							+ "', '" + currentOrder.getOrderTime() + "'," + currentOrder.getOrderTotal() + ");");
			log.debug("Statement is " + updateCartPreparedStatement);
			sendTransaction(updateCartPreparedStatement, userId);
			products.clear();
		} else {
			throw new CartIsEmptyException(userId);
		}
		return currentOrder;
	}

	public void sendTransaction(StringBuilder updateCartPreparedStatement, String userId) {
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(@NonNull TransactionStatus transactionStatus) {
				int updated = entityManager.createNativeQuery(updateCartPreparedStatement.toString()).executeUpdate();
				if (updated == 0) {
					String s = shoppingCartRestClient.clearCart(userId);
					transactionStatus.flush();
					log.debug("*** Checkout complete successfully, cart cleared ***");
				}
			}
		});
	}

}
