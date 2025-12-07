package com.lwf.model.bo;

import java.lang.Object;
import java.lang.String;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardPoolWithdrawTokensInputBO {
  private String to;

  private BigInteger amount;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(to);
    args.add(amount);
    return args;
  }
}
