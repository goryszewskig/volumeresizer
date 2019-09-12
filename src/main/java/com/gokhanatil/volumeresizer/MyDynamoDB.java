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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;


public abstract class MyDynamoDB {

    private static String dynamoTableName = System.getenv("DDB_NAME");
    private static String regionName = System.getenv("REGION_NAME");
    private static String clusterID = System.getenv("CLUSTER_ID");

    private static AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.standard()
            .withRegion(regionName).build();

    private static Table dynamoDbTable = new DynamoDB(ddbClient).getTable(dynamoTableName);

    static Item getVolumeInfo() {
        return dynamoDbTable.getItem(new GetItemSpec().withPrimaryKey("clusterid", clusterID ) );
    }

    static void setVolumeInfo(String pip, String volid) {
        dynamoDbTable.putItem( new Item().withPrimaryKey( "clusterid", clusterID).with("volid", volid).with( "pip", pip )  );
    }

    static void deleteVolumeInfo() {
        dynamoDbTable.deleteItem( new DeleteItemSpec().withPrimaryKey("clusterid", clusterID ) );
    }


}
