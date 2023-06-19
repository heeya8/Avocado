package com.example.avocado;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GridviewFoodAdapter extends BaseAdapter {
    ArrayList<GridviewFood> items = new ArrayList<GridviewFood>();
    Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Integer[] iconId = {
            R.drawable.fruite, R.drawable.vegetable, R.drawable.meat,
            R.drawable.marine, R.drawable.milk, R.drawable.etc
    };

    public GridviewFoodAdapter(ArrayList<GridviewFood> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        GridviewFood gridviewFood = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gridview_item, parent, false);
        }

        TextView gridFoodName = convertView.findViewById(R.id.gridFoodName);
        TextView gridExpiryDate = convertView.findViewById(R.id.gridExpiryDate);
        TextView gridUseDate = convertView.findViewById(R.id.gridUseDate);
        TextView gridCategory = convertView.findViewById(R.id.gridCategory);
        ImageView foodIcon = convertView.findViewById(R.id.foodIcon);

        gridFoodName.setText(gridviewFood.getFoodName());
        gridExpiryDate.setText(gridviewFood.getExpiryDate());
        gridUseDate.setText(gridviewFood.getUseDate());
        gridCategory.setText(gridviewFood.getCategory());

        if (gridviewFood.getCategory().equals("과일")) {
            foodIcon.setImageResource(iconId[0]);
        } else if (gridviewFood.getCategory().equals("채소")) {
            foodIcon.setImageResource(iconId[1]);
        } else if (gridviewFood.getCategory().equals("육류")) {
            foodIcon.setImageResource(iconId[2]);
        } else if (gridviewFood.getCategory().equals("수산물")) {
            foodIcon.setImageResource(iconId[3]);
        } else if (gridviewFood.getCategory().equals("유제품")) {
            foodIcon.setImageResource(iconId[4]);
        } else {
            foodIcon.setImageResource(iconId[5]);
        }

        // 현재 날짜를 가져옴
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // 유통기한을 Date 형식으로 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date expiryDate;
        try {
            expiryDate = dateFormat.parse(gridviewFood.getExpiryDate());
        } catch (ParseException e) {
            e.printStackTrace();
            return convertView;
        }

        // 남은 일수 계산
        long diffInMilliseconds = expiryDate.getTime() - currentDate.getTime();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMilliseconds);

        // 유통기한이 3일 미만으로 남은 경우 텍스트 색상 변경
        if (diffInDays < 3) {
            gridFoodName.setTextColor(Color.RED);
        } else {
            int defaultColor = ContextCompat.getColor(convertView.getContext(), android.R.color.black);
            gridFoodName.setTextColor(defaultColor);
        }

        return convertView;
    }
}
