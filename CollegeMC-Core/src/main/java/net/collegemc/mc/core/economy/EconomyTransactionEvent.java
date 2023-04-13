package net.collegemc.mc.core.economy;

import net.collegemc.common.network.data.college.ProfileId;

import java.util.UUID;

public class EconomyTransactionEvent {

  private final ProfileId targetId;
  private final UUID transactionId;
  private final long timestamp;
  private final EconomyTransactionResult result;
  private final EconomyOperation operation;
  private final double amount;

  public EconomyTransactionEvent(ProfileId targetId, UUID transactionId, long timestamp, EconomyTransactionResult result, EconomyOperation operation, double amount) {
    this.targetId = targetId;
    this.transactionId = transactionId;
    this.timestamp = timestamp;
    this.result = result;
    this.operation = operation;
    this.amount = amount;
  }
}
