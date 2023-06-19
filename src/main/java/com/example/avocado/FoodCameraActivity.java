package com.example.avocado;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.avocado.ml.Model;
import com.example.avocado.ml.Model2;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoodCameraActivity extends AppCompatActivity {

    Button camera, gallery, camera_add;
    ImageView imageView;
    TextView result;
    int imageSize = 32;
    Spinner categorySpinner;
    String[] categoryItems = {"과일","채소","육류","수산물","유제품","기타"};
    String categorySelect;
    DatePicker expiryDate, useDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide(); //타이틀 바 숨겨주는 코드

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_camera);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        camera_add = findViewById(R.id.camera_add);
        expiryDate = (DatePicker) findViewById(R.id.expiryDate);
        useDate = (DatePicker) findViewById(R.id.useDate);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        Intent nameIntent = getIntent();
        //name.setText(nameIntent.getStringExtra("FridgeName"));

        categorySpinner = (Spinner) findViewById(R.id.categorySpinner2);

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

        camera_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                food.put("foodName", result.getText().toString());
                food.put("category", categorySelect); //카테고리
                food.put("expiryDate", sdf.format(expiry)); //유통기한
                food.put("useDate", sdf.format(use)); //소비기한
                db.collection(nameIntent.getStringExtra("FridgeName")).document(result.getText().toString()).set(food);
                Toast.makeText(FoodCameraActivity.this, "식재료를 추가하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(FoodCameraActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,3);
                }else{
                    ActivityCompat.requestPermissions(FoodCameraActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
    }

    public void classifyImage(Bitmap image){
        try {
            Model2 model = Model2.newInstance(getApplicationContext());
            // 입력 데이터 생성.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0,0, image.getWidth(), image.getHeight());
            int pixel = 0;
            // 픽셀마다 RGB 값을 추출하고, 이 값들을 개별적으로 바이트 버퍼에 추가.
            for(int i=0; i<imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);
            // 모델의 예측을 실행하고 결과를 가져옴.
            Model2.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"감자","김치","당근","돼지고기","무",
                    "버섯","사과","시금치","아보카도","양파","오이","콩나물","파프리카"};
            result.setText(classes[maxPos]);
            // 더 이상 쓰지 않는 모델 리소스 해제.
            model.close();
        } catch (IOException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }else{
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image= MediaStore.Images.Media.getBitmap(this.getContentResolver(),dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}