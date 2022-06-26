package com.micoder.whatsappclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import soup.neumorphism.NeumorphCardView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter (List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture, messageSenderFile, messageReceiverFile;
        public NeumorphCardView receiverCardText, senderCardText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageSenderFile = itemView.findViewById(R.id.message_sender_file_view);
            messageReceiverFile = itemView.findViewById(R.id.message_receiver_file_view);
            receiverCardText = itemView.findViewById(R.id.neumorph_receiver_text_card);
            senderCardText = itemView.findViewById(R.id.neumorph_sender_text_card);

        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, final int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.receiverCardText.setVisibility(View.GONE);
        messageViewHolder.senderCardText.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.messageSenderFile.setVisibility(View.GONE);
        messageViewHolder.messageReceiverFile.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {

            if (fromUserID.equals(messageSenderID)) {

                messageViewHolder.senderCardText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);

                messageViewHolder.senderMessageText.setText(Html.fromHtml(
                        "<font color=#000000>" + "<big>" + messages.getMessage() + "</big>"
                                + "<br/>" + "<br/>" + "<small>" + "<small>" + messages.getTime()+ " - " + messages.getDate() + "</small>" + "</small>"));

            }
            else {

                messageViewHolder.receiverCardText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(Html.fromHtml(
                        "<font color=#000000>" + "<big>" + messages.getMessage() + "</big>"
                                + "<br/>" + "<br/>" + "<small>" + "<small>" + messages.getTime()+ " - " + messages.getDate() + "</small>" + "</small>"));
            }
        }
        else if (fromMessageType.equals("image")) {

                if (fromUserID.equals(messageSenderID)) {
                    messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                }
                else {
                    messageViewHolder.receiverCardText.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
                }
        }
        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.messageSenderFile.setVisibility(View.VISIBLE);

                //messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);
                //load your picture link from firbase to avoid the file img bug
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-clone-78bd0.appspot.com/o/Image_Files%2Ffile.png?alt=media&token=3fc1b123-907a-406e-b37a-5d59f5be342e")
                        .placeholder(R.drawable.file).into(messageViewHolder.messageSenderFile);

            }
            else {
                messageViewHolder.receiverCardText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverFile.setVisibility(View.VISIBLE);

                //messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.file);
                //load your picture link from firbase to avoid the file img bug
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-clone-78bd0.appspot.com/o/Image_Files%2Ffile.png?alt=media&token=3fc1b123-907a-406e-b37a-5d59f5be342e")
                        .placeholder(R.drawable.file).into(messageViewHolder.messageReceiverFile);

            }
        }



        if (fromUserID.equals(messageSenderID)) {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this Document",
                                        "Cancel",
                                        "Delete for Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 3) {
                                    deleteMessageForEveryOne(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });

                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete for Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 2) {
                                    deleteMessageForEveryOne(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });

                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View this Image",
                                        "Cancel",
                                        "Delete for Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1) {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 3) {
                                    deleteMessageForEveryOne(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });

                        builder.show();
                    }

                }
            });
        }
        else {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this Document",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    deleteReceivedMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });

                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    deleteReceivedMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });

                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View this Image",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    deleteReceivedMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1) {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });

                        builder.show();
                    }

                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }



    private void deleteSentMessage(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(holder.itemView.getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occured...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void deleteReceivedMessage(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(holder.itemView.getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occured...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    private void deleteMessageForEveryOne(final int position, final MessageViewHolder holder) {

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getMessageID())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occured...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }



}
