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
public class ReviewNFTMintInputBO {
  private String to;

  private BigInteger reviewId;

  private String productId;

  public List<Object> toArgs() {
    List args = new ArrayList();
    args.add(to);
    args.add(reviewId);
    args.add(productId);
    return args;
  }
}
