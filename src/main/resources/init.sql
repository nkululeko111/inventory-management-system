-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(12,4) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0
);

-- Create sales table
CREATE TABLE IF NOT EXISTS sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE  -- Added ON DELETE CASCADE
);

-- Create inventory_log table
CREATE TABLE IF NOT EXISTS inventory_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    operation VARCHAR(20) NOT NULL CHECK (operation IN ('ADD', 'REMOVE', 'UPDATE')),
    quantity INT NOT NULL,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE  -- Added ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50)
);

ALTER TABLE products ADD COLUMN supplier_id INT;
ALTER TABLE products ADD CONSTRAINT fk_supplier
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id);

