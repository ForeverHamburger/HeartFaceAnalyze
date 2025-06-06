package com.xupt.xuptfacerecognition.login.loginin;

import static com.xupt.xuptfacerecognition.base.ValidationUtil.PASSWORD_REGEX;
import static com.xupt.xuptfacerecognition.base.ValidationUtil.PHONE_REGEX_CN;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.tencent.mmkv.MMKV;
import com.xupt.xuptfacerecognition.R;
import com.xupt.xuptfacerecognition.base.InputValidator;
import com.xupt.xuptfacerecognition.databinding.FragmentLoginBinding;
import com.xupt.xuptfacerecognition.login.LoginActivity;
import com.xupt.xuptfacerecognition.login.register.RegisterFragment;
import com.xupt.xuptfacerecognition.login.register.RegisterModel;
import com.xupt.xuptfacerecognition.login.register.RegisterPresenter;
import com.xupt.xuptfacerecognition.prompt.PromptActivity;

import java.util.regex.Pattern;


public class LoginFragment extends Fragment implements LoginInContract.View {
    private FragmentLoginBinding binding;
    private LoginInContract.Presenter mPresenter;
    private boolean mIsChecked;

    public LoginFragment() {
    }


    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mPresenter.onstart();
        binding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                int fragmentHeight = binding.getRoot().findViewById(R.id.ConstraintLayout_login).getHeight();
                Log.d("fragmentHeight", "LoginInFragment: " + fragmentHeight);
                ((LoginActivity) getActivity()).adjustCardViewForFragment(fragmentHeight);
            }
        });
        Bundle args = getArguments();
        if (args != null) {
            String email = args.getString("email", "");
            String password = args.getString("password", "");

            binding.editTextUsername.setText(email);
            binding.editTextPassword.setText(password);
        }
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tlUsername.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        binding.tlPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        binding.tlUsername.setErrorIconDrawable(0);
        binding.tlPassword.setErrorIconDrawable(0);
        String text_forget_password = getString(R.string.login_forget_password);
        String text_to_register_before = getString(R.string.login_forget_to_register_before);
        String text_to_register_after = getString(R.string.login_forget_to_register_after);

        binding.textViewToRegister.setText(combineAndUnderline(text_to_register_before, text_to_register_after));
        binding.textViewForget.setText(combineAndUnderline("",text_forget_password));
        binding.buttonLogin.setOnClickListener(v -> {
            String phoneoremail = binding.editTextUsername.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            if (!processLogin(phoneoremail, password)) {
                return;
            }
            mPresenter.onLoginClick(phoneoremail, password);
        });
        // 账号输入框焦点改变监听
        binding.editTextUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String phoneoremail = binding.editTextUsername.getText().toString().trim();
                    // 移除之前可能存在的错误提示
                    binding.tlUsername.setError(null);
                    if (phoneoremail.isEmpty() || phoneoremail.trim().isEmpty()) {
                        binding.tlUsername.setError("账号不能为空");
                    }
                    if (!Pattern.matches(PHONE_REGEX_CN, phoneoremail)&&!Patterns.EMAIL_ADDRESS.matcher(phoneoremail).matches()) {
                        binding.tlUsername.setError("账号格式错误");
                    }
                } else {
                    binding.tlUsername.setError(null);
                }
            }
        });
        binding.editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String password = binding.editTextPassword.getText().toString();
                    binding.tlPassword.setError(null);
                    if (password == null || password.isEmpty()) {
                        binding.tlPassword.setError("密码不能为空");
                    }
                    if (password.length() < 8) {
                        binding.tlPassword.setError("密码至少需要8位");
                    }
                    if (!Pattern.matches(PASSWORD_REGEX, password)) {
                        binding.tlPassword.setError("需包含字母和数字");
                    }
                } else {
                    binding.tlPassword.setError(null);
                }
            }
        });
        binding.textViewToRegister.setOnClickListener(v -> {
            RegisterFragment registerFragment = new RegisterFragment();

            RegisterPresenter registerPresenter = new RegisterPresenter(registerFragment, new RegisterModel());
            registerFragment.setPresenter(registerPresenter);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_bottom,
                            R.anim.slide_out_bottom
                    )
                    .replace(R.id.fragment_container, registerFragment)
                    .commit();
        });

        binding.checkBoxRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsChecked = isChecked;
            }
        });

        MMKV mmkv = MMKV.mmkvWithID("Tea's Whisper");
        Log.d("test", "onViewCreated: "+mmkv.getBoolean("ifChecked",false));
        if (mmkv.getBoolean("ifChecked",false)) {
            Log.d("test", "onViewCreated: "+222 +mmkv.getString("Username",""));
            binding.checkBoxRemember.setChecked(true);
            binding.editTextUsername.setText(mmkv.getString("Username",""));
            binding.editTextPassword.setText(mmkv.getString("Password",""));
        } else {
            Log.d("test", "onViewCreated: "+111);
            binding.checkBoxRemember.setChecked(false);
        }

        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.title_font);
        binding.textViewTitle.setTypeface(typeface);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
