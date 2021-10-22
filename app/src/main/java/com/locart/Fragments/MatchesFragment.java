package com.locart.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.locart.Locart;
import com.locart.Chats.ChatsFirestore;
import com.locart.Message.MessageActivity;
import com.locart.Message.MessageClass;
import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.locart.Matches.MatchesClass;
import com.locart.Matches.MatchesFirestore;

import com.locart.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays Mathces Available to the user in two recyclerView, one for the most recent ones
 * the Bank for the ones with the most recent messages
 */
public class MatchesFragment extends Fragment implements SearchView.OnQueryTextListener {

    private String currentUserID, nexusCount = "0";
    TextView tvNexusCount;
    private View view;

    private TextView mNoMatches, mNoChats;
    private RecyclerView rvMatchLayout, rvChatLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MatchesFirestore adapter;
    private ChatsFirestore chatsFirestore;
    private static final String TAG = "MatchesFragment";

    private boolean allowRefresh = false;
    private Activity mActivity;
    private CallbackMatch callbackMatch;
    MatchFragmentReceiver myMatchReceiver;
    DataProccessor dataProccessor;
    FrameLayout matchLayout;
//        SearchView searchNexus;
    EditText searchBox;


    public MatchesFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_matches, container, false);

        mNoMatches = view.findViewById(R.id.no_matches_layout);
        mNoChats = view.findViewById(R.id.no_chats_layout);
        rvChatLayout = view.findViewById(R.id.recycler_view_chat);
        rvMatchLayout = view.findViewById(R.id.recycler_view_match);
        matchLayout = view.findViewById(R.id.card_layout);
        tvNexusCount = view.findViewById(R.id.nexus_count);
//        searchNexus = view.findViewById(R.id.search_nexus);
        searchBox = view.findViewById(R.id.searchBox);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();
        dataProccessor = new DataProccessor(getActivity());
