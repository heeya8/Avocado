package com.example.avocado;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InFridgeActivity extends AppCompatActivity {

    Intent intent;
    ArrayList<GridviewFood> list;
    GridviewFoodAdapter adapter;

    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    View navHeader;

    View dialogView, dia2, dia3;
    FloatingActionButton fab_in_fridge, fabAdd, fabCamera;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mFirebaseAuth;
    EditText foodName;
    DatePicker expiryDate, useDate;

    Spinner categorySpinner;
    String[] categoryItems = {"과일","채소","육류","수산물","유제품","기타"};
    String categorySelect;

    GridView gridView;

    //그리드뷰 위에 냉장고 이름 출력
    TextView fn;

    // 식재료 정보 조회 다이얼로그 위젯들
    TextView food, food2, foodCategory, foodExpiryDate, foodUseDate, cal, carb, fat, protein, storage;

    Boolean isFabOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_fridge);
        fab_in_fridge = (FloatingActionButton) findViewById(R.id.fab_in_fridge);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabCamera = (FloatingActionButton) findViewById(R.id.fabCamera);
        expiryDate = (DatePicker) findViewById(R.id.expiryDate);
        useDate = (DatePicker) findViewById(R.id.useDate);

        intent = getIntent();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        fabAdd.setVisibility(View.GONE);
        fabCamera.setVisibility(View.GONE);

        //그리드뷰 위에 냉장고 이름 출력
        fn = (TextView) findViewById(R.id.fn);
        fn.setText("- " + intent.getStringExtra("FridgeName") + " -");

        //그리드 뷰
        list = new ArrayList<>();
        gridView = findViewById(R.id.gridView);
        adapter = new GridviewFoodAdapter(list, getApplicationContext());

        //네비게이션뷰
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navHeader = navigationView.getHeaderView(0);

        TextView name = navHeader.findViewById(R.id.name); // 헤더 뷰에서 텍스트뷰 찾기
        name.setText(intent.getStringExtra("FridgeName")); // 원하는 텍스트 설정
        TextView email = navHeader.findViewById(R.id.email);
        email.setText(firebaseUser.getEmail());

        //네비게이션 토글 나타나게하는
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.d_open, R.string.d_close);
        toggle.syncState();

        ActionBar ac = getSupportActionBar();
        ac.setTitle(" "); //상단바 이름 설정


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_recipe) {
                    Intent intent = new Intent(InFridgeActivity.this, RecipeActivity.class);
                    startActivity(intent);
                } else if (id == R.id.action_settings) {
                    Toast.makeText(getApplicationContext(), "세팅", Toast.LENGTH_SHORT).show();
                }else if (id == R.id.action_Home) {
                    Toast.makeText(getApplicationContext(), "현재 페이지 입니다.", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogView = (View) View.inflate(InFridgeActivity.this, R.layout.food_add, null);

                foodName = (EditText) dialogView.findViewById(R.id.foodType);
                expiryDate = (DatePicker) dialogView.findViewById(R.id.expiryDate);
                useDate = (DatePicker) dialogView.findViewById(R.id.useDate);

                categorySpinner = (Spinner) dialogView.findViewById(R.id.categorySpinner);

                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_spinner_item,
                        categoryItems);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(categoryAdapter);

                categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        categorySelect = categoryItems[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(InFridgeActivity.this);
                dlgBuilder.setView(dialogView);

                //다이얼 로그 타이틀
                LinearLayout titleLayout = new LinearLayout(InFridgeActivity.this);
                titleLayout.setOrientation(LinearLayout.VERTICAL);
                titleLayout.setPadding(0, 30, 0, 0);

                TextView titleTextView = new TextView(InFridgeActivity.this);
                titleTextView.setText("- 식재료 추가 -");
                titleTextView.setGravity(Gravity.CENTER);
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                titleTextView.setTypeface(null, Typeface.BOLD);

                titleLayout.addView(titleTextView);
                dlgBuilder.setCustomTitle(titleLayout);


                dlgBuilder.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int eYear = expiryDate.getYear();
                        int eMonth = expiryDate.getMonth();
                        int eDay = expiryDate.getDayOfMonth();

                        int uYear = useDate.getYear();
                        int uMonth = useDate.getMonth();
                        int uDay = useDate.getDayOfMonth();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                        Date expiry = new GregorianCalendar(eYear, eMonth, eDay).getTime();
                        Date use = new GregorianCalendar(uYear, uMonth, uDay).getTime();

                        Map<String, Object> food = new HashMap<>();
                        food.put("foodName", foodName.getText().toString());
                        food.put("category", categorySelect); //카테고리
                        food.put("expiryDate", sdf.format(expiry)); //유통기한
                        food.put("useDate", sdf.format(use)); //소비기한
                        db.collection(intent.getStringExtra("FridgeName")).document(foodName.getText().toString()).set(food);
                        Toast.makeText(InFridgeActivity.this, "식재료를 추가하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                dlgBuilder.setNegativeButton("취소", null);
                dlgBuilder.show();
            }
        });
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(InFridgeActivity.this, FoodCameraActivity.class);
                myIntent.putExtra("FridgeName", intent.getStringExtra("FridgeName"));
                startActivity(myIntent);
            }
        });
        fab_in_fridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFabOpen) {
                    fabAdd.show();
                    fabCamera.show();
                    isFabOpen = true;
                } else {
                    fabAdd.hide();
                    fabCamera.hide();
                    isFabOpen = false;
                }
            }
        });


        gridView.setAdapter(adapter);


        EventChangeListener();

        // 길게 누르면 식재료 삭제
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) { //롱클릭 하면
                View dialogView = getLayoutInflater().inflate(R.layout.delete_dialog, null);
                TextView textView = dialogView.findViewById(R.id.textView9);
                textView.setText("삭제하시겠습니까?");
                Button cancelButton = dialogView.findViewById(R.id.noButton);
                Button confirmButton = dialogView.findViewById(R.id.yesButton);

                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(InFridgeActivity.this);
                dlgBuilder.setView(dialogView);
                final AlertDialog dialog = dlgBuilder.create();
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss(); // 다이얼로그 닫기
                    }
                });
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 삭제 처리 로직
                        db.collection(intent.getStringExtra("FridgeName"))
                                .whereEqualTo("foodName", list.get(position).getFoodName())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                document.getReference().delete();
                                            }
                                        } else {
                                            Log.d(TAG, "Error deleting document: ", task.getException());
                                        }
                                    }
                                });
                        list.remove(position);  //해당 포지션의 항목 삭제
                        adapter.notifyDataSetChanged(); //화면에 적용
                        Toast.makeText(InFridgeActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // 다이얼로그 닫기
                    }
                });
                // 다이얼로그 투명
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                return false;
            }
        });


        // 클릭하면 식재료 정보 조회
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dia3 = (View) View.inflate(InFridgeActivity.this, R.layout.foodinfo_dialog, null);
                food = (TextView) dia3.findViewById(R.id.food);
                food2 = (TextView) dia3.findViewById(R.id.food2);
                foodCategory = (TextView) dia3.findViewById(R.id.foodCategory);
                foodExpiryDate = (TextView) dia3.findViewById(R.id.foodExpiryDate);
                foodUseDate = (TextView) dia3.findViewById(R.id.foodUseDate);
                cal = (TextView) dia3.findViewById(R.id.cal);
                carb = (TextView) dia3.findViewById(R.id.carb);
                fat = (TextView) dia3.findViewById(R.id.fat);
                protein = (TextView) dia3.findViewById(R.id.protein);
                storage = (TextView) dia3.findViewById(R.id.storage);

                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(InFridgeActivity.this);
                dlgBuilder.setView(dia3);

                LinearLayout titleLayout = new LinearLayout(InFridgeActivity.this);
                titleLayout.setOrientation(LinearLayout.VERTICAL);
                titleLayout.setPadding(0, 30, 0, 0);

                TextView titleTextView = new TextView(InFridgeActivity.this);
                titleTextView.setText("식재료 정보");
                titleTextView.setGravity(Gravity.CENTER);
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                titleTextView.setTypeface(null, Typeface.BOLD);

                titleLayout.addView(titleTextView);
                dlgBuilder.setCustomTitle(titleLayout);

                db.collection("FoodInfo").document(list.get(position).getFoodName())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        food.setText(document.get("food").toString());
                                        cal.setText("칼로리: " + document.get("cal").toString());
                                        carb.setText("탄수화물: " + document.get("carb").toString());
                                        fat.setText("지방: " + document.get("fat").toString());
                                        protein.setText("단백질: " + document.get("protein").toString());
                                        storage.setText("<보관 방법>\n" + document.get("storage").toString());
                                    }
                                }
                            }
                        });

                db.collection(intent.getStringExtra("FridgeName"))
                        .document(list.get(position).getFoodName())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        food2.setText(document.get("foodName").toString());
                                        foodCategory.setText("카테고리: " + document.get("category").toString());
                                        foodExpiryDate.setText("유통기한: " + document.get("expiryDate").toString());
                                        foodUseDate.setText("소비기한: " + document.get("useDate").toString());
                                    }
                                }
                            }
                        });

                dlgBuilder.setPositiveButton("확인", null);
                dlgBuilder.show();

            }
        });
    }
    private void EventChangeListener() {
        String fridgeName = intent.getStringExtra("FridgeName");
        if (fridgeName == null) {
            Log.e("FridgeName", "FridgeName is null");
            return;
        }
        db.collection(fridgeName)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                GridviewFood gridviewFood = new GridviewFood(dc.getDocument().getString("foodName"),dc.getDocument().getString("expiryDate"),
                                        dc.getDocument().getString("useDate"),dc.getDocument().getString("category"));
                                list.add(gridviewFood);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    //옵션 메뉴
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        return true;
    } */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_recipe) {
            // 레시피 액티비티를 실행하는 코드를 추가
            Intent intent = new Intent(InFridgeActivity.this, RecipeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // 설정 액티비티를 실행하는 코드를 추가
            Toast.makeText(getApplicationContext(), "세팅", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
