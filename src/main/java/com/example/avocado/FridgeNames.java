package com.example.avocado;

public class FridgeNames {
    //냉장고 이름 공유하는 클래스
    String FridgeName;

    public FridgeNames() { }

    public FridgeNames(String fridgeName) {
        FridgeName = fridgeName;
    }

    public String getFridgeName() {
        return FridgeName;
    }

    public void setFridgeName(String fridgeName) {
        FridgeName = fridgeName;
    }
}
