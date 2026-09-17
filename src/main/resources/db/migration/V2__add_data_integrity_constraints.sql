ALTER TABLE category
    ADD CONSTRAINT uk_category_name UNIQUE (name);

ALTER TABLE product
    ADD CONSTRAINT chk_product_price_positive
        CHECK (price > 0),
    ADD CONSTRAINT chk_product_stock_non_negative
        CHECK (stock >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_item_quantity_positive
        CHECK (quantity > 0);