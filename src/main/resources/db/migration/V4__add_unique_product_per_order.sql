ALTER TABLE order_items
    ADD CONSTRAINT uk_order_item_product UNIQUE (order_id, product_id);
