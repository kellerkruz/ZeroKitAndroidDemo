package com.wanari.zerokit.zerokitdemo.fragments;

import com.tresorit.zerokit.PasswordEditText;
import com.tresorit.zerokit.observer.Action1;
import com.tresorit.zerokit.response.ResponseZerokitError;
import com.tresorit.zerokit.response.ResponseZerokitLogin;
import com.wanari.zerokit.zerokitdemo.R;
import com.wanari.zerokit.zerokitdemo.common.AppConf;
import com.wanari.zerokit.zerokitdemo.common.ZerokitManager;
import com.wanari.zerokit.zerokitdemo.entities.LoginData;
import com.wanari.zerokit.zerokitdemo.interfaces.ISignIn;
import com.wanari.zerokit.zerokitdemo.rest.APIManager;
import com.wanari.zerokit.zerokitdemo.rest.entities.UserJson;
import com.wanari.zerokit.zerokitdemo.utils.ValidationUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignInFragment extends Fragment implements TextWatcher, View.OnFocusChangeListener {

    private TextInputEditText usernameEditText;

    private TextInputLayout usernameContainer;

    private PasswordEditText passwordEditText;

    private TextInputLayout passwordContainer;

    private Button signInBtn;

    private CheckBox rememberMeCheckBox;

    private ISignIn parentListener;

    private PasswordEditText.PasswordExporter mPasswordExporter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        mPasswordExporter = new PasswordEditText.PasswordExporter();
        usernameEditText = (TextInputEditText) view.findViewById(R.id.usernameEditText);
        usernameContainer = (TextInputLayout) view.findViewById(R.id.usernameTextInputLayout);

        passwordEditText = (PasswordEditText) view.findViewById(R.id.passwordEditText);
        passwordContainer = (TextInputLayout) view.findViewById(R.id.passwordTextInputLayout);

        rememberMeCheckBox = (CheckBox) view.findViewById(R.id.remembarMe);

        signInBtn = (Button) view.findViewById(R.id.signInBtn);
        setListeners();
        setData();
        return view;
    }

    private void setData() {
        LoginData loginData = AppConf.getLoginData();
        if (loginData != null) {
            usernameEditText.setText(loginData.getUsername());
        }
    }

    private void setListeners() {
        usernameEditText.addTextChangedListener(this);
        passwordEditText.addTextChangedListener(this);
        usernameEditText.setOnFocusChangeListener(this);
        passwordEditText.setOnFocusChangeListener(this);

        passwordEditText.setPasswordExporter(mPasswordExporter);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateInput();
            }
        });
    }

    private void validateInput() {
        if (ValidationUtils.hasText(usernameContainer) && ValidationUtils.hasText(passwordContainer, mPasswordExporter)) {
            hideKeyboard();
            showProgress();
            final String alias = usernameEditText.getText().toString();

            APIManager.getInstance().getService().getUserIdByUserName(alias).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io()).subscribe(
                    new rx.functions.Action1<UserJson>() {
                        @Override
                        public void call(UserJson user) {
                            AppConf.storeLoginData(new LoginData(user.getUserName(), user.getUserId()));
                            login(user.getUserId());
                        }
                    }, new rx.functions.Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            showError(getString(R.string.alert_no_user));
                        }
                    });
        }
    }

    private void login(final String userId) {
        ZerokitManager.getInstance().getZerokit().login(userId, mPasswordExporter, rememberMeCheckBox.isChecked())
                .subscribe(new Action1<ResponseZerokitLogin>() {
                    @Override
                    public void call(ResponseZerokitLogin responseLogin) {
                        mPasswordExporter.clear();
                        loginSuccess();
                    }
                }, new Action1<ResponseZerokitError>() {
                    @Override
                    public void call(ResponseZerokitError responseZerokitError) {
                        showError(responseZerokitError.getMessage());
                    }
                });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void loginSuccess() {
        if (parentListener != null) {
            parentListener.loginSuccess();
        }
    }

    private void showError(String error) {
        if (parentListener != null) {
            parentListener.showError(error);
        }
    }

    private void showProgress() {
        if (parentListener != null) {
            parentListener.showProgress();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof ISignIn) {
            parentListener = (ISignIn) getActivity();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof ISignIn) {
            parentListener = (ISignIn) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentListener = null;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        clearErrors();
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        clearErrors();
    }

    private void clearErrors() {
        usernameContainer.setError(null);
        passwordContainer.setError(null);
    }
}
