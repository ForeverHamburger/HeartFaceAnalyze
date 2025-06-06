package com.xupt.xuptfacerecognition.login;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.tencent.mmkv.MMKV;
import com.xupt.xuptfacerecognition.R;
import com.xupt.xuptfacerecognition.databinding.ActivityLoginBinding;
import com.xupt.xuptfacerecognition.login.loginin.LoginFragment;
import com.xupt.xuptfacerecognition.login.loginin.LoginInModel;
import com.xupt.xuptfacerecognition.login.loginin.LoginInPresenter;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MMKV.initialize(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        initViews();
    }


    private void initViews() {
        initFragment();
    }



    private void initFragment() {
        FragmentManager fm = getSupportFragmentManager();
        LoginFragment loginInFragment = (LoginFragment) fm.findFragmentById(R.id.fragment_container);
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );

        if (loginInFragment == null) {
            loginInFragment = new LoginFragment();
        }
        LoginInPresenter loginInPresenter = new LoginInPresenter(loginInFragment, new LoginInModel());
        loginInFragment.setPresenter(loginInPresenter);
        ft.add(R.id.fragment_container, loginInFragment);
        ft.commit();
    }

    public void adjustCardViewForFragment(int fragmentHeight) {

        if (fragmentHeight <= 1400) {
            fragmentHeight = 1400;
        }
        if(fragmentHeight >= 1600){
            fragmentHeight = 1600;
        }
        ConstraintLayout constraintLayout = findViewById(R.id.main);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        // 获取当前的 CardView 和 FragmentContainerView
        View cardView = findViewById(R.id.cardView_login);
        View fragmentContainer = findViewById(R.id.fragment_container);

        // 获取当前的高度
        int currentCardHeight = cardView.getHeight();
        int currentFragmentHeight = fragmentContainer.getHeight();

        // 计算目标 CardView 的高度
        int targetCardHeight = fragmentHeight + 180;

        // 创建并启动 FragmentContainerView 高度动画
        ValueAnimator fragmentAnimator = ValueAnimator.ofInt(currentFragmentHeight, fragmentHeight);
        setupAnimator(fragmentAnimator, R.id.fragment_container, constraintLayout, constraintSet);

        // 创建并启动 CardView 高度动画
        ValueAnimator cardAnimator = ValueAnimator.ofInt(currentCardHeight, targetCardHeight);
        setupAnimator(cardAnimator, R.id.cardView_login, constraintLayout, constraintSet);
    }

    private void setupAnimator(ValueAnimator animator, int viewId, ConstraintLayout constraintLayout, ConstraintSet constraintSet) {
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            constraintSet.constrainHeight(viewId, animatedValue);
            constraintSet.applyTo(constraintLayout);
        });
        animator.start();
    }
}