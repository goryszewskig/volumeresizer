/*
 * MIT License
 *
 * Copyright (c) 2019 Gokhan Atil (https://gokhanatil.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.gokhanatil.volumeresizer;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public class Resizer implements RequestHandler<Map<String, Object>, String> {
    public String handleRequest(Map<String, Object> input, Context context) {

        String result = "{'result': 'success'}";

        Item volumeInfo = MyDynamoDB.getVolumeInfo();

        if (volumeInfo != null) {

            String targetVolume = volumeInfo.getString("volid");
            String targetInstance = volumeInfo.getString("pip");

            if (VolumeChecker.isResized(targetVolume)) {
                MySSH.runShellCommands(targetInstance);
                MyDynamoDB.deleteVolumeInfo();
                result = "{'result': 'resized " + targetVolume + "'}";
            } else
                result = "{'result': 'waiting for " + targetVolume + "'}";

        } else VolumeChecker.checkVolumes();

        return result;
    }

}
