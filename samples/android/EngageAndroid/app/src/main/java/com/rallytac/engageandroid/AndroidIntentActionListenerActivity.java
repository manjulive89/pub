//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.rallytac.engageandroid;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rallytac.engageandroid.ActiveConfiguration;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.Utils;

import java.io.BufferedInputStream;

public class AndroidIntentActionListenerActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_action_listener);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null)
        {
            if (type.startsWith("image/"))
            {
                processImageIntent(intent);
            }
        }
    }

    private void processImageIntent(Intent intent)
    {
        try
        {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null)
            {
                BufferedInputStream is = new BufferedInputStream(getContentResolver().openInputStream(uri));
                String scannedString = Utils.qrCodeBitmapToString(BitmapFactory.decodeStream(is));

                if(Utils.isEmptyString(scannedString))
                {
                    Toast.makeText(this, "Cannot process QR code", Toast.LENGTH_LONG).show();
                    finish();
                }

                ActiveConfiguration ac = Globals.getEngageApplication().processScannedQrCode(scannedString, null);

                Toast.makeText(this, "Loaded " + ac.getMissionName(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
        catch(Exception e)
        {
            Toast.makeText(this, "Cannot process image", Toast.LENGTH_LONG).show();
        }
    }
}