//        searchNexus.setOnQueryTextListener(this);

        initUi();

        return view;
    }

    private void initUi() {
//        if (mActivity == null) {
//            return;
//        }
        MatchesRecyclerView();
        ChatRecyclerView();

        /* check if new match is available and chat hasn't been sent to before */
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserID)
                .collection(Constants.MATCH)
                .whereEqualTo("user_chat_start", "no")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() == 0) {

//                            Log.d(TAG, " Size: " + String.valueOf(queryDocumentSnapshots.size()));
                            rvMatchLayout.setVisibility(View.GONE);
                            mNoMatches.setVisibility(View.VISIBLE);

                        } else {

//                            Log.d(TAG, " Size 2: " + String.valueOf(queryDocumentSnapshots.size()));
                            rvMatchLayout.setVisibility(View.VISIBLE);
                            mNoMatches.setVisibility(View.GONE);
                        }
                    }
                });

        /* check if chat has been done with match*/
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserID)
                .collection(Constants.CHATS)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() == 0) {

                            rvChatLayout.setVisibility(View.GONE);
                            mNoChats.setVisibility(View.VISIBLE);

                        } else {

                            rvChatLayout.setVisibility(View.VISIBLE);
                            mNoChats.setVisibility(View.GONE);

                        }
                    }
                });

        /* check for nexus count*/
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserID)
                .collection(Constants.MATCH)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() == 0) {
                            Log.d(TAG, " Size: " + String.valueOf(queryDocumentSnapshots.size()));
//                            tvNexusCount.setText(String.format("Nexus %s", nexusCount));
                        } else {

//                            Log.d(TAG, " Size 2: " + String.valueOf(queryDocumentSnapshots.size()));
                            nexusCount = String.valueOf(queryDocumentSnapshots.size());
                            tvNexusCount.setText(String.format("Nexus %s", nexusCount));
                        }
                    }
                });

        /* update nexus count in fire store */
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserID)
                .update(
                        "user_nexus", nexusCount
                ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Nexus count updated to: " + nexusCount);
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // When user changed the Text

//                String text = searchBox.getText().toString();
//                Query query = null;
//
//                if (text.isEmpty()) {
//                    query = db.collection(Constants.FIRE_STORE_COLLECTION)
//                            .document(currentUserID)
//                            .collection(Constants.CHATS)
//                            .orderBy("user_date_sent", Query.Direction.DESCENDING);
//                } else if (!text.isEmpty()) {
//
//                    query = db.collection(Constants.FIRE_STORE_COLLECTION)
//                            .document(currentUserID)
//                            .collection(Constants.CHATS)
//                            .whereEqualTo("chat_message", text)
//                            .orderBy("user_date_sent", Query.Direction.ASCENDING);
//                }
//
//                FirestoreRecyclerOptions<MessageClass> options = new FirestoreRecyclerOptions.Builder<MessageClass>()
//                        .setQuery(query, MessageClass.class)
//                        .build();
//                chatsFirestore.updateOptions(options);
            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.d(TAG, "Search box has changed to: " + s.toString());

                String text = searchBox.getText().toString();
                Query query = null;

                if (text.isEmpty()) {
                    query = db.collection(Constants.FIRE_STORE_COLLECTION)
                            .document(currentUserID)
                            .collection(Constants.CHATS)
                            .orderBy("user_date_sent", Query.Direction.DESCENDING);
                } else {

                    query = db.collection(Constants.CHATS)
//                            .document(currentUserID)
//                            .collection(Constants.CHATS)
                            .whereEqualTo("chat_message", text)
                            .orderBy("user_date_sent", Query.Direction.ASCENDING);

                    Log.d(TAG, "Search box has changed to: " + text);
                }

                FirestoreRecyclerOptions<MessageClass> options = new FirestoreRecyclerOptions.Builder<MessageClass>()
                        .setQuery(query, MessageClass.class)
                        .build();
                chatsFirestore.updateOptions(options);
//                chatsFirestore.startListening();

                Log.d(TAG, "Search box: " + options.toString());

            }
        });

    }

    private void ChatRecyclerView() {
        /* query for chats for user */
        Query query = db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserID)
                .collection(Constants.CHATS)
                .orderBy("user_date_sent", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MessageClass> options = new FirestoreRecyclerOptions.Builder<MessageClass>()
                .setQuery(query, MessageClass.class)
                .build();

        chatsFirestore = new ChatsFirestore(options, getActivity());
        rvChatLayout.setHasFixedSize(true);
        rvChatLayout.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatLayout.setAdapter(chatsFirestore);
        chatsFirestore.startListening();
        chatsFirestore.notifyDataSetChanged();

        chatsFirestore.setOnItemClickListener((documentSnapshot, position) -> {
            final MessageClass messageClass = documentSnapshot.toObject(MessageClass.class);
            final Intent intent = new Intent(getContext(), MessageActivity.class);
            intent.putExtra("user_uid", messageClass.getUser_receiver());
            startActivity(intent);
        });

    }

    private void MatchesRecyclerView() {

        /* query user where chat hasn't been sent to before. Totally new connection */
        Query query = db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserID)
                .collection(Constants.MATCH)
                .whereEqualTo("user_chat_start", "no")
                .orderBy("user_matched", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MatchesClass> options = new FirestoreRecyclerOptions.Builder<MatchesClass>()
                .setQuery(query, MatchesClass.class)
                .build();

        adapter = new MatchesFirestore(options);

        rvMatchLayout.setHasFixedSize(true);
        rvMatchLayout.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rvMatchLayout.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();


        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            final MatchesClass matchesClass = documentSnapshot.toObject(MatchesClass.class);
            String id = documentSnapshot.getId();
            String path = documentSnapshot.getReference().getPath();

            final Intent intent = new Intent(getContext(), MessageActivity.class);
            intent.putExtra("user_uid", matchesClass.getUser_matches());
            startActivity(intent);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        chatsFirestore.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void OnlineUser() {
        String currentUserId = currentUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserId)
                .update(arrayOnlineUser);
    }

    private void UserStatus(String status) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String currentUser = firebaseUser.getUid();

            Map<String, Object> arrayUserStatus = new HashMap<>();
            arrayUserStatus.put("user_status", status);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUser)
                    .update(arrayUserStatus);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UserStatus("online");
        OnlineUser();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }
        /* reload fragment */
        if (allowRefresh) {
            allowRefresh = false;
            callbackMatch.onMatchFragmentResume();
        }
        /* refresh on tab selected */
        myMatchReceiver = new MatchFragmentReceiver();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(myMatchReceiver,
                new IntentFilter("REFRESH_MATCH_FRAGMENT"));

    }

    @Override
    public void onPause() {
        super.onPause();
        UserStatus("paused");
        Locart.appRunning = false;
        /* refresh set to true */
        if (!allowRefresh)
            allowRefresh = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserStatus("offline");
        adapter.stopListening();
        chatsFirestore.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        /* Activity containing this fragment must implement its callbacks */
        callbackMatch = (CallbackMatch) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
//        Log.d(TAG, "search: "+ text);

        Query query = null;

        if (text.isEmpty()) {
            query = db.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUserID)
                    .collection(Constants.CHATS)
                    .orderBy("user_date_sent", Query.Direction.DESCENDING);
        } else {

            query = db.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUserID)
                    .collection(Constants.CHATS)
                    .whereEqualTo("chat_message", text)
                    .orderBy("user_date_sent", Query.Direction.ASCENDING);
        }

        FirestoreRecyclerOptions<MessageClass> options = new FirestoreRecyclerOptions.Builder<MessageClass>()
                .setQuery(query, MessageClass.class)
                .build();
        chatsFirestore.updateOptions(options);


        return false;
    }

    public interface CallbackMatch {
        /* Callback for when fragment resumes. */
        void onMatchFragmentResume();
    }

    private class MatchFragmentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MatchesFragment.this.refresh();
        }
    }

    public void refresh() {
        /* your code in refresh. */
//        Log.d(TAG, "YES");
        callbackMatch.onMatchFragmentResume();
    }

}