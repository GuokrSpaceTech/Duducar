package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.NoticeAdapter;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.database.BaseNotice;
import com.guokrspace.dududriver.database.BaseNoticeDao;
import com.guokrspace.dududriver.model.BaseNoticeItem;
import com.guokrspace.dududriver.model.DenseOrderNotice;
import com.guokrspace.dududriver.model.DuduMessage;
import com.guokrspace.dududriver.model.DuduNotice;
import com.guokrspace.dududriver.model.MessageResponseModel;
import com.guokrspace.dududriver.model.WealthNotice;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.net.message.MessageTag;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.DateUtil;
import com.guokrspace.dududriver.view.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.dao.query.Query;

/**
 * Created by hyman on 15/10/22.
 */
public class GrabOrderFragment extends BaseFragment implements Handler.Callback{

    @Bind(R.id.date_tv)
    TextView tvDate;
    @Bind(R.id.ordernum_tv)
    TextView tvOrderNum;
    @Bind(R.id.onlinetime_tv)
    TextView tvOnlineTime;
    @Bind(R.id.income_tv)
    TextView tvIncome;
    @Bind(R.id.turnover_rate_tv)
    TextView tvTurnoverRate;
    @Bind(R.id.graborder_rv)
    RecyclerView mRecyclerView;
    private Context context;
    private Handler mHandler;

    private NoticeAdapter mAdapter;
    private List<BaseNoticeItem> baseNoticeItems;


    public static GrabOrderFragment newInstance() {
        final GrabOrderFragment grabOrderFragment = new GrabOrderFragment();
//        final Bundle args = new Bundle();
        return grabOrderFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("daddy","oncreate" + CommonUtil.getTodayDoneWork());

    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        Log.e("daddy", "onattach" + CommonUtil.getTodayDoneWork());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graborder, container, false);
        ButterKnife.bind(this, view);
        Log.e("daddy", "oncreateview" + CommonUtil.getTodayDoneWork());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("daddy", "onviewcreated" + CommonUtil.getTodayDoneWork());
        initView();
    }

    private void initView() {
        baseNoticeItems = initData();
        if(baseNoticeItems.size() == 0){
            baseNoticeItems.add(new DuduNotice());
        }
        mAdapter = new NoticeAdapter(getActivity(), baseNoticeItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        updateTodayInfo();
        pullMessage();
    }

    private void updateTodayInfo(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 EEEE");
        String today = dateFormat.format(date);
        tvDate.setText(today);

        tvOrderNum.setText("已完成 " + CommonUtil.getTodayDoneWork() + " 单");
        tvOnlineTime.setText("");
        tvIncome.setText(CommonUtil.getTodayCash() + "");
        tvTurnoverRate.setText(CommonUtil.getTodayWorkRate() + " %");
    }

    private List<BaseNoticeItem> initData() {
        List<BaseNoticeItem> notices = new ArrayList<>();
        List<BaseNotice> data = DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().queryBuilder().orderDesc(BaseNoticeDao.Properties.NoticeId).list();
        Log.e("daddy message", " " + data.size() + " ");

        for (BaseNotice notice : data) {
            String type = notice.getType();
            String body = notice.getMessageBody();
            if(type == null){
                continue;
            }
            Log.e("daddy map", "notice id " + notice.getNoticeId());
            Log.e("daddy map ", "body" + body);
            if(type.equals("PayOver")){//支付通知
                notices.add(new WealthNotice(body));
            } else if(type.equals("Notice")){//系统通知
                notices.add(new DuduNotice(body));
            } else if(type.equals("HotMap")){//热力地图
                notices.add(new DenseOrderNotice(body));
            }
        }
        return notices;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public Handler getHanlder(){
        mHandler = new Handler(this);
        return mHandler;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){

            case MessageTag.MESSAGE_UPDATE_GRABORDER://update
                updateTodayInfo();
                break;
            case MessageTag.MESSAGE_UPDATE_MESSAGE:
                //TODO 更新通知
                Log.e("daddy message", "pull new message");
                pullMessage();
                break;
            case NEW_MESSAGE_UPDATE:
                baseNoticeItems = initData();
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return false;
    }

    private final int NEW_MESSAGE_UPDATE = 102;

    private void pullMessage() {
        //获取数据库中的消息列表, 按id顺序大到小排列
        List messageList = DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().queryBuilder().orderDesc(BaseNoticeDao.Properties.NoticeId).list();
        Integer currentId = 0;
        if(messageList.size() > 0){
            currentId = ((BaseNotice)messageList.get(0)).getNoticeId();
            //TODO 要做失效通知的清除操作
        }
        Log.e("daddy messaeg" ,"currentId  " + currentId);
        //每次登陆都去拉取最新的消息
        SocketClient.getInstance().pullMessages("new", Constants.MESSAGE_PER_PAGE, currentId, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Log.e("daddy message", messageBody + "recive");
                //TODO: 解析返回的消息结构
                MessageResponseModel responseModel = null;
                if (!TextUtils.isEmpty(messageBody)) {
                    responseModel = new Gson().fromJson(messageBody, MessageResponseModel.class);
                } else {
                    return;
                }
                Log.e("daddy message", responseModel.getMessage_list().size() + "list size" + responseModel.getMessage_list().get(0).getMessage_type());
                List<DuduMessage> messageList = responseModel.getMessage_list();
                if (messageList.size() > 0) {
                    boolean isUpdate = false;
                    Query query = DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().queryBuilder().where(BaseNoticeDao.Properties.NoticeId.eq("0")).build();
                    for (DuduMessage duduMessage : messageList) {
                        query.setParameter(0, duduMessage.getMessage_id());
                        Log.e("daddy message", query.list().size() + "dudu" + duduMessage.getMessage_id() + "messa" + duduMessage.getMessage_body());
                        if (query.list().size() < 1) {
                            Log.e("daddy message", "size + 1");
                            DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().insert(getBaseNotice(duduMessage));
                            isUpdate = true;
                        }
                    }
                    if (isUpdate) {
                        baseNoticeItems.clear();
                        baseNoticeItems.addAll(initData());
                        Log.e("daddy message", baseNoticeItems.size() + "");
                        if (baseNoticeItems.size() < 1) {//没有任何消息
                            baseNoticeItems.add(new DuduNotice());
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("daddy", error + "message error");
            }

            @Override
            public void onTimeout() {
            }
        });
    }

    private BaseNotice getBaseNotice(DuduMessage message){
        BaseNotice notice = new BaseNotice();
        notice.setDate(DateUtil.dateFormat(System.currentTimeMillis() + ""));
        notice.setNoticeId(Integer.parseInt(message.getMessage_id()));
        notice.setMessageBody(message.getMessage_body());
        notice.setOutOfTime(false);
        notice.setType(message.getMessage_type());
        Log.e("daddy notice", notice.getMessageBody() + "dddd");
        return notice;
    }

}
