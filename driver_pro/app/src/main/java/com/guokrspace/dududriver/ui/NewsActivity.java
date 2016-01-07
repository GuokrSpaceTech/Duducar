package com.guokrspace.dududriver.ui;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.guokrspace.dududriver.DuduDriverApplication;
import com.guokrspace.dududriver.R;
import com.guokrspace.dududriver.adapter.NewsAdapter;
import com.guokrspace.dududriver.common.Constants;
import com.guokrspace.dududriver.common.NewOrderReceiver;
import com.guokrspace.dududriver.database.BaseNotice;
import com.guokrspace.dududriver.database.BaseNoticeDao;
import com.guokrspace.dududriver.model.DuduMessage;
import com.guokrspace.dududriver.model.MessageResponseModel;
import com.guokrspace.dududriver.model.News;
import com.guokrspace.dududriver.net.ResponseHandler;
import com.guokrspace.dududriver.net.SocketClient;
import com.guokrspace.dududriver.util.CommonUtil;
import com.guokrspace.dududriver.util.DateUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;

public class NewsActivity extends AppCompatActivity implements Handler.Callback{

    private static final int HANDLE_REFRESH_OVER = 1;
    private static final int MESSAGE_NO_NEWS = 1091;

    private Context context;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private NewsAdapter mAdapter;
    private List<News> newsRecord = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout mRefreshLayout;

    private boolean isRefreshing = false;

    private NewOrderReceiver receiver;
    private MainOrderDialog dialog;

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (isRefreshing) {
                Toast.makeText(context, "正在刷新，请稍后...", Toast.LENGTH_SHORT).show();
                return;
            }
            isRefreshing = true;
            pullMessage();
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper(), this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        context = NewsActivity.this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        registerBroadcastReceiver();
        //进入页面自动刷新
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefreshing(mRefreshLayout, true, true);
            }
        }, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        unregisterReceiver(receiver);
    }

    //监听service传来的消息
    private void registerBroadcastReceiver(){
        receiver = new NewOrderReceiver(mHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_ORDER);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);
    }

    private void initView() {
        initToolBar();

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        Resources resources = context.getResources();
        mRefreshLayout.setColorSchemeColors(resources.getColor(R.color.materialRed),
        resources.getColor(R.color.materialBlue),
        resources.getColor(R.color.materialYellow),
        resources.getColor(R.color.materialGreen));
        //下拉刷新
        mRefreshLayout.setOnRefreshListener(refreshListener);

        mRecyclerView = (RecyclerView) findViewById(R.id.news_recyclerview);
        newsRecord.addAll(getNews());
        mAdapter = new NewsAdapter(context, newsRecord);
        mRecyclerView.setAdapter(mAdapter);
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(40));
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("消息中心");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsActivity.this.finish();
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_REFRESH_OVER:
                isRefreshing = false;
                mRefreshLayout.setRefreshing(false);
                break;
            case MESSAGE_NO_NEWS://没有消息
                Toast.makeText(context, "暂时没有新的通知消息", Toast.LENGTH_SHORT).show();
                break;
            case Constants.MESSAGE_NEW_ORDER:
                if(CommonUtil.getCurOrderItem() == null){
                    return false;
                }
                if(dialog == null || !dialog.isVisible()){
                    dialog = new MainOrderDialog(context, CommonUtil.getCurOrderItem());
                    dialog.show(getSupportFragmentManager(), "mainorderdialog");
                }
                break;
            default:
                break;
        }
        return false;
    }

    private class SpaceItemDecoration extends RecyclerView.ItemDecoration{

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            if(parent.getChildPosition(view) != 0)
                outRect.top = space;
        }
    }

        /**
         * 著作权归作者所有。
         * 商业转载请联系作者获得授权，非商业转载请注明出处。
         * 作者：RxRead
         * 链接：http://www.zhihu.com/question/35422150/answer/62708927
         * 来源：知乎
         */

    public static void setRefreshing(SwipeRefreshLayout refreshLayout,boolean refreshing, boolean notify){
        Class<? extends SwipeRefreshLayout> refreshLayoutClass = refreshLayout.getClass();
        if (refreshLayoutClass != null) {

            try {
                Method setRefreshing = refreshLayoutClass.getDeclaredMethod("setRefreshing", boolean.class, boolean.class);
                setRefreshing.setAccessible(true);
                setRefreshing.invoke(refreshLayout, refreshing, notify);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void pullMessage() {
        //获取数据库中的消息列表, 按id顺序大到小排列
        List messageList = DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().queryBuilder().orderDesc(BaseNoticeDao.Properties.NoticeId).list();
        Integer currentId = 0;
        if(messageList.size() > 0){
            currentId = ((BaseNotice)messageList.get(0)).getNoticeId();
            //TODO 要做失效通知的清除操作
        }
        Log.e("daddy messaeg", "currentId  " + currentId);
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
                        newsRecord.clear();
                        newsRecord.addAll(getNews());
                        Log.e("daddy message", newsRecord.size() + "");
                        if (newsRecord.size() >= 1) {
                            mHandler.sendEmptyMessage(HANDLE_REFRESH_OVER);
                        }
                    }
                    if (newsRecord.size() < 1) {//没有任何消息
                        mHandler.sendEmptyMessage(MESSAGE_NO_NEWS);
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

    private List<News> getNews() {
        List<News> newses = new ArrayList<>();
        List<BaseNotice> data = DuduDriverApplication.getInstance().mDaoSession.getBaseNoticeDao().queryBuilder().where(BaseNoticeDao.Properties.Type.eq("Notice"), BaseNoticeDao.Properties.OutOfTime.eq(false)).orderDesc(BaseNoticeDao.Properties.NoticeId).list();

        for (BaseNotice notice : data) {
            String type = notice.getType();
            String body = notice.getMessageBody();
            if(type == null){
                continue;
            }
            if(type.equals("Notice")){//系统通知
                //TODO 根据有效时间修改outoftime干脆用户删除了才算
                newses.add(getNews(body, notice.getNoticeId()));
            }
        }
        return newses;
    }

    private News getNews(String body, int id){
        News news = new News();
        news.setId(id);
        try{
            Log.e("daddy notice", "message" + body);
            JSONObject object = new JSONObject(body);
            news.setTitle(object.get("title") == null ? "嘟嘟播报" : (String) object.get("title"));
            news.setUrl(object.get("url") == null ? "" : (String) object.get("url"));
            news.setContent(object.get("content") == null ? "嘟嘟欢迎您" : (String) object.get("content"));
            news.setTime(object.get("time") == null ? (System.currentTimeMillis() + 1000*60*60*24) +"" : object.get("time").toString());
        } catch (JSONException e){
            e.printStackTrace();
        }
        return news;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}

