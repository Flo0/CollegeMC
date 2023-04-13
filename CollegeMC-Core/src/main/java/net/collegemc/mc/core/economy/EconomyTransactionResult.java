package net.collegemc.mc.core.economy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EconomyTransactionResult {

  INSUFFICIENT_FUNDS(false),
  SUCCESS(true),
  FAILED(false);

  @Getter
  private final boolean success;

}
