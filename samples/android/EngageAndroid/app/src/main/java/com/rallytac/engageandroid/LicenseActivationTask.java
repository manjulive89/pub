//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.rallytac.engageandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LicenseActivationTask extends AsyncTask<String, Void, String>
{
    private static final String TAG = LicenseActivationTask.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    public interface ITaskCompletionNotification
    {
        void onLicenseActivationTaskComplete(int result, String activationCode, String resultMessage);
    }

    private Context _ctx;
    private String _url;
    private String _entitlement;
    private String _key;
    private String _activationCode;
    private String _deviceId;
    private String _hValue;
    private ITaskCompletionNotification _completionNotification;

    private int _result;
    private String _resultActivationCode;
    private String _resultMessage;

    public LicenseActivationTask(Context ctx,
                                 String url,
                                 String entitlement,
                                 String key,
                                 String activationCode,
                                 String deviceId,
                                 String hValue,
                                 ITaskCompletionNotification completionNotification)
    {
        _ctx = ctx;
        _url = url;
        _entitlement = entitlement;
        _key = key;
        _activationCode = activationCode;
        _deviceId = deviceId;
        _hValue = hValue;
        _completionNotification = completionNotification;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);

        if(_completionNotification != null)
        {
            _completionNotification.onLicenseActivationTaskComplete(_result, _resultActivationCode, _resultMessage);
        }
    }

    @Override
    protected String doInBackground(String... params)
    {
        _result = -1;
        _resultActivationCode = null;
        _resultMessage = null;

        HttpURLConnection httpConnection = null;
        JSONObject deviceInfo;

        try
        {
            deviceInfo = new JSONObject();

            deviceInfo.put("manufacturer", Build.MANUFACTURER);
            deviceInfo.put("device", Build.DEVICE);
            deviceInfo.put("type", Build.TYPE);
            deviceInfo.put("board", Build.BOARD);
            deviceInfo.put("model", Build.MODEL);
            deviceInfo.put("cpuAbi", Build.CPU_ABI);
            deviceInfo.put("display", Build.DISPLAY);
            deviceInfo.put("hardware", Build.HARDWARE);
            deviceInfo.put("host", Build.HOST);
            deviceInfo.put("id", Build.ID);
            deviceInfo.put("user", Build.USER);
            deviceInfo.put("product", Build.PRODUCT);
            deviceInfo.put("tags", Build.TAGS);
        }
        catch (Exception e)
        {
            deviceInfo = null;
        }

        try
        {
            JSONObject obj = new JSONObject();

            obj.put("deviceSerialNumber", _deviceId);

            obj.put("entitlementKey", _entitlement);
            obj.put("licenseId", _key);
            obj.put("h", _hValue);

            if(!Utils.isEmptyString(_activationCode))
            {
                obj.put("activationCode", _activationCode);
            }

            if(deviceInfo != null)
            {
                obj.put("deviceInfo", deviceInfo);
            }

            obj.put("appVersion", BuildConfig.VERSION_NAME);
            obj.put("appPackage", Globals.getContext().getPackageName());

            String dataToPost = obj.toString();

            URL url = new URL(_url);

            httpConnection = (HttpURLConnection) url.openConnection();

            byte[] bytes = dataToPost.getBytes();
            int len = bytes.length;

            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-length", Integer.toString(len));
            httpConnection.setUseCaches(false);
            httpConnection.setAllowUserInteraction(false);
            httpConnection.setConnectTimeout(CONNECT_TIMEOUT);
            httpConnection.setReadTimeout(READ_TIMEOUT);
            httpConnection.setDoOutput(true);
            httpConnection.setChunkedStreamingMode(0);
            httpConnection.connect();

            OutputStream out = new BufferedOutputStream(httpConnection.getOutputStream());
            out.write(bytes);
            out.flush();
            out.close();

            int httpResultCode = httpConnection.getResponseCode();
            String httpResultMessage = httpConnection.getResponseMessage();

            if (httpResultCode == HttpURLConnection.HTTP_OK)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null)
                {
                    sb.append(line);
                }
                br.close();

                JSONObject rc = new JSONObject(sb.toString());
                Log.d(TAG, rc.toString());

                _result = rc.getInt("returnCode");
                _resultMessage = rc.optString("returnCodeDescr", null);
                _resultActivationCode = rc.optString("activationCode", null);
            }
            else
            {
                throw new Exception("HTTP failure " + httpResultCode);
            }
        }
        catch (Exception ex)
        {
            _result = -1;
            _activationCode = null;
            _resultMessage = ex.getLocalizedMessage();
        }
        finally
        {
            if(httpConnection != null)
            {
                httpConnection.disconnect();
            }
        }

        return null;
    }
}
