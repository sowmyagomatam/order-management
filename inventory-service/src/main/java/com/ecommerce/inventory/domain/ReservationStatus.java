package com.ecommerce.inventory.domain;

/**
 * Lifecycle of an inventory reservation tied to an order.
 * RESERVED  - stock has been reserved for the order (active hold)
 * RELEASED  - reservation compensated/released back to available stock (order cancelled)
 * CONFIRMED - reservation consumed because the order was fulfilled
 */
public enum ReservationStatus {
    RESERVED,
    RELEASED,
    CONFIRMED
}