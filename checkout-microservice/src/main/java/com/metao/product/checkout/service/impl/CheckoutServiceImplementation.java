package com.metao.product.checkout.service.impl;

import com.metao.product.checkout.clients.ProductCatalogRestClient;
import com.metao.product.checkout.clients.ShoppingCartRestClient;
import com.metao.product.checkout.domain.OrderEntity;
import com.metao.product.checkout.domain.ProductInventoryEntity;
import com.metao.product.checkout.exception.NotEnoughProductsInStockException;
import com.metao.product.checkout.repository.ProductInventoryRepository;
import com.metao.product.checkout.service.CheckoutService;
import com.metao.product.models.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CheckoutServiceImplementation implements CheckoutService {

	private final ShoppingCartRestClient shoppingCartRestClient;
	private final ProductCatalogRestClient productCatalogRestClient;
	private final ProductInventoryRepository productInventoryRepository;

	public OrderEntity checkout(String userId) throws NotEnoughProductsInStockException {
		Map<String, Integer> products = shoppingCartRestClient.getProductsInCart(userId);
		log.debug("*** In Checkout products ***");
		StringBuilder updateCartpreparedStatement = new StringBuilder();
		updateCartpreparedStatement.append("BEGIN TRANSACTION");
		OrderEntity currentOrder = null;
		StringBuilder orderDetails = new StringBuilder();
		orderDetails.append("Customer bought these Items: ");

		if (products.size() != 0) {
			for (Map.Entry<String, Integer> entry : products.entrySet()) {
				// Refresh quantity for every product before checking
				log.debug("*** Checking out product *** " + entry.getKey());
				final ProductInventoryEntity productInventory = productInventoryRepository.findById(entry.getKey()).orElse(null);
				final ProductDTO productDetails = productCatalogRestClient.getProductDetails(entry.getKey());

				if (productInventory.getQuantity() < entry.getValue())
					throw new NotEnoughProductsInStockException(productDetails.getTitle(), productInventory.getQuantity());

				updateCartpreparedStatement.append(" UPDATE product_inventory SET quantity = quantity - "
						+ entry.getValue() + " where asin = '" + entry.getKey() + "' ;");
				orderDetails.append(" Product: " + productDetails.getTitle() + ", Quantity: " + entry.getValue() + ";");
			}
			double orderTotal = getTotal(products);
			orderDetails.append(" Order Total is : " + orderTotal);
			currentOrder = createOrder(userId, orderDetails.toString(), orderTotal);
			updateCartpreparedStatement
					.append(" INSERT INTO orders (order_id, user_id, order_details, order_time, order_total) VALUES ("
							+ "'" + currentOrder.getId() + "', " + "'1'" + ", '" + currentOrder.getOrderDetails()
							+ "', '" + currentOrder.getOrderTime() + "'," + currentOrder.getOrderTotal() + ");");
			updateCartpreparedStatement.append(" END TRANSACTION;");
			log.debug("Statemet is " + updateCartpreparedStatement.toString());
			//cassandraTemplate.getCqlOperations().execute(updateCartpreparedStatement.toString());
		}
		products.clear();
		shoppingCartRestClient.clearCart(userId);
		log.debug("*** Checkout complete, cart cleared ***");
		return currentOrder;
	}

	private Double getTotal(Map<String, Integer> products) {
		double price = 0.0;
		ProductInventoryEntity productInventory;
		ProductDTO productDetails;
		for (Map.Entry<String, Integer> entry : products.entrySet()) {
			productInventory = productInventoryRepository.findById(entry.getKey()).orElse(null);
			productDetails = productCatalogRestClient.getProductDetails(entry.getKey());
			price = price + productDetails.getPrice() * entry.getValue();
		}
		return price;
	}

}
