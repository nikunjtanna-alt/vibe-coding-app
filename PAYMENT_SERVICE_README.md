# Payment Service - Spring Boot Microservice

A complete payment processing microservice built with Spring Boot, featuring REST APIs, SQL database storage, and comprehensive payment validation.

## 🚀 Features

- **RESTful API**: Complete CRUD operations for payments
- **Payment Processing**: Simulated payment gateway integration
- **Database Storage**: H2 in-memory database (configurable for MySQL/PostgreSQL)
- **Validation**: Comprehensive payment validation including Luhn algorithm
- **Security**: Card number masking and CVV protection
- **Statistics**: Payment analytics and reporting
- **CORS Support**: Frontend integration ready
- **Health Checks**: Service monitoring endpoints

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controller    │    │     Service     │    │   Repository    │
│   (REST API)    │◄──►│  (Business      │◄──►│   (Database)    │
│                 │    │    Logic)       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      DTOs       │    │   Validation    │    │   Entities      │
│  (Request/      │    │    Service      │    │   (JPA)         │
│   Response)     │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## 🛠️ Installation & Setup

### 1. Clone and Build
```bash
# Navigate to the project directory
cd payment-service

# Build the project
mvn clean install
```

### 2. Run the Application
```bash
# Run with Maven
mvn spring-boot:run

# Or run the JAR file
java -jar target/payment-service-1.0.0.jar
```

### 3. Access the Application
- **API Base URL**: `http://localhost:8080/payment-service`
- **H2 Database Console**: `http://localhost:8080/payment-service/h2-console`
- **Health Check**: `http://localhost:8080/payment-service/api/payments/health`

## 📚 API Endpoints

### Payment Processing
```
POST /api/payments/process
```
Process a new payment transaction.

**Request Body:**
```json
{
  "cardholderName": "John Doe",
  "cardNumber": "4111111111111111",
  "expiryDate": "12/25",
  "cvv": "123",
  "amount": 99.99,
  "orderItems": [
    {
      "productName": "Laptop",
      "quantity": 1,
      "price": 99.99
    }
  ]
}
```

**Response:**
```json
{
  "transactionId": "TXN-1703123456789-ABC12345",
  "status": "COMPLETED",
  "message": "Payment processed successfully",
  "amount": 99.99,
  "processedAt": "2023-12-21T10:30:45"
}
```

### Payment Retrieval
```
GET /api/payments/{id}                    # Get payment by ID
GET /api/payments/transaction/{txnId}     # Get payment by transaction ID
GET /api/payments                          # Get all payments
GET /api/payments/status/{status}         # Get payments by status
GET /api/payments/successful              # Get successful payments
GET /api/payments/failed                  # Get failed payments
GET /api/payments/recent                  # Get recent payments
```

### Statistics
```
GET /api/payments/stats
```
Returns payment statistics:
```json
{
  "totalCompleted": 150,
  "totalFailed": 25,
  "totalAmount": 15750.00
}
```

### Health Check
```
GET /api/payments/health
```
Returns service health status.

## 🗄️ Database Schema

### Payments Table
```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cardholder_name VARCHAR(255) NOT NULL,
    card_number VARCHAR(255) NOT NULL,
    expiry_date VARCHAR(10) NOT NULL,
    cvv VARCHAR(10) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    error_message TEXT
);
```

## 🔧 Configuration

### Development (H2 Database)
```properties
spring.datasource.url=jdbc:h2:mem:paymentdb
spring.datasource.username=sa
spring.datasource.password=password
```

### Production (MySQL)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/paymentdb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 Testing

### Test Card Numbers
- **Always Success**: `4111111111111111`
- **Always Fail**: `4000000000000000`
- **Random Success/Fail**: Any other valid card number

### Sample API Calls

#### Process Payment
```bash
curl -X POST http://localhost:8080/payment-service/api/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "cardholderName": "John Doe",
    "cardNumber": "4111111111111111",
    "expiryDate": "12/25",
    "cvv": "123",
    "amount": 99.99,
    "orderItems": [
      {
        "productName": "Laptop",
        "quantity": 1,
        "price": 99.99
      }
    ]
  }'
```

#### Get All Payments
```bash
curl http://localhost:8080/payment-service/api/payments
```

#### Get Payment Statistics
```bash
curl http://localhost:8080/payment-service/api/payments/stats
```

## 🔒 Security Features

- **Card Number Masking**: Only last 4 digits stored
- **CVV Protection**: CVV not stored in database
- **Input Validation**: Comprehensive validation rules
- **Luhn Algorithm**: Credit card number validation
- **Expiry Date Validation**: Checks for expired cards

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/payment-service/api/payments/health
```

### H2 Database Console
Access at: `http://localhost:8080/payment-service/h2-console`
- JDBC URL: `jdbc:h2:mem:paymentdb`
- Username: `sa`
- Password: `password`

## 🔄 Integration with Frontend

### CORS Configuration
The service is configured to accept requests from any origin:
```java
@CrossOrigin(origins = "*")
```

### Frontend Integration Example
```javascript
// Process payment
const response = await fetch('http://localhost:8080/payment-service/api/payments/process', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(paymentRequest)
});

const result = await response.json();
```

## 🚀 Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/payment-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export DB_USERNAME=your-db-username
export DB_PASSWORD=your-db-password
```

## 📝 Logging

The application logs:
- Payment processing events
- Validation errors
- Database operations
- API requests/responses

Log levels can be configured in `application.properties`.

## 🔧 Customization

### Adding New Payment Methods
1. Extend `PaymentProcessingService`
2. Add new validation rules in `PaymentValidationService`
3. Update DTOs and entities as needed

### Database Migration
For production, consider using Flyway or Liquibase for database migrations.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions:
1. Check the logs for error details
2. Verify API request format
3. Test with sample data
4. Check database connectivity

---

**Happy Coding! 🎉**
