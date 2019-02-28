package com.example.anhquoc.mycustom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.anhquoc.mycustom.pullrefresh.PullRefreshLayout;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements CustomSwipeRefresh.OnRefreshListener {

    @BindView(R.id.swipe_refresh_layout)
    CustomSwipeRefresh mCustomSwipeRefresh;

    @BindView(R.id.refreshing_icon)
    SimpleDraweeView mDraweeView;

    @BindView(R.id.refreshing_icon1)
    SimpleDraweeView mDraweeView1;

    @BindView(R.id.refreshing_icon2)
    SimpleDraweeView mDraweeView2;

    @BindView(R.id.refreshing_icon3)
    SimpleDraweeView mDraweeView3;

    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mCustomSwipeRefresh.setOnRefreshListener(this);

        mUri = new Uri.Builder().scheme(UriUtil.LOCAL_ASSET_SCHEME).path("refreshing_icon.webp").build();

        playAnimation(mDraweeView);
        playAnimation(mDraweeView1);
        playAnimation(mDraweeView2);
        playAnimation(mDraweeView3);
    }

    private void playAnimation(SimpleDraweeView view) {
        view.setController(
                Fresco.newDraweeControllerBuilder()
                        .setUri(mUri)
                        .setAutoPlayAnimations(true)
                        .build()
        );
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCustomSwipeRefresh.setRefreshing(false);
            }
        }, 2000);
    }
}
