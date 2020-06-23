//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.rallytac.engageandroid;

import com.rallytac.engage.engine.Engine;

import org.json.JSONArray;
import org.json.JSONObject;

public class CertStore
{
    public String _fileName;
    public JSONObject _descriptor;

    private String _cachedDisplayName = null;
    private String _cachedDescription = null;

    public String getDisplayName()
    {
        if(Utils.isEmptyString(_cachedDisplayName))
        {
            _cachedDisplayName = _fileName;

            if (!Utils.isEmptyString(_cachedDisplayName))
            {
                int pos = _cachedDisplayName.indexOf("}-");
                _cachedDisplayName = _cachedDisplayName.substring(pos + 2);
            }
            else
            {
                _cachedDisplayName = "(no name)";
            }
        }

        return _cachedDisplayName;
    }

    public String getDescription()
    {
        if(Utils.isEmptyString(_cachedDescription))
        {
            StringBuilder sb = new StringBuilder();

            sb.append("V");
            sb.append(_descriptor.optInt(Engine.JsonFields.CertStoreDescriptor.version, 0));
            sb.append(", ");

            JSONArray certificates = _descriptor.optJSONArray(Engine.JsonFields.CertStoreDescriptor.certificates);
            sb.append(certificates.length());
            sb.append(" certificates");

            sb.append("\nhash [");
            StringBuilder hashInput = new StringBuilder();
            hashInput.append(_descriptor.optInt(Engine.JsonFields.CertStoreDescriptor.version, 0));
            hashInput.append(certificates.toString());
            sb.append(Utils.md5HashOfString(hashInput.toString()));
            sb.append("]");

            _cachedDescription = sb.toString();
        }

        return _cachedDescription;
    }
}
