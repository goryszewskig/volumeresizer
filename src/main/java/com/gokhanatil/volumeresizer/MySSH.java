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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class MySSH {

    static String regionName = System.getenv("REGION_NAME");
    static String bucketName = System.getenv("BUCKET_NAME");
    static String fileName = System.getenv("FILE_NAME");

    static void runShellCommands(String targetInstance) {

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(regionName)
                .build();

        File localFile = new File("/tmp/temp.pem");

        s3Client.getObject(new GetObjectRequest(bucketName, fileName), localFile);

        JSch jsch = new JSch();

        try {
            jsch.addIdentity("/tmp/temp.pem");
            jsch.setConfig("StrictHostKeyChecking", "no");

            Session session = jsch.getSession("hadoop", targetInstance, 22);
            session.connect();

            runCommand(session, "sudo growpart /dev/xvdb 2");
            runCommand(session, "sudo xfs_growfs -d /mnt");

            session.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void runCommand(Session session, String cmdStatement) throws JSchException, IOException {


        ChannelExec channel = (ChannelExec) session.openChannel("exec");

        channel.setCommand(cmdStatement);
        channel.setErrStream(System.err);
        channel.setInputStream(null);

        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }

        channel.disconnect();

    }


}
