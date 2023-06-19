package com.example.avocado;

public class Fridges {
    String fridgeType;
    String owner;

    public Fridges(String fridgeType, String owner) {
        this.fridgeType = fridgeType;
        this.owner = owner;
    }

    public String getFridgeType() {
        return fridgeType;
    }

    public void setFridgeType(String fridgeType) {
        this.fridgeType = fridgeType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
