// Global variables
let currentEditProductId = null;
let currentEditSupplierId = null;
const API_BASE_URL = 'http://localhost:4567/api';

document.addEventListener('DOMContentLoaded', function() {
    // Initialize dashboard metrics
    fetchDashboardMetrics();

    // Load initial data
    fetchProducts();
    fetchSales();
    fetchSuppliers();
    loadSupplierDropdown();
    loadProductsForSale(); // Load products for sales dropdown

    // Setup event listeners
    setupEventListeners();

    // Initialize alerts container
    const alertsContainer = document.createElement('div');
    alertsContainer.id = 'alertsContainer';
    alertsContainer.className = 'position-fixed top-0 end-0 p-3';
    alertsContainer.style.zIndex = '11';
    document.body.appendChild(alertsContainer);
});

function setupEventListeners() {
    // Product form
    document.getElementById('saveProductBtn').addEventListener('click', handleProductSave);
    document.getElementById('addProductModal').addEventListener('show.bs.modal', function() {
        loadSupplierDropdown();
        resetProductForm();
    });
    document.getElementById('addProductModal').addEventListener('hidden.bs.modal', resetProductForm);

    // Supplier form
    document.getElementById('saveSupplierBtn').addEventListener('click', handleSupplierSave);
    document.getElementById('addSupplierModal').addEventListener('show.bs.modal', resetSupplierForm);
    document.getElementById('addSupplierModal').addEventListener('hidden.bs.modal', resetSupplierForm);

    // Sales form
    document.getElementById('confirmSaleBtn').addEventListener('click', recordSale);
    document.getElementById('saleProduct').addEventListener('change', updateSalePrice);
    document.getElementById('saleQuantity').addEventListener('input', updateSaleTotal);

}

