package net.collegemc.mc.core.economy;

import lombok.Getter;
import net.collegemc.common.network.data.college.ProfileId;

import java.util.function.DoubleBinaryOperator;

public class EconomyAccount {

  @Getter
  private final ProfileId holderId;
  @Getter
  private double balance;

  public EconomyAccount(ProfileId holderId) {
    this.holderId = holderId;
  }

  public boolean applyToBalance(DoubleBinaryOperator operator, double operand) {
    double newBalance = operator.applyAsDouble(balance, operand);
    if (newBalance < 0) {
      return false;
    }
    balance = newBalance;
    return true;
  }

}
