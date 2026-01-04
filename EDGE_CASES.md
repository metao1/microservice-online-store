# Edge Cases and Exception Flows

This document outlines potential edge cases and exception flows for the implemented e-commerce functionalities.

## User Signup/Login (Currently Stubbed)

* **User ID**: `userId` is currently passed directly.
* **Edge Cases (Future Considerations for User Microservice):**
    * Invalid `userId` format.
    * Attempting operations for a non-existent `userId` (should result in 404s or clear error messages).
    * Authentication failures (invalid credentials, expired tokens) - Not applicable with current stubbing.
    * Authorization failures (user trying to access/modify data not belonging to them) - Not applicable with current
      stubbing.

## Product Browse/Search (Inventory Microservice)

* **Get Product by SKU (`GET /products/{sku}`):**
    * **Edge Case**: SKU exists but product has missing critical fields (e.g., null price, though DB constraints might
      prevent this).
        * **Expected**: Service should handle gracefully; ideally, data validation at creation prevents this.
    * **Exception Flow**: Product with given SKU not found.
        * **Handled**: Returns HTTP 404 Not Found (as tested in `ProductControllerIT` and `ProductServiceTest` expecting
          `ProductNotFoundException`).
* **Get Products by Category (`GET /products/category/{name}`):**
    * **Edge Case**: Category name with special characters or very long names.
        * **Expected**: URL encoding should handle valid characters; service should trim/validate length if necessary.
    * **Edge Case**: Category exists but has no products.
        * **Handled**: Returns an empty list (as tested).
* **Search Products (`GET /products/search?keyword=X`):**
    * **Edge Case**: Keyword is empty or very long.
        * **Expected**: Empty keyword might return all products (if designed so) or no products. Long keywords should be
          handled without error. Current implementation likely returns no products for empty keyword if LIKE query is
          `%%`.
    * **Edge Case**: Keyword contains only special characters.
        * **Expected**: Likely returns no results.
    * **Edge Case**: Search results exceed reasonable limits (pagination helps, defaults to 10).

## Shopping Cart (Order Microservice)

* **General:**
    * **Edge Case**: `userId` or `sku` contains special characters or is excessively long.
        * **Expected**: Appropriate validation or encoding should be in place. Current implementation relies on String.
* **Add Item to Cart (`POST /cart/{userId}/{sku}`):**
    * **Edge Case**: Adding a product with quantity zero or negative.
        * **Expected**: Service should reject (e.g., return HTTP 400 Bad Request). Current `AddItemRequestDTO` doesn't
          have validation constraints on quantity, `ShoppingCartService` might need to add this.
    * **Edge Case**: `price` or `currency` in `AddItemRequestDTO` is invalid (e.g., negative price, unsupported
      currency).
        * **Expected**: Validation should catch this. `Currency` type handles valid currencies. Price validation might
          be needed.
    * **Edge Case (Future - Inventory Check)**: Product SKU does not exist in inventory, or product is out of stock.
        * **Expected**: Service should reject addition or inform user. *Currently not implemented.*
* **View Cart (`GET /cart/{userId}`):**
    * **Edge Case**: User exists but has an empty cart.
        * **Handled**: Returns `ShoppingCartDto` with an empty item list.
* **Update Item Quantity (`PUT /cart/{userId}/{sku}`):**
    * **Edge Case**: `UpdateCartItemQtyDTO` has non-numeric or excessively large quantity.
        * **Expected**: Should be caught by deserialization or validation.
    * **Exception Flow**: Item not found in cart for the given `userId` and `sku`.
        * **Handled**: Service throws `OrderNotFoundException`, controller returns HTTP 404 (as tested).
* **Remove Item from Cart (`DELETE /cart/{userId}/{sku}`):**
    * **Exception Flow**: Item not found in cart.
        * **Handled**: Service throws `OrderNotFoundException` (as per worker report for controller tests), controller
          returns HTTP 404.
