package de.hof.university.app.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.chat.Helper.ChatController;
import de.hof.university.app.chat.Helper.ConnectionMannager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements Observer {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SPLUS = "splus";
    private static final String ARG_LECTURE = "mylecture";

    private String mySplus;
    private TextView myLectureTitleTextView;
    private RecyclerView recyclerView;
    private EditText myEditTextView;
    private Button mySendButton;
    private ArrayList<ChatMessage> chatlist = new ArrayList<>();
    private ChatAdapter chatAdapter;

    private de.hof.university.app.debug.ChatFragment.OnFragmentInteractionListener mListener;
    private ChatController chatCtrl;
    private ConnectionMannager conManager;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SPLUS, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            MessageSingleton.getInstance().addObserver(this);
            mySplus = getArguments().getString(ARG_SPLUS);
            chatCtrl = new ChatController(getContext(),mySplus);
            conManager = new ConnectionMannager(getContext());
            chatCtrl.login();
            MessageSingleton.getInstance().addObserver(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_chat, container, false);


        myLectureTitleTextView = v.findViewById(R.id.lectureTitleTextView);
        recyclerView = v.findViewById(R.id.chatHistoryRecycler);
        myEditTextView = v.findViewById(R.id.editChat);
        mySendButton = v.findViewById(R.id.sendMessageButton);

        mySendButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(Html.fromHtml("<font color='"+ ContextCompat.getColor(MainActivity.getAppContext(), R.color.colorBlack)+"'>"+ "Stundenplanchat" +"</font>"));
        mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        chatAdapter = new ChatAdapter(chatlist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.getAppContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(chatAdapter);


        Log.d("recieved SPLUS", mySplus);
        return v;
    }

    public void sendMessage (View v){
        if (!conManager.checkInternet()){
            mySendButton.setActivated(false);
            mySendButton.setTextColor(Color.GRAY);
            showToast();
        }else{
            mySendButton.setActivated(true);
            mySendButton.setTextColor(Color.BLACK);
            Log.d("send Message: ", myEditTextView.getText().toString());
            ChatMessage msg = new ChatMessage("CarlaGuluci91", myEditTextView.getText().toString());
            chatlist.add(msg);

        chatAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
        chatCtrl.sendMessage(msg.getMessage());
        chatAdapter.notifyDataSetChanged();
        myEditTextView.setText("");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (chatAdapter != null) {
                chatAdapter.notifyDataSetChanged();
        }
        //if (context instanceof OnFragmentInteractionListener) {
        // mListener = (OnFragmentInteractionListener) context;
        //} else {
        //  throw new RuntimeException(context.toString()
        //        + " must implement OnFragmentInteractionListener");
        //}
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        chatCtrl.stopChat();
        chatlist.clear();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("mySplus ist:", mySplus);
        myLectureTitleTextView.setText(mySplus);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void update(Observable observable, Object o) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ChatMessage> messages = MessageSingleton.getInstance().getMessages();
                chatlist.clear();
                chatlist.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                Log.d("Achtung!",MessageSingleton.getInstance().getStatus().toString());
                switch (MessageSingleton.getInstance().getStatus()){
                    case working:
                        break;
                    case networkUnavailable:
                        showToast();
                        break;
                        default:
                }
            }
        });

    }

    public void showToast(){
        Toast.makeText(getContext(),R.string.chat_offline,
                Toast.LENGTH_SHORT).show();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more it {nformation.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}