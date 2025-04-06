package com.xupt.xuptfacerecognition.login.register;

import static com.xupt.xuptfacerecognition.login.loginin.LoginFragment.combineAndUnderline;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.xupt.xuptfacerecognition.R;
import com.xupt.xuptfacerecognition.base.ValidationResult;
import com.xupt.xuptfacerecognition.base.ValidationUtil;
import com.xupt.xuptfacerecognition.databinding.FragmentRegisterBinding;
import com.xupt.xuptfacerecognition.login.LoginActivity;
import com.xupt.xuptfacerecognition.login.loginin.LoginFragment;
import com.xupt.xuptfacerecognition.login.loginin.LoginInModel;
import com.xupt.xuptfacerecognition.login.loginin.LoginInPresenter;

public class RegisterFragment extends Fragment implements RegisterContract.View{
    private FragmentRegisterBinding binding;
    private RegisterContract.Presenter mPresenter;
    private CountDownTimer countDownTimer;

    public RegisterFragment() {
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        mPresenter.onstart();
        Log.d("fragmentHeight", "RegisterFragment" );
        binding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                int fragmentHeight = binding.getRoot().findViewById(R.id.ConstraintLayout_re).getHeight();
                Log.d("fragmentHeight", "RegisterFragment: " + fragmentHeight);
                ((LoginActivity) getActivity()).adjustCardViewForFragment(fragmentHeight);
            }
        });
        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        binding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                int fragmentHeight = binding.getRoot().findViewById(R.id.ConstraintLayout_re).getHeight();
                Log.d("fragmentHeight", "onreRegisterFragment: " + fragmentHeight);
                ((LoginActivity) getActivity()).adjustCardViewForFragment(fragmentHeight);
            }
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initError();
        String text_to_register_before = getString(R.string.login_have_to_register_before);
        String text_to_register_after = getString(R.string.login_have_to_register_after);
        binding.textViewToLogin.setText(combineAndUnderline(text_to_register_before, text_to_register_after));

        binding.buttonRegister.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            String phone = binding.editTextPhone.getText().toString();
            String confirmPassword = binding.editTextConfirmPassword.getText().toString();
            // 执行所有验证
            ValidationResult[] results = {
                    ValidationUtil.validatePhone(phone),
                    ValidationUtil.validateEmail(email),
                    ValidationUtil.validatePassword(password),
                    ValidationUtil.validateConfirmPassword(password, confirmPassword),
            };

            // 查找第一个错误
            for (ValidationResult result : results) {
                if (!result.isValid()) {
                    Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mPresenter.onRegisterClick(email, password, phone);
        });
        binding.textViewToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isACtive()) {
                    LoginFragment logininFragment = new LoginFragment();
                    LoginInPresenter loginInPresenter = new LoginInPresenter(logininFragment, new LoginInModel());
                    logininFragment.setPresenter(loginInPresenter);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_bottom,
                                    R.anim.slide_out_bottom
                            )
                            .replace(R.id.fragment_container, logininFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.title_font);
        binding.textViewTitle.setTypeface(typeface);
    }

    private void initError() {
        binding.tlPhone.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        binding.tlPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        binding.tlEmail.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        binding.tlConfirmPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        binding.tlPhone.setErrorIconDrawable(0);
        binding.tlPassword.setErrorIconDrawable(0);
        binding.tlEmail.setErrorIconDrawable(0);
        binding.tlConfirmPassword.setErrorIconDrawable(0);

        binding.editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String password = binding.editTextPassword.getText().toString();
                    binding.tlPassword.setError(null);
                    ValidationResult result = ValidationUtil.validatePassword(password);
                    if (!result.isValid()) {
                        binding.tlPassword.setError(result.getErrorMessage());
                    }
                } else {
                    binding.tlPassword.setError(null);
                }
            }
        });
        binding.editTextConfirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String ConfirmPassword = binding.editTextConfirmPassword.getText().toString();
                    String password = binding.editTextPassword.getText().toString();
                    binding.tlConfirmPassword.setError(null);
                    ValidationResult result = ValidationUtil.validateConfirmPassword(password,ConfirmPassword);
                    if (!result.isValid()) {
                        binding.tlConfirmPassword.setError(result.getErrorMessage());
                    }
                } else {
                    binding.tlConfirmPassword.setError(null);
                }
            }
        });
        binding.editTextEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String email = binding.editTextEmail.getText().toString();
                    binding.tlEmail.setError(null);
                    ValidationResult result = ValidationUtil.validateEmail(email);
                    if (!result.isValid()) {
                        binding.tlEmail.setError(result.getErrorMessage());
                    }
                } else {
                    binding.tlEmail.setError(null);
                }
            }
        });
        binding.editTextPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String phone = binding.editTextPhone.getText().toString();
                    binding.tlPhone.setError(null);
                    ValidationResult result = ValidationUtil.validatePhone(phone);
                    if (!result.isValid()) {
                        binding.tlPhone.setError(result.getErrorMessage());
                    }
                }
                else {
                    binding.tlPhone.setError(null);
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        binding = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        mPresenter.unSubscribe();
        mPresenter = null;
    }

    @Override
    public void showError(String error){
        Toast.makeText(getActivity(), "注册失败："+error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Boolean isACtive() {
        //判断是否加入到Activity
        return isAdded();
    }

    @Override
    public void showSuccess(String data) {
        Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();
        if(data.equals("注册成功")){
            showNewDialogConfirmation();
        }
    }
    public void showNewDialogConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("即将去登录")
                .setMessage("确定要去登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    initFragment();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void initFragment() {
        String email = binding.editTextEmail.getText().toString();
        String password = binding.editTextPassword.getText().toString();
        LoginFragment loginFragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("password", password);
        loginFragment.setArguments(args);

        LoginInPresenter loginInPresenter = new LoginInPresenter(loginFragment, new LoginInModel());
        loginFragment.setPresenter(loginInPresenter);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_bottom
                )
                .replace(R.id.fragment_container, loginFragment)
                .commit();
    }


    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        mPresenter = presenter;
    }
}