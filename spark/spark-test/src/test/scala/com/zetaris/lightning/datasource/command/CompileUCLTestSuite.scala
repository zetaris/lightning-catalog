/*
 *
 *  * Copyright 2023 ZETARIS Pty Ltd
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  * associated documentation files (the "Software"), to deal in the Software without restriction,
 *  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 *  * subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all copies
 *  * or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.zetaris.lightning.datasource.command

import com.zetaris.lightning.spark.SparkExtensionsTestBase
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CompileUCLTestSuite extends SparkExtensionsTestBase {
  test("compile DDLs") {
    val df = sparkSession.sql(
      """
        |COMPILE UCL IF NOT EXISTS crm NAMESPACE lightning.catalog.crm DDL
        |-- create table customer
        |CREATE TABLE IF NOT EXISTS customer (
        | id int NOT NULL PRIMARY KEY,
        | name varchar(200),
        | /*+@AccessControl(accessType="REGEX", regex="$ss", users = "*", groups = "*")*/
        | uid int UNIQUE,
        | address varchar(200),
        | part_id int FOREIGN KEY REFERENCES department(id)
        |);
        |
        |CREATE TABLE IF NOT EXISTS department (
        | id int NOT NULL,
        | name varchar(200),
        | CONSTRAINT pk_id PRIMARY KEY(id)
        |)
        |""".stripMargin)
    val json = df.collect()(0).getString(0)
    println(json)

    /**
    {
    "name": "crm",
    "namespace": [
        "lightning",
        "catalog",
        "crm"
    ],
    "tables": [
        {
            "fqn": [
                "customer"
            ],
            "columnSpecs": [
                {
                    "name": "id",
                    "dataType": "\"integer\"",
                    "primaryKey": {
                        "columns": []
                    },
                    "notNull": {
                        "columns": []
                    }
                },
                {
                    "name": "name",
                    "dataType": "\"varchar(200)\""
                },
                {
                    "name": "uid",
                    "dataType": "\"integer\"",
                    "unique": {
                        "columns": []
                    },
                    "accessControl": {
                        "accessType": "REGEX",
                        "regEx": "$ss",
                        "users": [
                            "*"
                        ],
                        "groups": [
                            "*"
                        ]
                    }
                },
                {
                    "name": "address",
                    "dataType": "\"varchar(200)\""
                },
                {
                    "name": "part_id",
                    "dataType": "\"integer\"",
                    "foreignKey": {
                        "columns": [],
                        "refTable": [
                            "department"
                        ],
                        "refColumns": [
                            "id"
                        ]
                    }
                }
            ],
            "unique": [],
            "foreignKeys": [],
            "ifNotExit": true,
            "namespace": [
                "lightning",
                "catalog",
                "crm"
            ],
            "dqAnnotations": [],
            "acAnnotations": []
        },
        {
            "fqn": [
                "department"
            ],
            "columnSpecs": [
                {
                    "name": "id",
                    "dataType": "\"integer\"",
                    "notNull": {
                        "columns": []
                    }
                },
                {
                    "name": "name",
                    "dataType": "\"varchar(200)\""
                }
            ],
            "primaryKey": {
                "columns": [
                    "id"
                ],
                "name": "pk_id"
            },
            "unique": [],
            "foreignKeys": [],
            "ifNotExit": true,
            "namespace": [
                "lightning",
                "catalog",
                "crm"
            ],
            "dqAnnotations": [],
            "acAnnotations": []
        }
    ]
}}  */
  }

}
