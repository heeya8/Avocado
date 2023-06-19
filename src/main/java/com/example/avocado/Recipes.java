package com.example.avocado;

public class Recipes {
    String recipeName;
    String ingredient1;
    String ingredient2;
    String ingredient3;
    String ingredient4;
    String link;
    String recipeImage;

    public Recipes(String recipeName, String ingredient1, String ingredient2, String ingredient3, String ingredient4, String link, String recipeImage) {
        this.recipeName = recipeName;
        this.ingredient1 = ingredient1;
        this.ingredient2 = ingredient2;
        this.ingredient3 = ingredient3;
        this.ingredient4 = ingredient4;
        this.link = link;
        this.recipeImage = recipeImage;
    }

    public String getRecipeName() { return recipeName; }

    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public String getIngredient1() { return ingredient1; }

    public void setIngredient1(String ingredient1) { this.ingredient1 = ingredient1; }

    public String getIngredient2() { return ingredient2; }

    public void setIngredient2(String ingredient2) { this.ingredient2 = ingredient2; }

    public String getIngredient3() {
        return ingredient3;
    }

    public void setIngredient3(String ingredient3) {
        this.ingredient3 = ingredient3;
    }

    public String getIngredient4() { return ingredient4; }

    public void setIngredient4(String ingredient4) {
        this.ingredient4 = ingredient4;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) { this.link = link; }

    public String getRecipeImage() { return recipeImage; }

    public void setRecipeImage(String recipeImage) { this.recipeImage = recipeImage; }
}
