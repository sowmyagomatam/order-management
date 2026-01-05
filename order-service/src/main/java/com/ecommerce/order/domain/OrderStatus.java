package com.ecommerce.order.domain;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED;

    public boolean canTransitionTo(OrderStatus newStatus){
        return switch (this) {
            case PENDING -> newStatus == INVENTORY_RESERVED ||
                    newStatus == CANCELLED;

            case INVENTORY_RESERVED -> newStatus == PAYMENT_PROCESSING ||
                    newStatus == CANCELLED;

            case PAYMENT_PROCESSING -> newStatus == PAYMENT_COMPLETED ||
                    newStatus == FAILED ||
                    newStatus == CANCELLED;

            case PAYMENT_COMPLETED -> newStatus == CONFIRMED;

            case CONFIRMED -> newStatus == SHIPPED ||
                    newStatus == CANCELLED;

            case SHIPPED -> newStatus == DELIVERED;

            // Terminal states - cannot transition from here
            case DELIVERED, CANCELLED, FAILED -> false;
        };
    }

    public boolean isTerminalState(){
        return this == DELIVERED || this == CANCELLED || this == FAILED;
    }

    // can an order be cancelled from this state
    public boolean isCancellable(){
        return this == PENDING ||
                this == INVENTORY_RESERVED ||
                this == CONFIRMED ||
                this == PAYMENT_PROCESSING ||
                this == PAYMENT_COMPLETED;
    }

}
