package com.innowise.event;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemEventTest {

    @Test
    void noArgsConstructor_shouldCreateEmptyItem() {
        OrderItemEvent item = new OrderItemEvent();

        assertThat(item).isNotNull();
        assertThat(item.getItemId()).isNull();
        assertThat(item.getItemName()).isNull();
        assertThat(item.getPrice()).isNull();
        assertThat(item.getQuantity()).isNull();
    }

    @Test
    void setters_shouldSetAllFields() {
        OrderItemEvent item = new OrderItemEvent();
        Long itemId = 123L;
        String itemName = "Test Product";
        BigDecimal price = new BigDecimal("99.99");
        Integer quantity = 5;

        item.setItemId(itemId);
        item.setItemName(itemName);
        item.setPrice(price);
        item.setQuantity(quantity);

        assertThat(item.getItemId()).isEqualTo(itemId);
        assertThat(item.getItemName()).isEqualTo(itemName);
        assertThat(item.getPrice()).isEqualByComparingTo(price);
        assertThat(item.getQuantity()).isEqualTo(quantity);
    }

    @Test
    void getters_shouldReturnCorrectValues() {
        OrderItemEvent item = new OrderItemEvent();
        Long itemId = 456L;
        String itemName = "Laptop";
        BigDecimal price = new BigDecimal("1299.99");
        Integer quantity = 2;

        item.setItemId(itemId);
        item.setItemName(itemName);
        item.setPrice(price);
        item.setQuantity(quantity);

        assertThat(item.getItemId()).isEqualTo(itemId);
        assertThat(item.getItemName()).isEqualTo(itemName);
        assertThat(item.getPrice()).isEqualByComparingTo(price);
        assertThat(item.getQuantity()).isEqualTo(quantity);
    }

    @Test
    void setItemId_shouldUpdateItemId() {
        OrderItemEvent item = new OrderItemEvent();
        Long initialId = 100L;
        Long newId = 200L;

        item.setItemId(initialId);
        assertThat(item.getItemId()).isEqualTo(initialId);

        item.setItemId(newId);

        assertThat(item.getItemId()).isEqualTo(newId);
    }

    @Test
    void setItemName_shouldUpdateItemName() {
        OrderItemEvent item = new OrderItemEvent();
        String initialName = "Old Name";
        String newName = "New Name";

        item.setItemName(initialName);
        assertThat(item.getItemName()).isEqualTo(initialName);

        item.setItemName(newName);

        assertThat(item.getItemName()).isEqualTo(newName);
    }

    @Test
    void setPrice_shouldUpdatePrice() {
        OrderItemEvent item = new OrderItemEvent();
        BigDecimal initialPrice = new BigDecimal("50.00");
        BigDecimal newPrice = new BigDecimal("75.00");

        item.setPrice(initialPrice);
        assertThat(item.getPrice()).isEqualByComparingTo(initialPrice);

        item.setPrice(newPrice);

        assertThat(item.getPrice()).isEqualByComparingTo(newPrice);
    }

    @Test
    void setQuantity_shouldUpdateQuantity() {
        OrderItemEvent item = new OrderItemEvent();
        Integer initialQuantity = 3;
        Integer newQuantity = 7;

        item.setQuantity(initialQuantity);
        assertThat(item.getQuantity()).isEqualTo(initialQuantity);

        item.setQuantity(newQuantity);

        assertThat(item.getQuantity()).isEqualTo(newQuantity);
    }

    @Test
    void equals_shouldReturnTrueForSameValues() {
        OrderItemEvent item1 = new OrderItemEvent();
        item1.setItemId(100L);
        item1.setItemName("Product A");
        item1.setPrice(new BigDecimal("50.00"));
        item1.setQuantity(2);

        OrderItemEvent item2 = new OrderItemEvent();
        item2.setItemId(100L);
        item2.setItemName("Product A");
        item2.setPrice(new BigDecimal("50.00"));
        item2.setQuantity(2);

        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentItemId() {
        OrderItemEvent item1 = new OrderItemEvent();
        item1.setItemId(100L);
        item1.setItemName("Product A");

        OrderItemEvent item2 = new OrderItemEvent();
        item2.setItemId(200L);
        item2.setItemName("Product A");

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentItemName() {
        OrderItemEvent item1 = new OrderItemEvent();
        item1.setItemId(100L);
        item1.setItemName("Product A");

        OrderItemEvent item2 = new OrderItemEvent();
        item2.setItemId(100L);
        item2.setItemName("Product B");

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentPrice() {
        OrderItemEvent item1 = new OrderItemEvent();
        item1.setItemId(100L);
        item1.setPrice(new BigDecimal("50.00"));

        OrderItemEvent item2 = new OrderItemEvent();
        item2.setItemId(100L);
        item2.setPrice(new BigDecimal("60.00"));

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentQuantity() {
        OrderItemEvent item1 = new OrderItemEvent();
        item1.setItemId(100L);
        item1.setQuantity(5);

        OrderItemEvent item2 = new OrderItemEvent();
        item2.setItemId(100L);
        item2.setQuantity(10);

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        OrderItemEvent item = new OrderItemEvent();
        item.setItemId(100L);
        item.setItemName("Product");
        item.setPrice(new BigDecimal("50.00"));
        item.setQuantity(3);

        int hashCode1 = item.hashCode();
        int hashCode2 = item.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void toString_shouldContainAllFields() {
        OrderItemEvent item = new OrderItemEvent();
        item.setItemId(789L);
        item.setItemName("Smartphone");
        item.setPrice(new BigDecimal("599.99"));
        item.setQuantity(1);

        String result = item.toString();

        assertThat(result).contains("789");
        assertThat(result).contains("Smartphone");
        assertThat(result).contains("599.99");
        assertThat(result).contains("1");
    }

    @Test
    void setPrice_shouldHandleZeroPrice() {
        OrderItemEvent item = new OrderItemEvent();

        item.setPrice(BigDecimal.ZERO);

        assertThat(item.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void setPrice_shouldHandleNegativePrice() {
        OrderItemEvent item = new OrderItemEvent();
        BigDecimal negativePrice = new BigDecimal("-10.00");

        item.setPrice(negativePrice);

        assertThat(item.getPrice()).isEqualByComparingTo(negativePrice);
    }

    @Test
    void setPrice_shouldHandleLargePrice() {
        OrderItemEvent item = new OrderItemEvent();
        BigDecimal largePrice = new BigDecimal("999999.99");

        item.setPrice(largePrice);

        assertThat(item.getPrice()).isEqualByComparingTo(largePrice);
    }

    @Test
    void setPrice_shouldHandlePriceWithManyDecimals() {
        OrderItemEvent item = new OrderItemEvent();
        BigDecimal precisePrice = new BigDecimal("99.999999");

        item.setPrice(precisePrice);

        assertThat(item.getPrice()).isEqualByComparingTo(precisePrice);
    }

    @Test
    void setQuantity_shouldHandleZeroQuantity() {
        OrderItemEvent item = new OrderItemEvent();

        item.setQuantity(0);

        assertThat(item.getQuantity()).isEqualTo(0);
    }

    @Test
    void setQuantity_shouldHandleLargeQuantity() {
        OrderItemEvent item = new OrderItemEvent();

        item.setQuantity(10000);

        assertThat(item.getQuantity()).isEqualTo(10000);
    }

    @Test
    void setItemName_shouldHandleEmptyString() {
        OrderItemEvent item = new OrderItemEvent();

        item.setItemName("");

        assertThat(item.getItemName()).isEmpty();
    }

    @Test
    void setItemName_shouldHandleLongString() {
        OrderItemEvent item = new OrderItemEvent();
        String longName = "A".repeat(1000);

        item.setItemName(longName);

        assertThat(item.getItemName()).hasSize(1000);
        assertThat(item.getItemName()).isEqualTo(longName);
    }

    @Test
    void setItemName_shouldHandleSpecialCharacters() {
        OrderItemEvent item = new OrderItemEvent();
        String specialName = "Product™ with €uro & symbols!@#$%";

        item.setItemName(specialName);

        assertThat(item.getItemName()).isEqualTo(specialName);
    }

    @Test
    void setters_shouldAllowNullValues() {
        OrderItemEvent item = new OrderItemEvent();
        item.setItemId(100L);
        item.setItemName("Product");
        item.setPrice(new BigDecimal("50.00"));
        item.setQuantity(5);

        item.setItemId(null);
        item.setItemName(null);
        item.setPrice(null);
        item.setQuantity(null);

        assertThat(item.getItemId()).isNull();
        assertThat(item.getItemName()).isNull();
        assertThat(item.getPrice()).isNull();
        assertThat(item.getQuantity()).isNull();
    }

    @Test
    void equals_shouldReturnTrueForSameObject() {
        OrderItemEvent item = new OrderItemEvent();
        item.setItemId(100L);

        assertThat(item).isEqualTo(item);
    }

    @Test
    void equals_shouldReturnFalseForNull() {
        OrderItemEvent item = new OrderItemEvent();
        item.setItemId(100L);

        assertThat(item).isNotEqualTo(null);
    }

    @Test
    void equals_shouldReturnFalseForDifferentClass() {
        OrderItemEvent item = new OrderItemEvent();
        item.setItemId(100L);
        String differentObject = "Not an OrderItemEvent";

        assertThat(item).isNotEqualTo(differentObject);
    }
}