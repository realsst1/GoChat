package com.example.shreyesh.gochat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {


    private List<Messages> messagesList;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        String message = messagesList.get(position).getMessage();
        holder.setMessageText(message);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView messageImage;
        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            messageText = (TextView) view.findViewById(R.id.messageSingleText);
            messageImage = (CircleImageView) view.findViewById(R.id.messageSingleImage);

        }

        public void setMessageText(String message) {
            messageText.setText(message);
        }
    }


}
