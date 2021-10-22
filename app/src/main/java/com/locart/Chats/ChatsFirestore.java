package com.locart.Chats;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.locart.Message.MessageClass;
import com.locart.model.ProfileClass;
import com.locart.R;
import com.locart.Utils.Constants;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Date;

public class ChatsFirestore extends FirestoreRecyclerAdapter<MessageClass, ChatsFirestore.ChatHolder> {


    private OnItemClickListener listener;
    Context mContext;
    String message_type, user_status, sCheckOfflineAfter24Hours = "no";
    Date online;


    public ChatsFirestore(@NonNull FirestoreRecyclerOptions<MessageClass> options, Context mContext) {
        super(options);
        this.mContext = mContext;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ChatHolder holder, int position, @NonNull final MessageClass model) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            ProfileClass profileClass = doc.getDocument().toObject(ProfileClass.class);
                            if (profileClass.getUser_id() != null && profileClass.getUser_id().equals(model.getUser_receiver())) {
                                holder.textViewChatsItemChatsName.setText(profileClass.getName());
                                /* get image url then load to view */
                                Glide.with(mContext.getApplicationContext())
                                        .load(profileClass.getZ_image())
                                        .placeholder(R.drawable.no_image)
                                        .into(holder.roundedImageViewChatsItemChatsImage);

                                user_status = profileClass.getUser_status();
                                online = doc.getDocument().getDate("user_online");

                                assert online != null;
                                long SavedMilliseconds = 0L;
                                SavedMilliseconds = online.getTime();

                                /* Check time elapsed */
                                if (System.currentTimeMillis() >= SavedMilliseconds + 24 * 60 * 60 * 1000) {
                                    /* time has elapsed */
                                    sCheckOfflineAfter24Hours = "yes";
                                }

                                /* check if user is a Shop and set ring as silver else check user status */
                                if (profileClass.getUser_sex()!= null && profileClass.getUser_sex().equals("Shop")){
                                    holder.roundedImageViewChatsItemChatsImage.setBorderColorStart(Color.TRANSPARENT);
                                    holder.roundedImageViewChatsItemChatsImage.setBorderColorEnd(R.color.argent);
                                }else {
                                    if (user_status != null && user_status.equals("online")) {
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorStart(Color.GREEN);
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorEnd(R.color.avacado);
                                    } else if (sCheckOfflineAfter24Hours.equals("yes")) {
                                        /* 24hrs of inactivity */
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorStart(R.color.amber);
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorEnd(Color.YELLOW);
                                    }
                                    else if (user_status != null && user_status.equals("offline")) {
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorStart(R.color.crimson);
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorEnd(Color.RED);
                                    } else if (user_status != null && user_status.equals("paused")) {
                                        /* app is in background */
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorStart(Color.BLUE);
                                        holder.roundedImageViewChatsItemChatsImage.setBorderColorEnd(R.color.azure);
                                    }
                                }


                            }
                        }
                    }

                });
        holder.textViewChatsItemChatsDate.setText(model.getChat_time());

        message_type = model.getMessage_type();

        switch (message_type) {
            case "TEXT":
                holder.textViewChatsItemChatsMessage.setText(model.getUser_message());
                break;
            case "IMAGE":
                holder.textViewChatsItemChatsMessage.setVisibility(View.GONE);
                holder.photoIcon.setVisibility(View.VISIBLE);
                holder.textViewImageIcon.setVisibility(View.VISIBLE);
                break;
            case "AUDIO":
                holder.textViewChatsItemChatsMessage.setVisibility(View.GONE);
                holder.voiceNoteIcon.setVisibility(View.VISIBLE);
                break;
        }

    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chats_item, viewGroup, false);
        return new ChatHolder(v);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    class ChatHolder extends RecyclerView.ViewHolder {

        TextView textViewChatsItemChatsName, textViewChatsItemChatsMessage, textViewChatsItemChatsDate,
                textViewImageIcon;
        CircularImageView roundedImageViewChatsItemChatsImage;
        ImageView voiceNoteIcon, photoIcon;


        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            textViewChatsItemChatsName = itemView.findViewById(R.id.textViewChatsItemChatsName);
            roundedImageViewChatsItemChatsImage = itemView.findViewById(R.id.roundedImageViewChatsItemChatsImage);
            textViewChatsItemChatsMessage = itemView.findViewById(R.id.textViewChatsItemChatsMessage);
            textViewChatsItemChatsDate = itemView.findViewById(R.id.textViewChatsItemChatsDate);
            voiceNoteIcon = itemView.findViewById(R.id.voice_note_icon);
            photoIcon = itemView.findViewById(R.id.image_view_icon);
            textViewImageIcon = itemView.findViewById(R.id.textViewImageIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

}
