package com.locart.Matches;


import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.locart.Users;
import com.locart.Utils.Constants;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

import javax.annotation.Nullable;

import com.locart.R;

public class MatchesFirestore extends FirestoreRecyclerAdapter<MatchesClass, MatchesFirestore.MatchesHolder> {


    private OnItemClickListener listener;
    Context mContext;
    private static final String TAG = "MatchesFireStore";

    public MatchesFirestore(@NonNull FirestoreRecyclerOptions<MatchesClass> options) {
        super(options);

    }

    @Override
    protected void onBindViewHolder(@NonNull final MatchesHolder holder, int position, @NonNull final MatchesClass model) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {

//                        Log.d(TAG, String.valueOf(queryDocumentSnapshots.size()));

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
//                            Log.d(TAG, Objects.requireNonNull(doc.getDocument().getString("name")));

                            Users users = doc.getDocument().toObject(Users.class);
                            if (users.getUser_id() != null && users.getUser_id().equals(model.getUser_matches())) {
                                holder.textViewMatchesName.setText(users.getName());

//                                Log.d(TAG, users.getName());
                                /* get image url then load to view */
                                Glide.with(mContext.getApplicationContext())
                                        .load(users.getZ_image())
                                        .placeholder(R.drawable.no_image)
                                        .into(holder.roundedImageViewMatchesImage);
                            }
                        }
                    }
                });

    }

    @NonNull
    @Override
    public MatchesHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.matches_item, viewGroup, false);
        mContext = viewGroup.getContext();
        return new MatchesHolder(v);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    class MatchesHolder extends RecyclerView.ViewHolder {

        TextView textViewMatchesName;
        RoundedImageView roundedImageViewMatchesImage;

        public MatchesHolder(@NonNull View itemView) {
            super(itemView);

            textViewMatchesName = itemView.findViewById(R.id.matchesName);
            roundedImageViewMatchesImage = itemView.findViewById(R.id.matchesImage);


            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

}