//        binding = null;
        mPresenter.unSubscribe();
        mPresenter = null;
    }

    @Override
    public void showError(String error) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "登录失败："+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public Boolean isACtive() {
        //判断是否加入到Activity
        return isAdded();
    }

    @Override
    public void loginSuccess() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "Login Success!", Toast.LENGTH_SHORT).show();

            }
        });
        MMKV mmkv = MMKV.mmkvWithID("Tea's Whisper");
        if (mIsChecked) {
            // 记住密码
            Log.d("test", "onViewCreated: "+333);
            mmkv.putBoolean("ifChecked",true);
            mmkv.putString("Username",binding.editTextUsername.getText().toString());
            mmkv.putString("Password",binding.editTextPassword.getText().toString());
        } else {
            mmkv.putBoolean("ifChecked",false);
            mmkv.putString("Username","");
            mmkv.putString("Password","");
        }

        Intent intent = new Intent(getActivity(), PromptActivity.class);
        startActivity(intent);
    }

    @Override
    public void setPresenter(LoginInContract.Presenter presenter) {
        mPresenter = presenter;
    }

    //辅助方法
    public static SpannableString combineAndUnderline(String firstPart, String secondPart) {
        // 为第一个字符串创建 SpannableString 并添加下划线
        SpannableString spannableFirstPart = new SpannableString(firstPart);
        spannableFirstPart.setSpan(new UnderlineSpan(), 0, firstPart.length(), 0);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLACK);
        spannableFirstPart.setSpan(colorSpan, 0, firstPart.length(), 0);

        // 为第二个字符串创建 SpannableString 并添加下划线
        SpannableString spannableSecondPart = new SpannableString(secondPart);
        spannableSecondPart.setSpan(new UnderlineSpan(), 0, secondPart.length(), 0);
        // 拼接两个 SpannableString
        SpannableString finalSpannableString = new SpannableString(firstPart + secondPart);
        // 仅对拼接后的后半段添加下划线
        finalSpannableString.setSpan(new UnderlineSpan(), firstPart.length(), firstPart.length() + secondPart.length(), 0);

        return finalSpannableString;
    }

    private boolean processLogin(String username, String password) {
        InputValidator validator = new InputValidator();
        if (!validator.validateAccount(username)) {
            binding.tlUsername.setError("账号格式不正确");
            Toast.makeText(getContext(), "账号格式不正确", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) {
                binding.tlUsername.setError("账号不能为空");
            }
            if (password.isEmpty()) {
                binding.tlPassword.setError("密码不能为空");
            }
            Toast.makeText(getContext(), "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (username.length() < 6 || password.length() < 6) {
            if (username.length() < 6) {
                binding.tlUsername.setError("账号长度不能小于6位");
            }
            if (password.length() < 6) {
                binding.tlPassword.setError("密码长度不能小于6位");
            }
            return false;
        }
        return true;
    }


}