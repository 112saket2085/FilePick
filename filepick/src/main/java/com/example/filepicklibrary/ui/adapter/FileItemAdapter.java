package com.example.filepicklibrary.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filepicklibrary.R;
import com.example.filepicklibrary.app.FilePickConstants;
import com.example.filepicklibrary.model.FileItemModel;
import java.util.List;

/**
 * Created by SAKET on 29/03/2020
 */
public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.ViewHolder> {

    private Context context;
    private List<FileItemModel> fileItemModelList;
    private OnBottomSheetItemClickListener listener;

    public FileItemAdapter(List<FileItemModel> fileItemModelList, OnBottomSheetItemClickListener listener) {
        this.fileItemModelList=fileItemModelList;
        this.listener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context=parent.getContext();
        View view= LayoutInflater.from(context).inflate(R.layout.item_file_picker,(ViewGroup) null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final FileItemModel fileItemModel=fileItemModelList.get(position);
        holder.imageView.setImageDrawable(fileItemModel.getImageResource());
        holder.textView.setText(fileItemModel.getIntent().getStringExtra(FilePickConstants.INTENT_FILE_TEXT));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null) {
                    listener.onBottomSheetClick(fileItemModel.getIntent());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return fileItemModelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);
        }

        private void initView(View view) {
            imageView = view.findViewById(R.id.image_view_icon);
            textView = view.findViewById(R.id.text_view_icon_text);
        }

    }

    public interface OnBottomSheetItemClickListener {
        void onBottomSheetClick(Intent intent);
    }

}
