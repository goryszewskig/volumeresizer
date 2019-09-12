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

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeVolumesModificationsRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.ModifyVolumeRequest;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceAsyncClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.Instance;
import com.amazonaws.services.elasticmapreduce.model.ListInstancesRequest;
import com.amazonaws.services.elasticmapreduce.model.ListInstancesResult;

public abstract class VolumeChecker {

    static String regionName = System.getenv("REGION_NAME");
    static String clusterID = System.getenv("CLUSTER_ID");
    static int minVolumeSize = Integer.parseInt(System.getenv("MIN_VOLUME_SIZE"));

    static AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
            .withRegion(regionName).build();

    static AmazonElasticMapReduce emr = AmazonElasticMapReduceAsyncClientBuilder.standard()
            .withRegion(regionName).build();


    public static void checkVolumes() {

        ListInstancesResult instancesResult = emr.listInstances( new ListInstancesRequest().withClusterId(clusterID).withInstanceGroupTypes("CORE") );

        for ( Instance instance : instancesResult.getInstances()){

            String instancePrivateIpAddress = instance.getPrivateIpAddress();
            String instanceVolumeId = instance.getEbsVolumes().get(0).getVolumeId();

            int volumeSize =  ec2.describeVolumes( new DescribeVolumesRequest().withVolumeIds(instanceVolumeId) ).getVolumes().get(0).getSize();

            if (volumeSize < minVolumeSize ) {

                ec2.modifyVolume( new ModifyVolumeRequest().withVolumeId(instanceVolumeId).withSize(minVolumeSize) );
                MyDynamoDB.setVolumeInfo( instancePrivateIpAddress, instanceVolumeId  );

                System.out.println("instanceVolumeId = " + instanceVolumeId);
                System.out.println("instancePublicIpAddress = " + instancePrivateIpAddress);

                break;
            }

        }

    }

    public static boolean isResized( String targetVolume ) {

        String modificationState = ec2.describeVolumesModifications(new DescribeVolumesModificationsRequest().withVolumeIds(targetVolume)).getVolumesModifications().get(0).getModificationState();

        if (modificationState.equals("completed" )) return true;

        return false;
    }


}