// ==================== PRODUCT FUNCTIONS ====================
function fetchProducts() {
    fetch(`${API_BASE_URL}/products`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(products => {
            const tableBody = document.getElementById('productsTableBody');
            tableBody.innerHTML = '';

            products.forEach(product => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${product.id}</td>
                    <td>${product.name}</td>
                    <td>$${product.price.toFixed(2)}</td>
                    <td>${product.stockQuantity}</td>
                    <td>${product.supplierName || 'No supplier'}</td>
                    <td>
                        <button class="btn btn-sm btn-warning me-2" onclick="prepareEditProduct(${product.id})">
                            <i class="fas fa-edit"></i> Edit
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="confirmDeleteProduct(${product.id})">
                            <i class="fas fa-trash"></i> Delete
                        </button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error fetching products:', error);
            showAlert('danger', 'Failed to load products');
        });
}

function handleProductSave() {
    const name = document.getElementById('productName').value.trim();
    const price = parseFloat(document.getElementById('productPrice').value);
    const stockQuantity = parseInt(document.getElementById('productQuantity').value);
    const supplierId = document.getElementById('productSupplier') ?
        parseInt(document.getElementById('productSupplier').value) || null : null;

    if (!name || isNaN(price) || isNaN(stockQuantity)) {
        showAlert('warning', 'Please enter valid product details');
        return;
    }

    const productData = {
        name: name,
        price: price,
        stockQuantity: stockQuantity,
        supplierId: supplierId
    };

    if (currentEditProductId) {
        updateProduct(currentEditProductId, productData);
    } else {
        addProduct(productData);
    }
}

function prepareEditProduct(productId) {
    fetch(`${API_BASE_URL}/products/${productId}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(product => {
            currentEditProductId = product.id;
            document.getElementById('productName').value = product.name;
            document.getElementById('productPrice').value = product.price;
            document.getElementById('productQuantity').value = product.stockQuantity;

            // Set supplier if dropdown exists
            if (document.getElementById('productSupplier')) {
                document.getElementById('productSupplier').value = product.supplierId || '';
            }

            // Update modal title and button
            document.getElementById('addProductModalLabel').textContent = 'Edit Product';
            document.getElementById('saveProductBtn').textContent = 'Update Product';

            // Show modal using vanilla JS (remove jQuery if not needed)
            const modal = new bootstrap.Modal(document.getElementById('addProductModal'));
            modal.show();
        })
        .catch(error => {
            console.error('Error fetching product:', error);
            showAlert('danger', 'Failed to load product details');
        });
}

function addProduct(productData) {
    fetch(`${API_BASE_URL}/products`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(productData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to add product');
        return response.json();
    })
    .then(data => {
        showAlert('success', 'Product added successfully');
        document.getElementById('addProductModal').querySelector('.btn-close').click();
        fetchProducts();
        fetchDashboardMetrics();
        loadProductsForSale();
         fetchSuppliers();

    })
    .catch(error => {
        console.error('Error adding product:', error);
        showAlert('danger', 'Failed to add product: ' + error.message);
    });
}

function updateProduct(productId, productData) {
    fetch(`${API_BASE_URL}/products/${productId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(productData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to update product');
        return response.json();
    })
    .then(data => {
        showAlert('success', 'Product updated successfully');
        document.getElementById('addProductModal').querySelector('.btn-close').click();
        fetchProducts();
        fetchDashboardMetrics();
        loadProductsForSale(); // Refresh products dropdown for sales
    })
    .catch(error => {
        console.error('Error updating product:', error);
        showAlert('danger', 'Failed to update product: ' + error.message);
    });
}

function confirmDeleteProduct(productId) {
    if (confirm('Are you sure you want to delete this product? This action cannot be undone.')) {
        deleteProduct(productId);
    }
}

function deleteProduct(productId) {
    fetch(`${API_BASE_URL}/products/${productId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to delete product');
        showAlert('success', 'Product deleted successfully');
        fetchProducts();
        fetchDashboardMetrics();
        loadProductsForSale();
    })
    .catch(error => {
        console.error('Error deleting product:', error);
        showAlert('danger', 'Failed to delete product');
    });
}

function resetProductForm() {
    const form = document.getElementById('addProductForm');
    if (form) {
        form.reset();
        document.getElementById('addProductModalLabel').textContent = 'Add New Product';
        document.getElementById('saveProductBtn').textContent = 'Save Product';
        currentEditProductId = null;
    }
}

// ==================== SUPPLIER FUNCTIONS ====================
function fetchSuppliers() {
    fetch(`${API_BASE_URL}/suppliers`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(suppliers => {
            const tableBody = document.getElementById('suppliersTableBody');
            tableBody.innerHTML = '';

            suppliers.forEach(supplier => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${supplier.id}</td>
                    <td>${supplier.name}</td>
                    <td>${supplier.contactPerson || 'N/A'}</td>
                    <td>${supplier.email || 'N/A'}</td>
                    <td>${supplier.phone || 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-warning me-2" onclick="prepareEditSupplier(${supplier.id})">
                            <i class="fas fa-edit"></i> Edit
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="confirmDeleteSupplier(${supplier.id})">
                            <i class="fas fa-trash"></i> Delete
                        </button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error fetching suppliers:', error);
            showAlert('danger', 'Failed to load suppliers');
        });
}

function loadSupplierDropdown() {
    fetch(`${API_BASE_URL}/suppliers`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(suppliers => {
            const dropdown = document.getElementById('productSupplier');
            if (!dropdown) return;

            // Clear existing options except the first one
            dropdown.innerHTML = '<option value="">Select Supplier</option>';

            // Add supplier options
            suppliers.forEach(supplier => {
                const option = document.createElement('option');
                option.value = supplier.id;
                option.textContent = supplier.name;
                dropdown.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading suppliers for dropdown:', error);
        });
}

function handleSupplierSave() {
    const name = document.getElementById('supplierName').value.trim();
    const contactPerson = document.getElementById('contactPerson').value.trim();
    const email = document.getElementById('supplierEmail').value.trim();
    const phone = document.getElementById('supplierPhone').value.trim();

    if (!name) {
        showAlert('warning', 'Supplier name is required');
        return;
    }

    const supplierData = {
        name: name,
        contactPerson: contactPerson || null,
        email: email || null,
        phone: phone || null
    };

    if (currentEditSupplierId) {
        updateSupplier(currentEditSupplierId, supplierData);
    } else {
        addSupplier(supplierData);
    }
}

function addSupplier(supplierData) {
    fetch(`${API_BASE_URL}/suppliers`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(supplierData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to add supplier');
        return response.json();
    })
    .then(data => {
        showAlert('success', 'Supplier added successfully');
        document.getElementById('addSupplierModal').querySelector('.btn-close').click();
        fetchSuppliers();
        loadSupplierDropdown();
    })
    .catch(error => {
        console.error('Error adding supplier:', error);
        showAlert('danger', 'Failed to add supplier: ' + error.message);
    });
}

function prepareEditSupplier(supplierId) {
    fetch(`${API_BASE_URL}/suppliers/${supplierId}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(supplier => {
            currentEditSupplierId = supplier.id;
            document.getElementById('supplierName').value = supplier.name;
            document.getElementById('contactPerson').value = supplier.contactPerson || '';
            document.getElementById('supplierEmail').value = supplier.email || '';
            document.getElementById('supplierPhone').value = supplier.phone || '';

            // Update modal title and button
            document.getElementById('addSupplierModalLabel').textContent = 'Edit Supplier';
            document.getElementById('saveSupplierBtn').textContent = 'Update Supplier';

            // Show modal using vanilla JS
            const modal = new bootstrap.Modal(document.getElementById('addSupplierModal'));
            modal.show();
        })
        .catch(error => {
            console.error('Error fetching supplier:', error);
            showAlert('danger', 'Failed to load supplier details');
        });
}

function updateSupplier(supplierId, supplierData) {
    fetch(`${API_BASE_URL}/suppliers/${supplierId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(supplierData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to update supplier');
        return response.json();
    })
    .then(data => {
        showAlert('success', 'Supplier updated successfully');
        document.getElementById('addSupplierModal').querySelector('.btn-close').click();
        fetchSuppliers();
        loadSupplierDropdown();
    })
    .catch(error => {
        console.error('Error updating supplier:', error);
        showAlert('danger', 'Failed to update supplier: ' + error.message);
    });
}

function confirmDeleteSupplier(supplierId) {
    if (confirm('Are you sure you want to delete this supplier? Products from this supplier will remain but their supplier reference will be removed.')) {
        deleteSupplier(supplierId);
    }
}

function deleteSupplier(supplierId) {
    fetch(`${API_BASE_URL}/suppliers/${supplierId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to delete supplier');
        showAlert('success', 'Supplier deleted successfully');
        fetchSuppliers();
        loadSupplierDropdown();
    })
    .catch(error => {
        console.error('Error deleting supplier:', error);
        showAlert('danger', 'Failed to delete supplier');
    });
}

function resetSupplierForm() {
    const form = document.getElementById('addSupplierForm');
    if (form) {
        form.reset();
        document.getElementById('addSupplierModalLabel').textContent = 'Add New Supplier';
        document.getElementById('saveSupplierBtn').textContent = 'Save Supplier';
        currentEditSupplierId = null;
    }
}

// ==================== SALES FUNCTIONS ====================
function fetchSales() {
    fetch(`${API_BASE_URL}/sales`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(sales => {
            const tableBody = document.getElementById('salesTableBody');
            tableBody.innerHTML = '';

            sales.forEach(sale => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${sale.id}</td>
                    <td>${sale.productName || `Product ID: ${sale.productId}`}</td>
                    <td>${sale.quantitySold}</td>
                    <td>$${sale.unitPrice.toFixed(2)}</td>
                    <td>$${(sale.quantitySold * sale.unitPrice).toFixed(2)}</td>
                    <td>${new Date(sale.saleDate).toLocaleString()}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error fetching sales:', error);
            showAlert('danger', 'Failed to load sales data');
        });
}

function loadProductsForSale() {
    fetch(`${API_BASE_URL}/products`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(products => {
            const dropdown = document.getElementById('saleProduct');
            if (!dropdown) return;

            dropdown.innerHTML = '<option value="">Select Product</option>';

            products.forEach(product => {
                const option = document.createElement('option');
                option.value = product.id;
                option.textContent = `${product.name} (${product.stockQuantity} in stock)`;
                option.dataset.price = product.price;
                dropdown.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading products for sale:', error);
            showAlert('danger', 'Failed to load products for sale');
        });
}

function updateSalePrice() {
    const productSelect = document.getElementById('saleProduct');
    const priceInput = document.getElementById('salePrice');
    const selectedOption = productSelect.options[productSelect.selectedIndex];

    if (selectedOption && selectedOption.dataset.price) {
        priceInput.value = selectedOption.dataset.price;
        updateSaleTotal();
    } else {
        priceInput.value = '';
        document.getElementById('saleTotalPrice').textContent = '$0.00';
    }
}

function updateSaleTotal() {
    const quantity = parseInt(document.getElementById('saleQuantity').value) || 0;
    const price = parseFloat(document.getElementById('salePrice').value) || 0;
    const total = (quantity * price).toFixed(2);
    document.getElementById('saleTotalPrice').textContent = `$${total}`;
}
function recordSale() {
    const productSelect = document.getElementById('saleProduct');
    const quantityInput = document.getElementById('saleQuantity');
    const priceInput = document.getElementById('salePrice');

    // Validate inputs exist
    if (!productSelect || !quantityInput || !priceInput) {
        showAlert('danger', 'Form elements not found');
        return;
    }

    // Get selected product details
    const selectedOption = productSelect.options[productSelect.selectedIndex];
    if (!selectedOption || !selectedOption.value) {
        showAlert('warning', 'Please select a product');
        return;
    }

    const availableStock = parseInt(selectedOption.dataset.stock) || 0;
    const quantity = parseInt(quantityInput.value);
    const price = parseFloat(priceInput.value);

    // Validate quantity
    if (isNaN(quantity) || quantity <= 0) {
        showAlert('warning', 'Please enter valid quantity (minimum 1)');
        return;
    }

    // Validate stock
    if (quantity > availableStock) {
        showAlert('warning', `Only ${availableStock} units available`);
        return;
    }

    // Validate price
    if (isNaN(price) || price <= 0) {
        showAlert('warning', 'Please enter valid price');
        return;
    }

    const saleData = {
        productId: parseInt(productSelect.value),
        quantitySold: quantity,
        unitPrice: price
    };

    // Show loading state
    const confirmBtn = document.getElementById('confirmSaleBtn');
    const originalBtnText = confirmBtn.innerHTML;
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Processing...';

    fetch(`${API_BASE_URL}/sales`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(saleData)
    })
    .then(async response => {
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Sale failed');
        }
        return data;
    })
    .then(data => {
        showAlert('success', 'Sale recorded successfully');

        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('recordSaleModal'));
        if (modal) modal.hide();

        // Refresh data
        fetchSales();
        fetchProducts();
        fetchDashboardMetrics();
        loadProductsForSale();
    })
    .catch(error => {
        console.error('Sale error:', error);
        showAlert('danger', error.message || 'Failed to record sale');
    })
    .finally(() => {
        // Reset button state
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = originalBtnText;
        }
    });
}
// ==================== DASHBOARD & UTILITY FUNCTIONS ====================
function fetchDashboardMetrics() {
    fetch(`${API_BASE_URL}/products`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(products => {
            // Update total products count
            document.getElementById('totalProducts').textContent = products.length;

            // Calculate and update total stock
            const totalStock = products.reduce((sum, product) => sum + (product.stockQuantity || 0), 0);
            document.getElementById('totalStock').textContent = totalStock;

            // Calculate and update low stock items
            const lowStockItems = products.filter(p => (p.stockQuantity || 0) < 5).length;
            document.getElementById('lowStock').textContent = lowStockItems;
        })
        .catch(error => {
            console.error('Error fetching dashboard metrics:', error);
            showAlert('danger', 'Failed to load dashboard data');
        });

    // Load today's sales
    const today = new Date().toISOString().split('T')[0];
    fetch(`${API_BASE_URL}/reports/sales?from=${today}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(report => {
            document.getElementById('todaysSales').textContent = report.totalSales || 0;
        })
        .catch(error => {
            console.error('Error fetching today\'s sales:', error);
        });
}
function showAlert(type, message) {
    // Ensure the alerts container exists
    let alertsContainer = document.getElementById('alertsContainer');

    // If it doesn't exist, create it
    if (!alertsContainer) {
        alertsContainer = document.createElement('div');
        alertsContainer.id = 'alertsContainer';
        alertsContainer.className = 'position-fixed top-0 end-0 p-3';
        alertsContainer.style.zIndex = '1100';
        document.body.appendChild(alertsContainer);
    }

    // Create the alert element
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.role = 'alert';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    // Add the alert to the container
    alertsContainer.appendChild(alertDiv);

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        alertDiv.classList.remove('show');
        alertDiv.addEventListener('transitionend', () => {
            alertDiv.remove();
        });
    }, 5000);
}
// Stock adjustment functions
function adjustStock(productId, delta) {
    fetch(`${API_BASE_URL}/products/${productId}/stock/adjust`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ delta: delta })
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to adjust stock');
        return response.json();
    })
    .then(data => {
        showAlert('success', 'Stock adjusted successfully');
        fetchProducts();
        fetchDashboardMetrics();
        loadProductsForSale();
    })
    .catch(error => {
        console.error('Error adjusting stock:', error);
        showAlert('danger', 'Failed to adjust stock');
    });
}