package com.locart.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.locart.R
import com.locart.Utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class ProfileAdapter(
        private var profileUsers: List<ProfileUser> = emptyList()) :
        RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    private val firebaseFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_profile, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profileUsers[position]

        /* check if username is not empty */
        if (profile.username == "") {
            holder.name.text = profile.name
        } else {
            """ @${profile.username}""".also { holder.username.text = it }
            holder.name.text = profile.name
//            holder.username.text = ""profile.username
        }
        holder.nexus.text = profile.nexus

        /* check for job and company */
        if (profile.job != "" && profile.company != "") {
            holder.job.text = String.format("%s at %s", profile.job, profile.company)
        } else if (profile.job == "" && profile.company != "") {
            holder.job.text = profile.company
        } else if (profile.job != "" && profile.company == "") {
            holder.job.text = profile.job
        } else {
            holder.job.visibility = View.GONE
        }


        if (profile.education != "") {
            holder.education.text = profile.education
        } else {
            holder.education.visibility = View.GONE
        }

        if (profile.city != "") {
            holder.city.text = profile.city
        } else {
            holder.city.visibility = View.GONE
        }

        if (profile.website != "") {
            holder.website.text = profile.website
        } else {
            holder.website.visibility = View.GONE
        }

        if (profile.about != "") {
            holder.about.text = profile.about
        } else {
            holder.about.visibility = View.GONE
        }

        if (profile.lookingFor != "None") {
            holder.lookingFor.text = "Looking For: " + profile.lookingFor
        } else {
            holder.lookingFor.visibility = View.GONE
        }

        if (profile.donate != "None" && profile.donateHow != "") {
            holder.donate.text = """${profile.donate}: ${profile.donateHow}"""
        } else {
            holder.donate.visibility = View.GONE
        }


        Glide.with(holder.image)
                .load(profile.url)
                .placeholder(R.drawable.no_image)
                .into(holder.image)

//        holder.back.setOnClickListener {
//            val context = holder.back.context
//            (context as ProfileActivity).finish()
//
//        }

        holder.textViewShareAccount.setOnClickListener {
            /* logic to share user profile */
            val context = holder.textViewShareAccount.context

            val username = profile.username
            if (username != "") {
                val sendIntent = Intent(Intent.ACTION_SEND)
                sendIntent.type = "text/plain"
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Locart.me/$username")
                context.startActivity(sendIntent)
            } else {
                Toast.makeText(context.applicationContext, "Username is not set", Toast.LENGTH_SHORT).show()
            }
        }

        holder.textViewBlockAccount.setOnClickListener {

            val profileUserId = profile.id
            val context = holder.textViewBlockAccount.context
            val profileName: String = profile.name
            val currentUserId: String = FirebaseAuth.getInstance().currentUser!!.uid

            val mapBlock: MutableMap<String, Any> = HashMap()
            mapBlock["user_blocked"] = Timestamp.now()
            mapBlock["user_blocks"] = profileUserId
            mapBlock["user_super"] = "no"

            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUserId)
                    .collection(Constants.BLOCK)
                    .document(profileUserId)
                    .set(mapBlock)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context.applicationContext,
                                    "You have blocked: $profileName", Toast.LENGTH_SHORT).show()
                        }
                    }

        }
        holder.textViewMuteAccount.setOnClickListener {
            val context = holder.textViewMuteAccount.context
            Toast.makeText(context.applicationContext,
                    "Mute Coming Soon", Toast.LENGTH_SHORT).show()
        }
        holder.tvSearchChat.setOnClickListener {
            val context = holder.tvSearchChat.context
            Toast.makeText(context.applicationContext,
                    "Search Coming Soon", Toast.LENGTH_SHORT).show()

        }

    }

    private fun Any?.ifNullOrEmpty(default: String) =
            if (this == null || (this is CharSequence && this.isEmpty()))
                default
            else
                this.toString()

    override fun getItemCount(): Int {
        return profileUsers.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val username: TextView = view.findViewById(R.id.username)
        var city: TextView = view.findViewById(R.id.city)
        var image: ImageView = view.findViewById(R.id.imageViewImage)

        val nexus: TextView = view.findViewById(R.id.nexus)
        var job: TextView = view.findViewById(R.id.job)
        var education: TextView = view.findViewById(R.id.education)
        val website: TextView = view.findViewById(R.id.website)
        var about: TextView = view.findViewById(R.id.about)

//        var back: ImageView = view.findViewById(R.id.backButton)
        var textViewBlockAccount: TextView = view.findViewById(R.id.tvBlockAccount)
        var textViewShareAccount: TextView = view.findViewById(R.id.tvShareAccount)
        var textViewMuteAccount: TextView = view.findViewById(R.id.tvMuteAccount)
        var tvSearchChat: TextView = view.findViewById(R.id.tvSearchChat)

        val lookingFor: TextView = view.findViewById(R.id.looking_for)
        val donate: TextView = view.findViewById(R.id.donate)

    }

}