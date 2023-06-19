package com.example.avocado;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FridgesAdapter extends RecyclerView.Adapter<FridgesAdapter.FridgesViewHolder> {
    FridgeNames fridgeNames;
    ArrayList<Fridges> list;
    private Context context;

    public FridgesAdapter(ArrayList<Fridges> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public FridgesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout,parent,false);
        return new FridgesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FridgesViewHolder holder, int position) {
        holder.fridgeType.setText(list.get(position).getFridgeType());
        holder.owner.setText(list.get(position).getOwner());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class FridgesViewHolder extends RecyclerView.ViewHolder{
        TextView fridgeType;
        TextView owner;

        public FridgesViewHolder(@NonNull View itemView) {
            super(itemView);
            fridgeType = (TextView) itemView.findViewById(R.id.fridgeType);
            owner = (TextView) itemView.findViewById(R.id.owner);

            fridgeType.setClickable(true);
            fridgeType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        Intent intent = new Intent(context,InFridgeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("FridgeName",fridgeType.getText().toString());
                        context.startActivity(intent);
                    }
                    //fridgeNames = new FridgeNames(); // 냉장고 이름 공유
                    //fridgeNames.setFridgeName(fridgeType.getText().toString());
                    //System.out.print("\n\n\n\n"+fridgeNames+"\n\n\n\n");
                }
            });

        }
    }
}
