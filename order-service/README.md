## Architecture decisions

- Event-driven microservices
- Kafka as integration layer
- Saga (choreography)
- Transactional outbox

### Order Service responsibilities

- Accept order creation request
- Persist order + outbox entry atomically
- Return immediately

### Kafka design

Topic: order.created
Key: orderId
JSON events with versioning + metadata