CREATE TABLE category (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE product (
    id INT NOT NULL AUTO_INCREMENT,
    description VARCHAR(255) NOT NULL,
    stock INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE orders (
    id INT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NOT NULL,
    status VARCHAR(255) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE order_items (
    id INT NOT NULL AUTO_INCREMENT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id) REFERENCES product (id)
);
