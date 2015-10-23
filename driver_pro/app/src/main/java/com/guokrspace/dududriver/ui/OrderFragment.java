package com.guokrspace.dududriver.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.OnButtonClickListener;
import com.dexafree.materialList.card.provider.WelcomeCardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.guokrspace.dududriver.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class OrderFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MSG_ORDER_RECEIVED = 1001;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MaterialListView mListView;
    private Context mContext;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what)
            {
                case MSG_ORDER_RECEIVED:
                    Card card = new Card.Builder(mContext)
                            .setTag("WELCOME_CARD")
                            .setDismissible()
                            .withProvider(WelcomeCardProvider.class)
                            .setTitle("Welcome Card")
                            .setTitleColor(Color.WHITE)
                            .setDescription("I am the description")
                            .setDescriptionColor(Color.WHITE)
                            .setSubtitle("My subtitle!")
                            .setSubtitleColor(Color.WHITE)
                            .setBackgroundColor(Color.BLUE)
                            .setButtonText("Okay!")
                            .setOnButtonPressedListener(new OnButtonClickListener() {
                                @Override
                                public void onButtonClicked(final View view, final Card card) {
                                    Toast.makeText(mContext, "Welcome!", Toast.LENGTH_SHORT).show();
                                }
                            }).endConfig().build();

                    mListView.add(card);
                    break;
            }

            return false;
        }
    });

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    public static OrderFragment newInstance(String param1, String param2) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrderFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mContext = getActivity().getBaseContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order, null);
        MaterialListView mListView = (MaterialListView) root.findViewById(R.id.material_listview);
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }



}
