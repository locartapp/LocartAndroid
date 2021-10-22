package com.locart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.locart.Utils.Constants;
import com.locart.model.ProfileClass;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SwipeAdapter extends RecyclerView.Adapter<SwipeAdapter.ViewHolder> {

    ArrayList<ProfileClass> arrayProfileClasses;
    Context context;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    public SwipeAdapter(ArrayList<ProfileClass> arrayProfileClasses, Context context) {
        this.arrayProfileClasses = arrayProfileClasses;
        this.context = context;
    }

    @NonNull
    @Override
    public SwipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.swipe_item, viewGroup, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new ViewHolder(view, context, arrayProfileClasses);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeAdapter.ViewHolder viewHolder, int i) {
        /* show profile from profile class */
        ProfileClass userClass = arrayProfileClasses.get(i);
        String nexus = userClass.getUser_nexus();
        String job = userClass.getUser_job();
        String city = userClass.getUser_city();
        String education = userClass.getUser_education();
        String about = userClass.getUser_about();
        String company = userClass.getUser_company();
        String website = userClass.getUser_website();
        String lookingFor = userClass.getLooking_for();
        String donateHow = userClass.getUser_donate_how();
        String donate = userClass.getUser_donate();


        if (userClass.getUser_username().equals("") || userClass.getUser_username().isEmpty()) {
            viewHolder.textViewName.setText(userClass.getName());
        } else {
            viewHolder.textViewName.setText(userClass.getName());
            viewHolder.textViewUsername.setText(String.format(" @%s", userClass.getUser_username()));

        }

        if (nexus != null && !nexus.isEmpty()) {
            viewHolder.textViewNexus.setText(String.format("Nexus: %s", userClass.getUser_nexus()));

        } else {
            viewHolder.textViewNexus.setVisibility(View.GONE);
        }

        if (job != null && company != null && !job.equals("") && !company.equals("")) {
            viewHolder.textViewJob.setText(String.format("%s at %s", job, company));
        } else if (job != null && company != null && job.equals("") && !company.equals("")) {
            viewHolder.textViewJob.setText(company);
        } else if (job != null && company != null && !job.equals("") && company.equals("")) {
            viewHolder.textViewJob.setText(job);
        } else {
            viewHolder.textViewJob.setVisibility(View.GONE);
        }
        if (education != null && !education.equals("")) {
            viewHolder.textViewEducation.setText(education);
        } else {
            viewHolder.textViewEducation.setVisibility(View.GONE);
        }
        if (city != null && !city.equals("")) {
            viewHolder.textViewCity.setText(city);
        } else {
            viewHolder.textViewCity.setVisibility(View.GONE);
        }
        if (website != null && !website.equals("")) {
            viewHolder.textViewWebsite.setText(website);
        } else {
            viewHolder.textViewWebsite.setVisibility(View.GONE);
        }
        if (about != null && !about.equals("")) {
            viewHolder.textViewAbout.setText(about);
        } else {
            viewHolder.textViewAbout.setVisibility(View.GONE);
        }

        if (lookingFor != null && !lookingFor.equals("None")) {
            viewHolder.textViewLookingFor.setText(String.format("Looking For: %s", lookingFor));
        } else {
            viewHolder.textViewLookingFor.setVisibility(View.GONE);
        }
        if (donateHow != null && !donateHow.isEmpty() && !donate.equals("None")) {
            viewHolder.textViewDonate.setText(String.format("%s: %s", donate, donateHow));
        } else {
            viewHolder.textViewDonate.setVisibility(View.GONE);
        }


        /* get image url then load to view */
        Glide.with(context.getApplicationContext())
                .load(userClass.getZ_image())
                .placeholder(R.drawable.no_image)
                .into(viewHolder.imageViewImage);
    }

    @Override
    public int getItemCount() {
        return arrayProfileClasses.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewNexus, textViewJob, textViewCity, textViewEducation,
                textViewWebsite, textViewAbout, textViewShareAccount, textViewBlockAccount,
                textViewMuteAccount, textViewUsername, textViewLookingFor, textViewDonate;

        ImageView imageViewImage;
        ArrayList<ProfileClass> intentUserClass;
        Context context;

        public ViewHolder(@NonNull View itemView, Context context, ArrayList<ProfileClass> intentUserClass) {
            super(itemView);
            this.intentUserClass = intentUserClass;
            this.context = context;

            textViewName = itemView.findViewById(R.id.name);
            textViewUsername = itemView.findViewById(R.id.Username);
            textViewCity = itemView.findViewById(R.id.city);
            textViewNexus = itemView.findViewById(R.id.nexus);
            textViewJob = itemView.findViewById(R.id.job);
            textViewEducation = itemView.findViewById(R.id.education);
            textViewWebsite = itemView.findViewById(R.id.website);
            textViewAbout = itemView.findViewById(R.id.about);
            textViewBlockAccount = itemView.findViewById(R.id.tvBlockAccount);
            textViewShareAccount = itemView.findViewById(R.id.tvShareAccount);
            textViewMuteAccount = itemView.findViewById(R.id.tvMuteAccount);
            imageViewImage = itemView.findViewById(R.id.imageViewImage);
            textViewLookingFor = itemView.findViewById(R.id.looking_for);
            textViewDonate = itemView.findViewById(R.id.donate);

            textViewBlockAccount.setOnClickListener(v -> {
                //logic to block user
//                Toast.makeText(itemView.getContext(), "Block user", Toast.LENGTH_SHORT).show();
                int position = getAdapterPosition();
                ProfileClass intentProfileClass = this.intentUserClass.get(position);
                String profileUserId = intentProfileClass.getUser_id();
                String profileName = intentProfileClass.getName();

                Map<String, Object> mapBlock = new HashMap<>();
                mapBlock.put("user_blocked", Timestamp.now());
                mapBlock.put("user_blocks", profileUserId);
                mapBlock.put("user_super", "no");

                firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                        .document(firebaseUser.getUid())
                        .collection(Constants.BLOCK)
                        .document(profileUserId)
                        .set(mapBlock)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(context.getApplicationContext(),
                                        "You have blocked: " + profileName, Toast.LENGTH_SHORT).show();
                            }
                        });

            });

            textViewShareAccount.setOnClickListener(v -> {
                //logic to share user profile
                int position = getAdapterPosition();
                ProfileClass intentProfileClass = this.intentUserClass.get(position);
                String username = intentProfileClass.getUser_username();
                if (!username.equals("")) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Locart.me/" + username);
                    context.startActivity(sendIntent);
                } else {
                    Toast.makeText(itemView.getContext(), "Username is not set", Toast.LENGTH_SHORT).show();
                }


            });

            textViewMuteAccount.setOnClickListener(v -> {
                //logic to block user
                Toast.makeText(itemView.getContext(), "Mute Coming Soon", Toast.LENGTH_SHORT).show();
            });


        }


    }

}

