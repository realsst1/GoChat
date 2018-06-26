package com.example.shreyesh.gochat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendList;
    private DatabaseReference databaseReference, userDatabaseReference;
    private View mainView;
    private String currentUserID;
    private FirebaseAuth firebaseAuth;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendList = (RecyclerView) mainView.findViewById(R.id.friendList);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("FriendsData").child(currentUserID);
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.keepSynced(true);
        userDatabaseReference.keepSynced(true);
        friendList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecylcerAdpater = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_list,
                FriendsViewHolder.class,
                databaseReference

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String userID = getRef(position).getKey();
                userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String thumb = dataSnapshot.child("thumbnail").getValue().toString();
                        if (dataSnapshot.hasChild("online")) {
                            Boolean userOnline = (Boolean) dataSnapshot.child("online").getValue();
                            viewHolder.setUserOnline(userOnline);
                        }
                        viewHolder.setName(userName);
                        viewHolder.setImage(thumb);

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                alert.setTitle("Select Option");
                                alert.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        switch (i) {
                                            case 0:
                                                Intent intent = new Intent(getContext(), ProfileActivity.class);
                                                intent.putExtra("from_user_id", userID);
                                                startActivity(intent);
                                                break;
                                            case 1:
                                                Intent chatintent = new Intent(getContext(), ChatActivity.class);
                                                chatintent.putExtra("from_user_id", userID);
                                                chatintent.putExtra("user_name", userName);
                                                startActivity(chatintent);

                                            default:
                                                break;
                                        }
                                    }
                                });

                                alert.show();
                            }
                        });



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        friendList.setAdapter(friendsRecylcerAdpater);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDate(String date) {
            TextView userName = (TextView) view.findViewById(R.id.userSingleStatus);
            userName.setText(date);
        }

        public void setName(String name) {
            TextView userName = (TextView) view.findViewById(R.id.userSingleName);
            userName.setText(name);
        }

        public void setImage(final String image) {
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


        public void setUserOnline(Boolean image) {
            ImageView imageView = (ImageView) view.findViewById(R.id.userSingleOnlineDot);
            if (image == true) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
