package net.collegemc.mc.core.economy;

import com.google.common.base.Preconditions;

public enum EconomyOperation {

  ADD {
    @Override
    public double apply(double identity, double operand) {
      Preconditions.checkArgument(operand >= 0, "Cannot add a negative amount to an account");
      return identity + operand;
    }
  },
  SUBTRACT {
    @Override
    public double apply(double identity, double operand) {
      Preconditions.checkArgument(operand >= 0, "Cannot subtract a negative amount from an account");
      return identity - operand;
    }
  };

  public abstract double apply(double identity, double operand);

}
