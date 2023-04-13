package net.collegemc.mc.core.economy;

import lombok.Getter;
import net.collegemc.common.network.data.college.ProfileId;

import java.util.UUID;

public class EconomyTransaction {

  @Getter
  private final ProfileId targetId;
  @Getter
  private final UUID transactionId;
  @Getter
  private final EconomyOperation operation;
  @Getter
  private final double amount;

  public EconomyTransaction(ProfileId targetId, EconomyOperation operation, double amount) {
    this.targetId = targetId;
    this.operation = operation;
    this.amount = amount;
    this.transactionId = UUID.randomUUID();
  }

  public EconomyTransactionResult applyTo(EconomyAccount economyAccount) {
    double originalBalance = economyAccount.getBalance();
    try {
      boolean success = economyAccount.applyToBalance(operation::apply, amount);
      return success ? EconomyTransactionResult.SUCCESS : EconomyTransactionResult.INSUFFICIENT_FUNDS;
    } catch (Exception e) {
      economyAccount.applyToBalance((a, b) -> originalBalance, 0);
      return EconomyTransactionResult.FAILED;
    }
  }

}
