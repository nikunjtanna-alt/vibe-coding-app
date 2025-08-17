// Cart state
let cart = [];
let cartTotal = 0;

// DOM elements
const cartSidebar = document.getElementById('cartSidebar');
const cartItems = document.getElementById('cartItems');
const cartCount = document.getElementById('cartCount');
const cartTotalElement = document.getElementById('cartTotal');
const paymentModal = document.getElementById('paymentModal');
const paymentItems = document.getElementById('paymentItems');
const paymentTotal = document.getElementById('paymentTotal');

// Initialize the app
document.addEventListener('DOMContentLoaded', function() {
    updateCartDisplay();
    
    // Add form submission handler
    const paymentForm = document.querySelector('.payment-form');
    paymentForm.addEventListener('submit', handlePayment);
});

// Cart functions
function addToCart(name, price) {
    const existingItem = cart.find(item => item.name === name);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            name: name,
            price: price,
            quantity: 1
        });
    }
    
    updateCartDisplay();
    showNotification(`${name} added to cart!`);
}

function removeFromCart(name) {
    cart = cart.filter(item => item.name !== name);
    updateCartDisplay();
    showNotification(`${name} removed from cart!`);
}

function updateQuantity(name, change) {
    const item = cart.find(item => item.name === name);
    if (item) {
        item.quantity += change;
        if (item.quantity <= 0) {
            removeFromCart(name);
        } else {
            updateCartDisplay();
        }
    }
}

function updateCartDisplay() {
    // Update cart count
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCount.textContent = totalItems;
    
    // Update cart items display
    cartItems.innerHTML = '';
    cartTotal = 0;
    
    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        cartTotal += itemTotal;
        
        const cartItemElement = document.createElement('div');
        cartItemElement.className = 'cart-item';
        cartItemElement.innerHTML = `
            <div class="cart-item-info">
                <h4>${item.name}</h4>
                <div class="cart-item-price">$${item.price.toFixed(2)}</div>
            </div>
            <div class="cart-item-quantity">
                <button class="quantity-btn" onclick="updateQuantity('${item.name}', -1)">-</button>
                <span>${item.quantity}</span>
                <button class="quantity-btn" onclick="updateQuantity('${item.name}', 1)">+</button>
            </div>
        `;
        cartItems.appendChild(cartItemElement);
    });
    
    cartTotalElement.textContent = `$${cartTotal.toFixed(2)}`;
}

// Cart sidebar toggle
function toggleCart() {
    cartSidebar.classList.toggle('open');
}

// Payment functions
function processPayment() {
    if (cart.length === 0) {
        showNotification('Your cart is empty!', 'error');
        return;
    }
    
    // Update payment modal with cart items
    paymentItems.innerHTML = '';
    let paymentTotalAmount = 0;
    
    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        paymentTotalAmount += itemTotal;
        
        const paymentItemElement = document.createElement('div');
        paymentItemElement.className = 'payment-item';
        paymentItemElement.innerHTML = `
            <span>${item.name} x${item.quantity}</span>
            <span>$${itemTotal.toFixed(2)}</span>
        `;
        paymentItems.appendChild(paymentItemElement);
    });
    
    paymentTotal.textContent = `$${paymentTotalAmount.toFixed(2)}`;
    
    // Show payment modal
    paymentModal.classList.add('show');
}

function closePaymentModal() {
    paymentModal.classList.remove('show');
    // Reset form
    document.querySelector('.payment-form').reset();
}

async function handlePayment(event) {
    event.preventDefault();
    
    // Get form data
    const cardName = document.getElementById('cardName').value;
    const cardNumber = document.getElementById('cardNumber').value;
    const expiry = document.getElementById('expiry').value;
    const cvv = document.getElementById('cvv').value;
    
    // Simple validation
    if (!cardName || !cardNumber || !expiry || !cvv) {
        showNotification('Please fill in all payment fields!', 'error');
        return;
    }
    
    if (cardNumber.length < 16) {
        showNotification('Please enter a valid card number!', 'error');
        return;
    }
    
    // Simulate payment processing
    // In your script.js, replace the simulated payment with real API call
async function handlePayment(event) {
    event.preventDefault();
    
    try {
        // Prepare payment request
        const paymentRequest = {
            cardholderName: cardName,
            cardNumber: cardNumber,
            expiryDate: expiry,
            cvv: cvv,
            amount: cartTotal,
            orderItems: cart.map(item => ({
                productName: item.name,
                quantity: item.quantity,
                price: item.price
            }))
        };
        
        // Call the Spring Boot payment service
        const response = await fetch('http://localhost:8080/payment-service/api/payments/process', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(paymentRequest)
        });
        
        const result = await response.json();
        
        if (result.status === 'COMPLETED') {
            showNotification(`Payment successful! Transaction ID: ${result.transactionId}`, 'success');
            
            // Clear cart
            cart = [];
            updateCartDisplay();
            
            // Close modal
            closePaymentModal();
            
            // Close cart sidebar
            cartSidebar.classList.remove('open');
        } else {
            showNotification(result.errorMessage || 'Payment failed!', 'error');
        }
    } catch (error) {
        console.error('Payment error:', error);
        showNotification('Payment processing failed: ' + error.message, 'error');
    }
}

// Notification system
function showNotification(message, type = 'success') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas ${getNotificationIcon(type)}"></i>
            <span>${message}</span>
        </div>
    `;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${getNotificationColor(type)};
        color: white;
        padding: 15px 20px;
        border-radius: 10px;
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        z-index: 3000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
        max-width: 300px;
    `;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

function getNotificationIcon(type) {
    switch (type) {
        case 'success': return 'fa-check-circle';
        case 'error': return 'fa-exclamation-circle';
        case 'info': return 'fa-info-circle';
        default: return 'fa-bell';
    }
}

function getNotificationColor(type) {
    switch (type) {
        case 'success': return '#48bb78';
        case 'error': return '#e53e3e';
        case 'info': return '#4299e1';
        default: return '#667eea';
    }
}

// Close modal when clicking outside
paymentModal.addEventListener('click', function(event) {
    if (event.target === paymentModal) {
        closePaymentModal();
    }
});

// Close cart when clicking outside (on mobile)
document.addEventListener('click', function(event) {
    if (window.innerWidth <= 768) {
        if (!cartSidebar.contains(event.target) && 
            !event.target.closest('.cart-icon') && 
            cartSidebar.classList.contains('open')) {
            cartSidebar.classList.remove('open');
        }
    }
});

// Keyboard shortcuts
document.addEventListener('keydown', function(event) {
    // ESC key to close modals
    if (event.key === 'Escape') {
        if (paymentModal.classList.contains('show')) {
            closePaymentModal();
        }
        if (cartSidebar.classList.contains('open')) {
            cartSidebar.classList.remove('open');
        }
    }
});

// Add some nice animations for product cards
document.querySelectorAll('.product-card').forEach(card => {
    card.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-10px) scale(1.02)';
    });
    
    card.addEventListener('mouseleave', function() {
        this.style.transform = 'translateY(0) scale(1)';
    });
});

// Add loading animation for payment button
function addLoadingToButton(button) {
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
    button.disabled = true;
    
    return () => {
        button.innerHTML = originalText;
        button.disabled = false;
    };
}
