package com.medflow.modules.pharmacy.domain.entity;

import com.medflow.shared.exception.BusinessRuleViolationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "medications",
    uniqueConstraints = @UniqueConstraint(name = "uk_medications_name", columnNames = "name"))
public class Medication {

  @Id
  private UUID id;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, length = 100)
  private String category;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false)
  private int stockQuantity;

  @Column(nullable = false)
  private int reorderLevel;

  private LocalDate expiryDate;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected Medication() {
  }

  public Medication(String name, String category, BigDecimal unitPrice, int stockQuantity,
      int reorderLevel, LocalDate expiryDate) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.category = category;
    this.unitPrice = unitPrice;
    this.stockQuantity = stockQuantity;
    this.reorderLevel = reorderLevel;
    this.expiryDate = expiryDate;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void update(String name, String category, BigDecimal unitPrice, int reorderLevel,
      LocalDate expiryDate) {
    this.name = name;
    this.category = category;
    this.unitPrice = unitPrice;
    this.reorderLevel = reorderLevel;
    this.expiryDate = expiryDate;
    touch();
  }

  /** @return true when this adjustment crossed the reorder threshold downwards. */
  public boolean adjustStock(int delta) {
    var newQuantity = stockQuantity + delta;
    if (newQuantity < 0) {
      throw new BusinessRuleViolationException(
          "Insufficient stock: " + stockQuantity + " unit(s) of " + name + " available");
    }
    var wasAboveThreshold = stockQuantity > reorderLevel;
    this.stockQuantity = newQuantity;
    touch();
    return wasAboveThreshold && isLowStock();
  }

  public boolean isLowStock() {
    return stockQuantity <= reorderLevel;
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getName() { return name; }
  public String getCategory() { return category; }
  public BigDecimal getUnitPrice() { return unitPrice; }
  public int getStockQuantity() { return stockQuantity; }
  public int getReorderLevel() { return reorderLevel; }
  public LocalDate getExpiryDate() { return expiryDate; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
