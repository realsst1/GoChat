package com.example.shreyesh.gochat;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView chatList;
    private DatabaseReference messageReference;
    private DatabaseReference userReference;
    private DatabaseReference convReference;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private View view;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        //initialize view
        chatList = (RecyclerView) view.findViewById(R.id.chatList);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseAuth = FirebaseAuth.getInstance();

        //database initialize
        convReference = FirebaseDatabase.getInstance().getReference().child("chats").child(currentUserID);
        userReference = FirebaseDatabase.getInstance().getReference().child("users");
        messageReference = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUserID);

        //offline sync
        convReference.keepSynced(true);
        userReference.keepSynced(true);
        messageReference.keepSynced(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatList.setLayoutManager(linearLayoutManager);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = convReference.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.users_single_list,
                ConvViewHolder.class,
                query

        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder viewHolder, final Conv model, int position) {

                final String userID = getRef(position).getKey();

                Query lastMessage = messageReference.child(userID).limitToLast(1);
                lastMessage.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        viewHolder.setMessage(data, model.isSeen());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                userReference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("thumbnail").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.setUserName(userName);
                        viewHolder.setUserImage(image);

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("from_user_id", userID);
                                intent.putExtra("user_name", userName);
                                startActivity(intent);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        chatList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class ConvViewHolder extends RecyclerView.ViewHolder {
        View view;

        public ConvViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setMessage(String status, boolean isSeen) {
            TextView userStatus = (TextView) view.findViewById(R.id.userSingleStatus);
            userStatus.setText(status);
            if (!isSeen) {
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.BOLD);
            } else {
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setUserName(String name) {
            TextView userName = (TextView) view.findViewById(R.id.userSingleName);
            userName.setText(name);
        }

        public void setUserImage(final String image) {
            final CircleImageView circleImageView = (CircleImageView) view.findViewById(R.id.userSingleImage);
            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatarplaceholder).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(image).placeholder(R.drawable.avatarplaceholder).into(circleImageView);
                }
            });
        }

        public void setUserOnline(String userOnline) {
            ImageView onlineDot = (ImageView) view.findViewById(R.id.userSingleOnlineDot);
            if (userOnline.equals("online")) {
                onlineDot.setVisibility(View.VISIBLE);
            } else {
                onlineDot.setVisibility(View.INVISIBLE);
            }
        }

    }


}
