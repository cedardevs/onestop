package org.cedar.onestop.registry.service;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
public class TopicsConfigurationProps {
  @NotNull
  private Integer numPartitions;
  @NotNull
  private Short replicationFactor;

  public Short getReplicationFactor() {
    return replicationFactor;
  }

  public void setReplicationFactor(Short replicationFactor) {
    this.replicationFactor = replicationFactor;
  }

  public Integer getNumPartitions() {
    return numPartitions;
  }

  public void setNumPartitions(Integer numPartitions) {
    this.numPartitions = numPartitions;
  }
}
