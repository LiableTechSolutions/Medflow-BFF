package com.medflow.shared.persistence;

import jakarta.persistence.AttributeConverter;
import java.util.Locale;

/**
 * Persists a Java enum using the lower-case spelling the database design uses for its
 * enum types ({@code WALK_IN} ⇄ {@code walk_in}). Subclasses are annotated
 * {@code @Converter(autoApply = true)} so entities keep plain enum fields.
 *
 * <p>Enum-typed columns are declared as {@code VARCHAR} plus a {@code CHECK} constraint
 * listing the exact values, which keeps the migrations portable across PostgreSQL and the
 * in-memory database used by the test suite while enforcing the same domain.
 */
public abstract class LowerCaseEnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

  private final Class<E> type;

  protected LowerCaseEnumConverter(Class<E> type) {
    this.type = type;
  }

  @Override
  public String convertToDatabaseColumn(E attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public E convertToEntityAttribute(String dbData) {
    return dbData == null ? null : Enum.valueOf(type, dbData.toUpperCase(Locale.ROOT));
  }
}
