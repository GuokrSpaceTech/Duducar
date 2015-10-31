package com.guokrspace.duducar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.guokrspace.duducar.database.PersonalInformation;
import com.squareup.picasso.RequestCreator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonalInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PersonalInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalInfoFragment extends Fragment {


    private DuduApplication mApplication;
    private Context mContext;
    private MaterialListView materialListView;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    public static PersonalInfoFragment newInstance() {
        PersonalInfoFragment fragment = new PersonalInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PersonalInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_personal_info, container, false);
        materialListView = (MaterialListView)root.findViewById(R.id.material_listview);

        setHasOptionsMenu(true);

        for(PersonalInformation personalInformation:mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list())
        {
            Card card = new Card.Builder(mContext)
                    .setTag("SMALL_IMAGE_CARD")
                    .setDismissible()
                    .withProvider(new CardProvider<>())
                    .setLayout(R.layout.material_small_image_card)
                    .setTitle(personalInformation.getMobile())
                    .setTitleColor(Color.GRAY)
                    .setDescription("关于我的介绍...")
                    .setDescriptionColor(Color.GRAY)
                    .setDrawable(R.drawable.ic_drawer)
                    .setBackgroundColor(Color.WHITE)
                    .setDrawableConfiguration(new CardProvider.OnImageConfigListener() {
                        @Override
                        public void onImageConfigure(@NonNull final RequestCreator requestCreator) {
                            requestCreator.rotate(90.0f)
                                    .resize(150, 150)
                                    .centerCrop();
                        }
                    })
                    .endConfig()
                    .build();
            materialListView.getAdapter().add(card);

        }

        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mApplication = (DuduApplication)activity.getApplicationContext();
        mContext = activity;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if(mContext!=null)
            ((PreOrderActivity)mContext).getSupportActionBar().setTitle("个人信息");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
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
        public void onFragmentInteraction(Uri uri);
    }

}
