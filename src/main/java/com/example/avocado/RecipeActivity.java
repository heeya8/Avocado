package com.example.avocado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecipeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<Recipes> list;
    private RecipesAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    ImageView imageView;
    int originalColor;
    boolean isColorChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        ActionBar ac = getSupportActionBar();
        ac.hide();
        //ac.setTitle(" "); //상단바 이름 설정

        imageView=findViewById(R.id.imageView3);

        recyclerView = findViewById(R.id.recyclerView);
        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipesAdapter(list, this);
        recyclerView.setAdapter(adapter);

        fetchRecipesFromFirestore();

        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.d_open, R.string.d_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigationView);
       /* navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id==R.id.action_recipe) {
                    Toast.makeText(getApplicationContext(), "현재 페이지 입니다.", Toast.LENGTH_SHORT).show();
                }else if (id==R.id.action_settings){
                    Toast.makeText(getApplicationContext(),"세팅",Toast.LENGTH_SHORT).show();
                }else if (id==R.id.action_Home){
                    Intent intent = new Intent(RecipeActivity.this, InFridgeActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });*/

        // 원래 색상 저장
        originalColor = ContextCompat.getColor(this, R.color.originalColor);
    }

    public void onImageViewClick(View view) {
        ImageView imageView = (ImageView) view;

        if (!isColorChanged) {
            // 색 변경
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.yellow), PorterDuff.Mode.SRC_IN);
            isColorChanged = true;
        } else {
            // 원래 색으로 되돌리기
            imageView.setColorFilter(originalColor, PorterDuff.Mode.SRC_IN);
            isColorChanged = false;
        }
    }

    private void fetchRecipesFromFirestore() {
        db.collection("Recipe")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String recipeName = document.getString("recipeName");
                            String ingredient1 = document.getString("ingredient1");
                            String ingredient2 = document.getString("ingredient2");
                            String ingredient3 = document.getString("ingredient3");
                            String ingredient4 = document.getString("ingredient4");
                            String link = document.getString("link");
                            String recipeImage = document.getString("recipeImage");

                            Recipes recipe = new Recipes(recipeName, ingredient1, ingredient2, ingredient3, ingredient4, link, recipeImage);
                            list.add(recipe);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RecipeActivity.this, "레시피를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
