package com.pakrhanbeen.modernjavainaction.chapter10;

/**
 * 거래.
 */
class Trade {
    public enum Type {
        BUY,
        SELL
    }
    private Type type;

    private Stock stock;
    private int quantity;
    private double price;

    Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }

    Stock getStock() {
        return stock;
    }

    void setStock(Stock stock) {
        this.stock = stock;
    }

    int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    double getPrice() {
        return price;
    }

    void setPrice(double price) {
        this.price = price;
    }

    double getValue() {
        return quantity * price;
    }
}
