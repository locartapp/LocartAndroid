package com.locart.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import com.locart.Activity.MainActivity
import com.locart.R
import com.locart.Utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.yuyakaido.android.cardstackview.*
import java.util.*
import kotlin.collections.ArrayList


class ProfileActivity : AppCompatActivity(), CardStackListener {

    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { ProfileAdapter(createSpots()) }

    var profileUser: String? = null
    var id: String? = null
    var name: String? = null
    private var imageUrl: String? = null
    var city: String? = null
    var nexus: String? = null
    var nexusCount: String? = null
    var about: String? = null
    var job: String? = null
    var education: String? = null
    var username: String? = null
    var website: String? = null
    var company: String? = null
    var currentUser: String? = null
    var lookingFor: String? = null
    var donate: String? = null
    var donateHow: String? = null

    private val firebaseFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val TAG = ProfileActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


        val extras = intent.extras
        if (null != extras) {
            id = extras.getString("user_uid")
            name = extras.getString("name")
            imageUrl = extras.getString("image")
            city = extras.getString("city")
            profileUser = extras.getString("user_uid")
            nexus = extras.getString("nexus")
            nexusCount = """Nexus: $nexus"""
            about = extras.getString("about")
            job = extras.getString("job")
            education = extras.getString("education")
            username = extras.getString("username")
            website = extras.getString("website")
            company = extras.getString("company")

            lookingFor = extras.getString("lookingFor")
            donate = extras.getString("donate")
            donateHow = extras.getString("donateHow")
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            currentUser = user.uid
        }

        setupCardStackView()

    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
//        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
//        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        if (direction == Direction.Right) {
            Toast.makeText(applicationContext, "Already in your Nexus", Toast.LENGTH_SHORT).show()
            finish()
//            Log.d("CardStackView", "already connected");
        } else if (direction == Direction.Left) {
//            Log.d("CardStackView", "disconnected done");
            Toast.makeText(applicationContext,
                    "Unlinked from your nexus", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            unlink()

        }

    }

    private fun unlink() {
        /* create unlink collection for current user */
        createUnlink()

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser.toString())
                .collection(Constants.MATCH)
                .document(profileUser.toString())
                .delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(currentUser.toString())
                                .collection(Constants.LINK)
                                .document(profileUser.toString())
                                .delete()
                                .addOnCompleteListener { task2 ->
                                    if (task2.isSuccessful) {
                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                .document(profileUser.toString())
                                                .collection(Constants.MATCH)
                                                .document(currentUser.toString())
                                                .delete()
                                                .addOnCompleteListener { task3 ->
                                                    if (task3.isSuccessful) {
                                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                .document(profileUser.toString())
                                                                .collection(Constants.LINK)
                                                                .document(currentUser.toString())
                                                                .delete()
                                                                .addOnCompleteListener { task4 ->
                                                                    if (task4.isSuccessful) {
                                                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                                .document(profileUser.toString())
                                                                                .collection(Constants.LIKE)
                                                                                .document(currentUser.toString())
                                                                                .delete()
                                                                                .addOnCompleteListener { task5 ->
                                                                                    if (task5.isSuccessful) {
                                                                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                                                .document(currentUser.toString())
                                                                                                .collection(Constants.LIKE)
                                                                                                .document(profileUser.toString())
                                                                                                .delete()
                                                                                                .addOnCompleteListener { task6 ->
                                                                                                    if (task6.isSuccessful) {
                                                                                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                                                                .document(currentUser.toString())
                                                                                                                .collection(Constants.CHATS)
                                                                                                                .document(profileUser.toString())
                                                                                                                .delete()
                                                                                                                .addOnCompleteListener { task7 ->
                                                                                                                    if (task7.isSuccessful) {
                                                                                                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                                                                                .document(profileUser.toString())
                                                                                                                                .collection(Constants.CHATS)
                                                                                                                                .document(currentUser.toString())
                                                                                                                                .delete()
                                                                                                                                .addOnCompleteListener { task8 ->
                                                                                                                                    if (task8.isSuccessful) {
                                                                                                                                        /* delete all chat related to both users */
                                                                                                                                        deleteAllChats()

                                                                                                                                    }
                                                                                                                                }
                                                                                                                    }
                                                                                                                }

                                                                                                    }
                                                                                                }

                                                                                    }
                                                                                }
                                                                    }
                                                                }
                                                    }
                                                }
                                    }
                                }
                    }
                }

    }

    private fun createUnlink() {
        val mapUnlinkCurrentUser: MutableMap<String, Any> = HashMap()
        mapUnlinkCurrentUser["user_unlink"] = profileUser.toString()
        mapUnlinkCurrentUser["user_unlinked"] = Timestamp.now()

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser.toString())
                .collection(Constants.UNLINK)
                .document(profileUser.toString())
                .set(mapUnlinkCurrentUser)
                .addOnSuccessListener {
                    val mapUnlinkProfileUser: MutableMap<String, Any> = HashMap()
                    mapUnlinkProfileUser["user_unlink"] = profileUser.toString()
                    mapUnlinkProfileUser["user_unlinked"] = Timestamp.now()

                    firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                            .document(profileUser.toString())
                            .collection(Constants.UNLINK)
                            .document(currentUser.toString())
                            .set(mapUnlinkProfileUser)

                }

    }

    private fun deleteAllChats() {
        val itemsRef: CollectionReference = firebaseFirestore.collection(Constants.CHATS)
        /* query for chats for user */
        val query = itemsRef.whereEqualTo("chat_sender", currentUser.toString())
                .whereEqualTo("chat_receiver", profileUser.toString())
        val query2 = itemsRef.whereEqualTo("chat_sender", profileUser.toString())
                .whereEqualTo("chat_receiver", currentUser.toString())

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    itemsRef.document(document.id).delete()
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
            }
        }

        query2.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    itemsRef.document(document.id).delete()
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.exception)
            }
        }

    }

    override fun onCardRewound() {
//        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
//        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.name)
//        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.name)
//        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupCardStackView() {
        initialize()
    }

    private fun initialize() {
        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Arrays.asList(Direction.Left, Direction.Right))
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(false)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun createSpots(): List<ProfileUser> {
        val spots = ArrayList<ProfileUser>()

        spots.add(ProfileUser(id = "", name = name.toString(), city = city.toString(), url = imageUrl.toString(),
                nexus = nexusCount.toString(), job = job.toString(), education = education.toString(),
                website = website.toString(), about = about.toString(), company = company.toString(),
                username = username.toString(), lookingFor = lookingFor.toString(), donate = donate.toString(), donateHow = donateHow.toString()))

        return spots
    }

    fun finishMe() {
        finish()
    }
}