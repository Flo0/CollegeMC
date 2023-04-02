package net.collegemc.common.network.data.college.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class AttributeContainer {

  private final Map<CollegeAttribute, Integer> attributeMap;

  public AttributeContainer() {
    this.attributeMap = new HashMap<>();
  }

  public int getAttributeLevel(CollegeAttribute attribute) {
    return this.attributeMap.getOrDefault(attribute, 0);
  }

  public void setAttributeLevel(CollegeAttribute attribute, int level) {
    this.attributeMap.put(attribute, level);
  }

  public void applyToAttribute(CollegeAttribute attribute, IntUnaryOperator operator) {
    this.setAttributeLevel(attribute, operator.applyAsInt(this.getAttributeLevel(attribute)));
  }

}
