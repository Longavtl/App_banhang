package com.longavtl.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.util.AndroidException;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.longavtl.R;
import com.longavtl.adapter.SanPhamMoiAdapter;
import com.longavtl.adapter.loaiSpAdapter;
import com.longavtl.model.SanPhamMoi;
import com.longavtl.model.SanPhamMoiModel;
import com.longavtl.model.loaiSp;
import com.longavtl.retrofit.ApiBanHang;
import com.longavtl.retrofit.RetrofitClient;
import com.longavtl.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewFlipper viewFlipper;
    RecyclerView recyclerViewManHinhChinh;
    NavigationView navigationview;
    ListView listviewManHinhChinh;
    DrawerLayout drawerlayout;
    loaiSpAdapter loaiSpAdapter;
    List<loaiSp> mangloaisp;
    CompositeDisposable compositeDisposable =new CompositeDisposable();
    ApiBanHang apiBanHang;
    List<SanPhamMoi> mangSpMoi;
    SanPhamMoiAdapter spAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        apiBanHang= RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        AnhXa();
        ActionBar();
        if(isConnected(this))
        {
            Toast.makeText(this, "đã kết nối internet", Toast.LENGTH_SHORT).show();
            ActionViewFlipper();
            getLoaiSanPham();
            getSpMoi();
            getEventClick();
        }
        else
        {
            Toast.makeText(this, "không có kết nối internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void getEventClick() {
        listviewManHinhChinh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i)
                {
                    case 0:
                        Intent trangchu =new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(trangchu);
                        break;
                    case 1:
                        Intent dienthoai =new Intent(getApplicationContext(),DienThoaiActivity.class);
                        dienthoai.putExtra("loai",1);
                        startActivity(dienthoai);
                        break;
                    case 2:
                        Intent laptop =new Intent(getApplicationContext(),LaptopActivity.class);
                        startActivity(laptop);
                        break;
                }
            }
        });
    }

    private void getSpMoi() {
        compositeDisposable.add(apiBanHang.getSpMoi()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(sanPhamMoiModel ->
                {
                    if(sanPhamMoiModel.isSuccess())
                    {
                        mangSpMoi=sanPhamMoiModel.getResult();
                        spAdapter =new SanPhamMoiAdapter(getApplicationContext(),mangSpMoi);
                        recyclerViewManHinhChinh.setAdapter(spAdapter);
                    }

                },throwable -> {
                    Toast.makeText(getApplicationContext(),"Không kết nối được với sever"+throwable.getMessage(),Toast.LENGTH_LONG).show();
                        }
                        ));
    }

    private void getLoaiSanPham() {
        compositeDisposable.add(apiBanHang.getLoaiSp().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(loaiSpModel -> {
                    if(loaiSpModel.isSuccess()){
                        //Toast.makeText(getApplicationContext(),loaiSpModel.getResult().get(1).getTensanpham(), Toast.LENGTH_SHORT).show();
                    mangloaisp=loaiSpModel.getResult();
                        //khởi tạo Adapter
                        loaiSpAdapter =new loaiSpAdapter(getApplicationContext(),mangloaisp);
                        listviewManHinhChinh.setAdapter(loaiSpAdapter);
                    }
                }));
    }

    private void ActionViewFlipper() {
        List<String> maquangcao=new ArrayList<>();
        maquangcao.add("http://mauweb.monamedia.net/thegioididong/wp-content/uploads/2017/12/banner-Le-hoi-phu-kien-800-300.png");
        maquangcao.add("http://mauweb.monamedia.net/thegioididong/wp-content/uploads/2017/12/banner-HC-Tra-Gop-800-300.png");
        maquangcao.add("http://mauweb.monamedia.net/thegioididong/wp-content/uploads/2017/12/banner-big-ky-nguyen-800-300.jpg");
        for(int i=0;i<maquangcao.size();i++)
        {
            ImageView imageview=new ImageView(getApplicationContext());
            Glide.with(getApplicationContext()).load(maquangcao.get(i)).into(imageview);
            imageview.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imageview);
        }
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
        Animation slide_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_right);
        Animation slide_out = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_right);
        viewFlipper.setInAnimation(slide_in);
        viewFlipper.setInAnimation(slide_out);
    }

    private void ActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            drawerlayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void AnhXa() {
        toolbar=(Toolbar) findViewById(R.id.toolbarManHinhChinh);
        viewFlipper=(ViewFlipper) findViewById(R.id.viewflipper);
        recyclerViewManHinhChinh=(RecyclerView) findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager =new GridLayoutManager(this,2);
        recyclerViewManHinhChinh.setLayoutManager(layoutManager);
        recyclerViewManHinhChinh.setHasFixedSize(true);
        navigationview=(NavigationView) findViewById(R.id.navigationview);
        listviewManHinhChinh=(ListView) findViewById(R.id.listview);
        drawerlayout=(DrawerLayout) findViewById(R.id.drawerlayout);
        //khởi tạo list
        mangloaisp=new ArrayList<>();
        mangSpMoi= new ArrayList<>();
        if(Utils.manggiohang==null)
        {
            Utils.manggiohang=new ArrayList<>();
        }
    }
    private boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi= connectivityManager.getNetworkInfo(connectivityManager.TYPE_WIFI);
        NetworkInfo mobile= connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE);
        if((wifi!=null& wifi.isConnected())||(mobile!=null& mobile.isConnected()) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}