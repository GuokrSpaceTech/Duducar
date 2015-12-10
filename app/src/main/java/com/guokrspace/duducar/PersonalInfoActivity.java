package com.guokrspace.duducar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.guokrspace.duducar.database.PersonalInformation;
import com.squareup.picasso.RequestCreator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonalInfoActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PersonalInfoActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalInfoActivity extends AppCompatActivity {


    private DuduApplication mApplication;
    private Context mContext;
    private MaterialListView materialListView;
    private Toolbar mToolbar;


    public PersonalInfoActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_personal_info);
        mApplication = (DuduApplication)getApplicationContext();
        mContext = this;
        //init toolbar
        initToolBar();
        materialListView = (MaterialListView)findViewById(R.id.material_listview);

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
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalInfoActivity.this.finish();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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


}
