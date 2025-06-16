CREATE TABLE payment
(
  id                     BYTEA          NOT NULL,
  order_id               VARCHAR(255)   NOT NULL,
  amount                 DECIMAL(10, 2) NOT NULL,
  currency               VARCHAR(255)   NOT NULL,
  payment_method_type    VARCHAR(255)   NOT NULL,
  payment_method_details VARCHAR(255),
  status                 VARCHAR(255)   NOT NULL,
  failure_reason         VARCHAR(255),
  processed_at           TIMESTAMP WITHOUT TIME ZONE,
  created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_payment PRIMARY KEY (id)
);