package com.bytedance.androidcamp.network.dou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.api.GetVideoResponse;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.api.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.util.RecycleViewDivider;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swiperereshlayout ;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "MainActivity";
    private RecyclerView mRv;
    private RecyclerView mLv;
    private List<Video> mVideos = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;
    private StaggeredGridLayoutManager mLayoutManager;
    private WaterFallAdapter mAdapter;

    // TODO 8: initialize retrofit & miniDouyinService
    private Retrofit retrofit;
    private Gson gson;
    private IMiniDouyinService miniDouyinService;

    public IMiniDouyinService getMiniDouyinService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(IMiniDouyinService.HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (miniDouyinService == null) {
            miniDouyinService = retrofit.create(IMiniDouyinService.class);
        }
        return miniDouyinService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView();
        initBtns();
        swiperereshlayout = (SwipeRefreshLayout)findViewById(R.id.swiperereshlayout);
        swiperereshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Call <GetVideoResponse> call = getMiniDouyinService().getVideos();
                        call.enqueue(new Callback<GetVideoResponse>() {
                            @Override
                            public void onResponse(Call<GetVideoResponse> call, Response<GetVideoResponse> response) {
                                if(response.body()!=null &&response.body().getVideos()!= null) {
                                    mVideos = response.body().getVideos();
                                    mRv.getAdapter().notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(Call<GetVideoResponse> call, Throwable throwable) {
                            }
                        });
                        swiperereshlayout.setRefreshing(false);
                    }

                });
            }
        });
    }

    private void initBtns() {
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = "
                                + mSelectedVideo
                                + ", mSelectedImage = "
                                + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }
            }
        });

        mBtnRefresh = findViewById(R.id.btn_refresh);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
        }

        public void bind(final Activity activity, final Video video) {
            ImageHelper.displayWebImage(video.getImageUrl(), img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoActivity.launch(activity, video.getVideoUrl());
                }
            });
        }
    }

    private void initRecyclerView() {
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRv=findViewById(R.id.rv);
        mRv.setLayoutManager(mLayoutManager);
        final Paint dividerPaint = new Paint();
        dividerPaint.setColor(0xFFB5C5);
        mRv.setLayoutManager(mLayoutManager);
        mRv.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MyViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.video_item_view, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                ViewGroup.LayoutParams params = viewHolder.itemView.getLayoutParams();
                params.height=video.getImage_h()*viewHolder.itemView.getWidth()/video.getImage_w();
                viewHolder.bind(MainActivity.this, video);
            }

            @Override
            public int getItemCount() {
                return mVideos.size();
            }
        });
        mRv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, 10,0xFF98FB98));
        mRv.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, 10,0xFF98FB98));


    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = ["
                + requestCode
                + "], resultCode = ["
                + resultCode
                + "], data = ["
                + data
                + "]");

        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        File f = new File(ResourceUtils.getRealPath(MainActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);
        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part videoPart = getMultipartFromUri("video", mSelectedVideo);
        // TODO 9: post video & update buttons
        Call <PostVideoResponse> call = getMiniDouyinService().postvideo("134567","doo",coverImagePart,videoPart);
        miniDouyinService.postvideo("1023324","doudoud",coverImagePart,videoPart).enqueue(
                new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        if(response.body()!=null){

                        }
                        mBtn.setText("select a image");
                        mBtn.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
                        mBtn.setText("select a image");
                        mBtn.setEnabled(true);
                    }
                }
        );

        Toast.makeText(this, "TODO 9: post video & update buttons", Toast.LENGTH_SHORT).show();


    }

    public void fetchFeed(View view) {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);

        // TODO 10: get videos & update recycler list
        Call <GetVideoResponse> call = getMiniDouyinService().getVideos();
        call.enqueue(new Callback<GetVideoResponse>() {
            @Override
            public void onResponse(Call<GetVideoResponse> call, Response<GetVideoResponse> response) {

                if(response.body()!=null &&response.body().getVideos()!= null) {
                    mVideos = response.body().getVideos();
                }
                mRv.getAdapter().notifyDataSetChanged();
                mBtnRefresh.setText("refresh");
                mBtnRefresh.setEnabled(true);
            }

            @Override
            public void onFailure(Call<GetVideoResponse> call, Throwable throwable) {
                mBtnRefresh.setText(R.string.refresh_feed);
                mBtnRefresh.setEnabled(true);
                Toast.makeText(MainActivity.this,throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(this, "TODO 10: get videos & update recycler list"+mVideos.size(), Toast.LENGTH_SHORT).show();
    }
}
