package com.lwf.model.bo;

import java.lang.Object;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinDayValidatorSetMinCoinDaysInputBO {
  private BigInteger _minCoinDays;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(_minCoinDays);
    return args;
  }
}
