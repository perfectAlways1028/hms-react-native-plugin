/*
    Copyright 2020-2021. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License")
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.huawei.hms.rn.account.modules;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.huawei.hms.rn.account.logger.HMSLogger;
import com.huawei.hms.rn.account.utils.Utils;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.common.AccountAuthException;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

public class HMSAccountAuthManager extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final String FIELD_AUTH_ACCOUNT = "authAccount";
    private static final int REQUEST_ADD_AUTH_SCOPES = 999;
    private Promise mAddAuthScopesPromiseT;
    private HMSLogger logger;

    public HMSAccountAuthManager(ReactApplicationContext reactContext) {
        super(reactContext);
        logger = HMSLogger.getInstance(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Nonnull
    @Override
    public String getName() {
        return "HMSAccountAuthManager";
    }

    @ReactMethod
    public void getAuthResult(Promise promise) {
        logger.startMethodExecutionTimer("getAuthResult");
        ReadableMap parsedAuthAccount = Utils.parseAuthAccount(AccountAuthManager.getAuthResult());
        logger.sendSingleEvent("getAuthResult");
        promise.resolve(parsedAuthAccount);
    }

    @ReactMethod
    public void getAuthResultWithScopes(ReadableMap arguments, Promise promise) {
        ReadableArray scope = Utils.getScopeArray(arguments);
        if(scope != null) {
            List<Scope> scopeList = Utils.toScopeList(scope);
            try {
                logger.startMethodExecutionTimer("getAuthResultWithScopes");
                ReadableMap parsedAuthAccount = Utils.parseAuthAccount(AccountAuthManager.getAuthResultWithScopes(scopeList));
                logger.sendSingleEvent("getAuthResultWithScopes");
                promise.resolve(parsedAuthAccount);
            } catch (AccountAuthException e) {
                Utils.handleError(promise, e);
            }
        } else {
            promise.reject("3004", "Null authScopeList");
        }
    }

    @ReactMethod
    public void containScopes(ReadableMap readableMap, Promise promise) {
        ReadableMap fieldAuthAccount = (ReadableMap) Utils.argumentNullCheck(readableMap, FIELD_AUTH_ACCOUNT);
        ReadableArray array = Utils.getScopeArray(readableMap);

        if (fieldAuthAccount != null && array != null) {
            AuthAccount authAccount = Utils.toAuthResult(fieldAuthAccount, FIELD_AUTH_ACCOUNT);
            List<Scope> scopeList = Utils.toScopeList(array);
            logger.startMethodExecutionTimer("containScopes");
            boolean isContainScope = AccountAuthManager.containScopes(authAccount, scopeList);
            logger.sendSingleEvent("containScopes");
            promise.resolve(isContainScope);
        } else {
            promise.reject("3016", "Null authAccount or authScopeList");
        }
    }

    @ReactMethod
    public void addAuthScopes(ReadableMap readableMap, Promise promise) {
        logger.startMethodExecutionTimer("addAuthScopes");
        mAddAuthScopesPromiseT = promise;
        AccountAuthManager.addAuthScopes(Objects.requireNonNull(getCurrentActivity()),
                REQUEST_ADD_AUTH_SCOPES,
                Utils.toScopeList(Utils.getScopeArray(readableMap)));
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_ADD_AUTH_SCOPES && mAddAuthScopesPromiseT != null) {
            logger.sendSingleEvent("addAuthScopes");
            mAddAuthScopesPromiseT.resolve(true);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}