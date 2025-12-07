package com.lwf.model.bo;

import java.lang.Object;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCoreCtorBO {
  private String _accessControl;

  private String _validator;

  private String _rewardPool;

  private String _reviewNFT;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(_accessControl);
    args.add(_validator);
    args.add(_rewardPool);
    args.add(_reviewNFT);
    return args;
  }
}
