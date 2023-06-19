package com.example.avocado;

import com.google.type.DateTime;

public class GridviewFood {
    String foodName;
    String expiryDate;
    String useDate;
    String category;

    public GridviewFood(String foodName, String expiryDate, String useDate, String category) {
        this.foodName = foodName;
        this.expiryDate = expiryDate;
        this.useDate = useDate;
        this.category = category;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getUseDate() {
        return useDate;
    }

    public void setUseDate(String useDate) {
        this.useDate = useDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
