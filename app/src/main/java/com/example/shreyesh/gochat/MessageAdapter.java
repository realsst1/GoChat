package com.example.shreyesh.gochat;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.text.DateFormat.getDateTimeInstance;

public class MessageAdapter extends RecyclerView.Adapter {


    private List<Messages> messagesList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }


    @Override
    public int getItemViewType(int position) {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        Messages messages = (Messages) messagesList.get(position);
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        String fromUserID = messagesList.get(position).getFrom();
        if (fromUserID.equals(currentUserID)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
            return new RecivedViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout_current, parent, false);
            return new SentViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        String message = messagesList.get(position).getMessage();
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        String fromUserID = messagesList.get(position).getFrom();
        final Long time = messagesList.get(position).getTime();

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((RecivedViewHolder) holder).setMessageText(message);
                databaseReference.child("users").child(fromUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String image = dataSnapshot.child("thumbnail").getValue().toString();
                        ((RecivedViewHolder) holder).setMessageImage(image);
                        ((RecivedViewHolder) holder).setMessageTime(time);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentViewHolder) holder).setMessageSent(message);
                ((SentViewHolder) holder).setMessageTime(time);
                break;
        }

    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class RecivedViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, messageTime;
        public CircleImageView messageImage;
        View view;

        public RecivedViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            messageText = (TextView) view.findViewById(R.id.messageSingleText);
            messageImage = (CircleImageView) view.findViewById(R.id.messageSingleImage);
            messageTime = (TextView) view.findViewById(R.id.messageSingleTimestamp);
        }

        public void setMessageText(String message) {
            messageText.setText(message);
        }

        public void setMessageImage(String image) {
            Picasso.get().load(image).placeholder(R.drawable.avatarplaceholder).into(messageImage);
        }

        public void setMessageTime(Long time) {
            Date date = new Date(time);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
            messageTime.setText(simpleDateFormat.format(date));
        }

        /*public  String getTimeDate(long timeStamp){
            try{
                DateFormat dateFormat = getDateTimeInstance();
                Date netDate = (new Date(timeStamp));
                return dateFormat.format(netDate);
            } catch(Exception e) {
                return "date";
            }
        }*/
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView messageSent, messageTime;

        public SentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            messageSent = (TextView) view.findViewById(R.id.messageSingleTextCurrent);
            messageTime = (TextView) view.findViewById(R.id.messageSingleTextCurrentTime);
        }

        public void setMessageSent(String text) {
            messageSent.setText(text);
        }

        public void setMessageTime(Long time) {
            Date date = new Date(time);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
            messageTime.setText(simpleDateFormat.format(date));
        }
    }

}
