CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_email UNIQUE (email)
);

ALTER TABLE orders
    ADD COLUMN customer_id INT NULL,
    ADD CONSTRAINT fk_order_customer
        FOREIGN KEY (customer_id) REFERENCES users (id);


INSERT INTO users (name, email, password, role) VALUES
('Administrador', 'admin@order.local', '$2a$10$/UhZXPd043M.MvnSW96vde.uKyxgLcEZc5sVjaIuteUQRl3vNwM7.', 'ADMIN');
