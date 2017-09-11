package com.xinra.reviewcommunity.entity;

import com.xinra.nucleus.entity.BaseEntity;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Market extends BaseEntity {
  
  private @NonNull String slug;
  
}