* **Clear Cart (`DELETE /cart/{userId}` - if implemented in controller):**
    * **Edge Case**: Clearing an already empty cart.
        * **Expected**: Should succeed silently (HTTP 204 No Content).

## Order Checkout (Order Microservice - `POST /order`)

* **Using `CreateOrderRequestDTO` (`{"userId": "..."}`):**
    * **Exception Flow**: Cart is empty for the given `userId`.
        * **Handled**: `OrderController` returns HTTP 400 Bad Request (as tested).
    * **Edge Case (Future - Inventory Check)**: Items in the cart are no longer available or prices have changed
      significantly since adding to cart.
        * **Expected**: Order creation should fail for those items, or the entire order, or user should be notified.
          *Currently, no inventory check is performed during order creation.*
    * **Edge Case**: One of many cart items fails processing (e.g., hypothetical inventory check fails for one item).
        * **Expected**: Transactional behavior needs to be defined. Does the whole order fail? Or are partial orders
          created? Current implementation creates separate order events for each cart item. If one event publication
          fails, others might still proceed. The `OrderController` loop does not seem transactional across all items.
* **Kafka Interaction:**
    * **Edge Case**: Kafka broker is down or unreachable when `KafkaEventHandler` tries to publish `OrderCreatedEvent`.
        * **Expected**: `KafkaEventHandler`'s `applyExceptionally` block might catch this, but the current
          implementation within it just calls `kafkaFactory.submit/publish` again. Robust retry/dead-letter queue (DLQ)
          mechanism would be needed for production. Spring Kafka's `@RetryableTopic` on the listener side helps with
          consumer errors, but producer errors need handling too.

## Payment (Handled by `payment-microservice` - Mocked Processing)

* **Current Mock**: The `payment-microservice` consumes `OrderCreatedEvent`s, simulates payment processing (random
  success/failure), and produces `OrderPaymentEvent`s (`SUCCESSFUL` or `FAILED`). The `order-microservice` consumes
  these events to update order status to `PAID` or `PAYMENT_FAILED`.
* **Edge Cases:**
    * **`payment-microservice` is down/unavailable**: `OrderCreatedEvent`s will queue in Kafka (if topic is durable) or
      be lost (if not/retention period exceeded). Orders in `order-microservice` will remain in `NEW` (or initial)
      status. No `OrderPaymentEvent` will be produced.
    * **`OrderPaymentEvent` is lost**: If `payment-microservice` processes payment but its `OrderPaymentEvent` fails to
      be published or is lost before `order-microservice` consumes it, the order status in `order-microservice` will not
      be updated, leading to inconsistency. (Requires robust Kafka production/consumption, possibly dead-letter queues).
    * **`order-microservice` fails to process `OrderPaymentEvent`**: If `order-microservice`'s listener for payment
      events fails (e.g., database issue when updating order status), the event might go to a DLQ or be retried. Order
      status would remain unchanged until resolved.
    * **Duplicate `OrderCreatedEvent` processing by `payment-microservice`**: If the payment service processes the same
      order event twice (e.g., due to Kafka consumer retries without idempotency), it might attempt two payments or send
      two `OrderPaymentEvent`s. The payment processing logic should ideally be idempotent.
    * **Duplicate `OrderPaymentEvent` processing by `order-microservice`**: The `PaymentEventListener` in
      `order-microservice` has basic idempotency (checks if order is already `PAID`/`PAYMENT_FAILED`).
* **Future Considerations (Real Payment Microservice):**
    * Payment authorization failure (insufficient funds, invalid card).
    * Payment gateway timeout or errors.
    * Fraud detection holds.
    * Handling refunds and chargebacks.

## Order Confirmation Notification (Placeholder Log Message)

* **Current Placeholder**: A log message "Order X status updated to CONFIRMED after mock payment. Notification pending."
* **Edge Cases (Future - Real Notification Service):**
    * Invalid email/phone for user.
    * Notification service unavailable.
    * Notification bounces or fails delivery.
    * **Expected (Future)**: Retries, logging of notification status. User should still be able to see order status via
      API.
