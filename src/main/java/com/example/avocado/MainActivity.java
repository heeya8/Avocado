package com.example.avocado;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Fridges> list;
    RecyclerView.Adapter adapter;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private FloatingActionButton fab_main;
    View dialogView;
    EditText fridgeName;

    private ExpiryDateNotifier expiryDateNotifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ac = getSupportActionBar();
      //  ac.setTitle("냉장고 목록"); //상단바 이름 설정
        ac.hide(); //상단바 숨기기
        
        //expiryDateNotifier = new ExpiryDateNotifier(this);

        // 알람 기능 실행
        //expiryDateNotifier.checkExpiryDates();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fab_main = (FloatingActionButton) findViewById(R.id.fab_main);

        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View v) {
                showCustomDialog();
            }
        });

        adapter = new FridgesAdapter(list, getApplicationContext());
        recyclerView.setAdapter(adapter);
        EventChangeListener();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    //스와이프 하여 아이템 삭제
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();

            //왼쪽으로 스와이프 했을 시
            if (direction == ItemTouchHelper.LEFT) {
                //db 데이터 삭제
                //RefrigeratorName의 하위 document 삭제
                db.collection("RefrigeratorName")
                        .whereEqualTo("fridgeName", list.get(position).getFridgeType())
                        .whereEqualTo("owner", firebaseAuth.getUid())
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
                //냉장고 collection 삭제
                db.collection(list.get(position).getFridgeType()).document(firebaseUser.getUid()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });
                //RecyclerView의 아이템 삭제
                list.remove(position);
                adapter.notifyItemRemoved(position);
            }
        }
    };

    private void EventChangeListener() {
        db.collection("RefrigeratorName").whereEqualTo("owner", firebaseAuth.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Fridges fridges = new Fridges(dc.getDocument().getString("fridgeName"),dc.getDocument().getString("owner"));
                                list.add(fridges);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    //옵션 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                // "Settings" 메뉴 항목을 선택했을 때의 동작 처리
                return true;
            case R.id.action_recipe:
                Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showCustomDialog() {

        // 다이얼로그 빌더 생성
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);

        // XML 레이아웃 파일을 인플레이션하여 뷰 생성
        View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.refrigerator_add, null);

        // 다이얼로그 빌더에 뷰 설정
        dialogBuilder.setView(dialogView);

        // 다이얼로그 생성
        final AlertDialog dialog = dialogBuilder.create();

        //배경 투명하게 만들기
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        TextView textView = dialogView.findViewById(R.id.textView);
        //textView.setTextColor(Color.GREEN); // 글자를 투명하게 설정

        // XML 레이아웃 파일의 뷰 요소들 찾기
        EditText refrigeratorName = dialogView.findViewById(R.id.refrigerator_name);
        Button addButton = dialogView.findViewById(R.id.ok_btn);
        addButton.setTextColor(Color.BLACK);
        Button cancelButton = dialogView.findViewById(R.id.cancle_btn);
        cancelButton.setTextColor(Color.BLACK);

        // "추가" 버튼 클릭 이벤트 리스너
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 추가 버튼 클릭 시 동작 구현
                String name = refrigeratorName.getText().toString();
                fridgeName = (EditText) dialogView.findViewById(R.id.refrigerator_name);
                // 냉장고 종류 기본 세팅
                Map<String, Object> fridge = new HashMap<>();
                fridge.put("owner", firebaseUser.getUid());
                fridge.put("fridgeName", fridgeName.getText().toString());
                // 냉장고 내부 기본 세팅

                if (name != null && !name.isEmpty()) {
                    // 냉장고 이름이 정상적으로 입력되었을 때의 동작 수행
                    Map<String, Object> food = new HashMap<>();
                    food.put("foodName", "사과"); // 예시 식재료
                    food.put("expiryDate", "2023-05-14"); //유통기한
                    food.put("useDate", "2023-05-14"); //소비기한
                    food.put("category","과일"); // 카테고리
                    db.collection("RefrigeratorName").document(fridgeName.getText().toString()).set(fridge);
                    db.collection(fridgeName.getText().toString()).document("사과").set(food);
                    //db.collection(fridgeName.getText().toString()).document();
                    Toast.makeText(MainActivity.this, "냉장고를 추가하였습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // 다이얼로그 닫기
                    // ...
                } else {
                    // 냉장고 이름이 유효하지 않을 때의 처리
                    Toast.makeText(MainActivity.this, "냉장고 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // "취소" 버튼 클릭 이벤트 리스너
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // 다이얼로그 닫기
            }
        });

        // 다이얼로그 표시
        dialog.show();
    }

}
