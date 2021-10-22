package com.locart.Message;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.locart.Chats.ChatsClass;
import com.locart.R;
import com.locart.views.LocartSeekbar;
import com.bumptech.glide.Glide;
import com.github.abdularis.buttonprogress.DownloadButtonProgress;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    ArrayList<ChatsClass> arrayUsersClass;
    Context context;

    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String message_type, currentUser;
    private static final String TAG = "MessageAdapter";
    /* Initializing MediaPlayer */
    MediaPlayer player = new MediaPlayer();
    private boolean isPlaying = false;
    Handler seekHandler = new Handler();
    Runnable run;


    public MessageAdapter(ArrayList<ChatsClass> arrayUsersClass, Context context) {
        this.arrayUsersClass = arrayUsersClass;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (i) {
            case MSG_TYPE_RIGHT:
                 view = inflater.inflate(R.layout.message_item_right, viewGroup, false);
                break;

            case MSG_TYPE_LEFT:
                view = inflater.inflate(R.layout.message_item_left, viewGroup, false);
                break;
            default:
                break;
        }

         return new ViewHolder(view, context, arrayUsersClass);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        ChatsClass userClass = arrayUsersClass.get(i);
        try{
            ChatsClass userClass2 = arrayUsersClass.get(i - 1);

            Timestamp timestamp = new Timestamp(userClass.getChat_date_sent());
            Timestamp timestamp2 = new Timestamp(userClass2.getChat_date_sent());

            String date = DateFormat.getDateInstance().format(timestamp.toDate());
            String date2 = DateFormat.getDateInstance().format(timestamp2.toDate());
//            Log.d(TAG, "Date : " + date);
//            Log.d(TAG, "Date 2: " + date2);

            /* check for different date and populate */
            if (i > 0) {
                if (date.equals(date2)) {
                    viewHolder.tvDate.setVisibility(View.GONE);
                } else {
                    viewHolder.tvDate.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.tvDate.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }

        message_type = userClass.getMessage_type();

        currentUser = firebaseAuth.getCurrentUser().getUid();

//        Log.d(TAG, currentUser);
        /* check for last message and show eye or tick */
        if (i == (getItemCount() - 1)) {
            // last message in view
            /* show tick for only current user that is the sender */
            if (currentUser.equals(userClass.getChat_sender())) {
                if (userClass.getChat_seen_chat().equals("no")) {
                    viewHolder.imageViewChatSent.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.imageViewChatSeen.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.imageViewChatSeen.setVisibility(View.INVISIBLE);
                viewHolder.imageViewChatSent.setVisibility(View.INVISIBLE);
            }
        }


        /* check if it is text content */
        switch (message_type) {
            case "TEXT":
                viewHolder.textViewChatMessage.setText(userClass.getChat_message());
                viewHolder.tvTimestamp.setText(userClass.getChat_time());
                viewHolder.messageImage.setVisibility(View.GONE);
                viewHolder.relativeLayoutVoiceNote.setVisibility(View.GONE);
                Timestamp timestamp = new Timestamp(userClass.getChat_date_sent());
                String day = (new SimpleDateFormat("EEEE", Locale.getDefault())).format(timestamp.toDate());
//                Log.d(TAG, "Day of The Week :"+ day);

                viewHolder.tvDate.setText(day);

                break;
            case "IMAGE":
                /* check if it is image content */
                viewHolder.textViewChatMessage.setVisibility(View.GONE);
                viewHolder.relativeLayoutVoiceNote.setVisibility(View.GONE);
                viewHolder.tvTimestamp.setText(userClass.getChat_time());
                Timestamp timestamp2 = new Timestamp(userClass.getChat_date_sent());
                String day2 = (new SimpleDateFormat("EEEE", Locale.getDefault())).format(timestamp2.toDate());
                viewHolder.tvDate.setText(day2);

                Glide.with(context.getApplicationContext())
                        .load(userClass.getChat_message())
                        .fitCenter()
                        .placeholder(R.drawable.no_image)
                        .into(viewHolder.messageImage);

                break;
            case "AUDIO":
                /* check if it is audio content */
                viewHolder.textViewChatMessage.setVisibility(View.GONE);
                viewHolder.messageImage.setVisibility(View.GONE);
                viewHolder.tvTimestamp.setText(userClass.getChat_time());
                String audioUrl = userClass.getChat_message();
                Timestamp timestamp3 = new Timestamp(userClass.getChat_date_sent());
                String day3 = (new SimpleDateFormat("EEEE", Locale.getDefault())).format(timestamp3.toDate());
                viewHolder.tvDate.setText(day3);

                /* get audio then load to view */
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    player.setDataSource(audioUrl);
                    player.prepareAsync();
                    player.setOnPreparedListener(mediaPlayer -> {

                        viewHolder.tvTimeDuration.setText("0:00/" + calculateDuration(player.getDuration()));

                        /* use this to set seek bar length and then update every second */
                        viewHolder.voiceSeekBar.setMax(mediaPlayer.getDuration());
                        viewHolder.voiceSeekBar.setTag(i);
                        viewHolder.voiceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (player != null && fromUser) {
                                    player.seekTo(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });

                    });

                    /* when voice finished playing, show default image view of play */
                    player.setOnCompletionListener(mp -> {
                        viewHolder.voiceNoteImage.setImageResource(R.drawable.ic_play_arrow);
//                    player.start();
                    });

                    viewHolder.voiceNoteImage.setOnClickListener(v -> {
                        /* play/pause audio */
                        if (!isPlaying) {
                            /* play voice note */
                            isPlaying = true;
                            player.start();
                            viewHolder.voiceNoteImage.setImageResource(R.drawable.ic_pause);

                            run = () -> {
                                /* Updating SeekBar every 100 milli seconds */
//                        if (player.getCurrentPosition() != null && viewHolder.voiceSeekBar != null){
//                            viewHolder.voiceSeekBar.setProgress(player.getCurrentPosition());
//                        }
                                viewHolder.voiceSeekBar.setProgress(player.getCurrentPosition());
                                seekHandler.postDelayed(run, 100);
                                /* For Showing time of audio(inside runnable) */
                                int miliSeconds = player.getCurrentPosition();
                                if (miliSeconds != 0) {
                                    /* if audio is playing, showing current time; */
                                    long minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds);
                                    long seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds);
                                    if (minutes == 0) {
                                        viewHolder.tvTimeDuration.setText("0:" + seconds + "/" + calculateDuration(player.getDuration()));
                                    } else {
                                        if (seconds >= 60) {
                                            long sec = seconds - (minutes * 60);
                                            viewHolder.tvTimeDuration.setText(minutes + ":" + sec + "/" + calculateDuration(player.getDuration()));
                                        }
                                    }
                                } else {
                                    /* Displaying total time if audio not playing */
                                    int totalTime = player.getDuration();
                                    long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
                                    long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
                                    if (minutes == 0) {
                                        viewHolder.tvTimeDuration.setText("0:" + seconds);
                                    } else {
                                        if (seconds >= 60) {
                                            long sec = seconds - (minutes * 60);
                                            viewHolder.tvTimeDuration.setText(minutes + ":" + sec);
                                        }
                                    }
                                }
                            };
                            run.run();

                        } else {
                            /* pause voice note */

                            isPlaying = false;
                            player.pause();
                            viewHolder.voiceNoteImage.setImageResource(R.drawable.ic_play_arrow);
//                    viewHolder.voiceSeekBar.setMax(length);
                        }
                    });


                } catch (Exception e) {
                    Log.d(TAG, "Failed: " + e.getMessage());
                }

                player.setOnErrorListener((mp, what, extra) -> {
                    mp.reset();
                    return false;
                });
                break;
        }

    }

    public void stopPlaying() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
    }

    /* converting time in millis to minutes:second format eg 14:15 min */
    private String calculateDuration(int duration) {
        String finalDuration = "";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        if (minutes == 0) {
            finalDuration = "0:" + seconds;
        } else {
            if (seconds >= 60) {
                long sec = seconds - (minutes * 60);
                finalDuration = minutes + ":" + sec;
            }
        }
        return finalDuration;
    }

    @Override
    public int getItemCount() {
        return arrayUsersClass.size();

    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (arrayUsersClass.get(position).getChat_sender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewChatMessage, tvTimestamp, tvTimeDuration, tvDate;
        ImageView imageViewChatSent, imageViewChatSeen, messageImage, voiceNoteImage;
        LocartSeekbar voiceSeekBar;
        DownloadButtonProgress progressButton;
        RelativeLayout relativeLayoutVoiceNote;
        ArrayList<ChatsClass> intentUserClass = new ArrayList<ChatsClass>();
        Context context;

        public ViewHolder(@NonNull View itemView, Context context, ArrayList<ChatsClass> intentUserClass) {
            super(itemView);
            this.intentUserClass = intentUserClass;
            this.context = context;
            itemView.setOnClickListener(this);

            textViewChatMessage = itemView.findViewById(R.id.textViewChatMessage);
            tvTimestamp = itemView.findViewById(R.id.timestamp);
            tvTimeDuration = itemView.findViewById(R.id.tvDuration);
            tvDate = itemView.findViewById(R.id.header_date);
            messageImage = itemView.findViewById(R.id.imageViewMessageImage);
            imageViewChatSent = itemView.findViewById(R.id.imageViewChatSent);
            imageViewChatSeen = itemView.findViewById(R.id.imageViewChatSeen);
            voiceNoteImage = itemView.findViewById(R.id.voicePlayBtn);
            voiceSeekBar = itemView.findViewById(R.id.voiceSeekBar);
            progressButton = itemView.findViewById(R.id.progressButton);
            relativeLayoutVoiceNote = itemView.findViewById(R.id.relativeLayoutVoiceNote);
        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            ChatsClass intentUsersClass = this.intentUserClass.get(position);

//            Intent intent = new Intent(this.context, ProfileActivity.class);
//            intent.putExtra("user_message", intentUsersClass.getChat_message());

        }

    }


}


