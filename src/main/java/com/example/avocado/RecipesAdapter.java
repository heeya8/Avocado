package com.example.avocado;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipesViewHolder> {
    private ArrayList<Recipes> list;
    private Context context;

    public RecipesAdapter(ArrayList<Recipes> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false);
        return new RecipesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesViewHolder holder, int position) {
        Recipes recipe = list.get(position);
        holder.recipeName.setText(recipe.getRecipeName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class RecipesViewHolder extends RecyclerView.ViewHolder {
        TextView recipeName;

        public RecipesViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeName_rv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Recipes recipe = list.get(position);
                        showDialog(recipe);
                    }
                }
            });
        }
    }

    private void showDialog(Recipes recipe) {
        Dialog customDialog = new Dialog(context);
        customDialog.setContentView(R.layout.recipe_view);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView recipeName = customDialog.findViewById(R.id.recipeName);
        TextView ingredient1 = customDialog.findViewById(R.id.ingredient1);
        TextView ingredient2 = customDialog.findViewById(R.id.ingredient2);
        TextView ingredient3 = customDialog.findViewById(R.id.ingredient3);
        TextView ingredient4 = customDialog.findViewById(R.id.ingredient4);
        TextView link = customDialog.findViewById(R.id.recipe_link);
        ImageView recipeImageView = customDialog.findViewById(R.id.recipe_image);
        recipeName.setText(recipe.getRecipeName());
        ingredient1.setText(recipe.getIngredient1());
        ingredient2.setText(recipe.getIngredient2());
        ingredient3.setText(recipe.getIngredient3());
        ingredient4.setText(recipe.getIngredient4());

        String recipeImage = recipe.getRecipeImage();
        if (recipeImage != null) {
            Glide.with(context)
                    .load(recipeImage)
                    .into(recipeImageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.logo) // 기본 이미지 리소스
                    .into(recipeImageView);
        }

        link.setText(recipe.getLink());
        link.setMovementMethod(LinkMovementMethod.getInstance()); // 하이퍼링크 활성화

        TextView closeButton = customDialog.findViewById(R.id.option_codetype_dialog_positive);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        customDialog.show();
    }
}
