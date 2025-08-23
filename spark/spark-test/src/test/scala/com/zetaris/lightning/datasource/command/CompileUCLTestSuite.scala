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

import com.zetaris.lightning.model.serde.UnifiedSemanticLayer
import com.zetaris.lightning.model.{NamespaceNotFoundException, TableNotActivatedException, UnifiedSemanticLayerFoundException}
import com.zetaris.lightning.spark.SparkExtensionsTestBase
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CompileUCLTestSuite extends SparkExtensionsTestBase {
  override def beforeAll(): Unit = {
    super.beforeAll()
    initRoootNamespace()
  }

  override def beforeEach(): Unit = {
    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.metastore.crm CASCADE")
  }

  test("throw InvalidNamespaceException if no namespace is defined") {
    intercept[NamespaceNotFoundException] {
      val df = sparkSession.sql(
        """
          |COMPILE USL IF NOT EXISTS crm NAMESPACE lightning.metastore.crm DDL
          |-- create table customer
          |CREATE TABLE IF NOT EXISTS customer (
          | id int NOT NULL PRIMARY KEY,
          | name varchar(200),
          | /*+@AccessControl(accessType="REGEX", regex="$ss", users = "*", groups = "*")*/
          | uid int UNIQUE,
          | address varchar(200),
          | part_id int FOREIGN KEY REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
          |);
          |
          |CREATE TABLE IF NOT EXISTS department (
          | id int NOT NULL,
          | name varchar(200),
          | CONSTRAINT pk_id PRIMARY KEY(id)
          |)
          |""".stripMargin)

    }
  }

  test("should deploy under root namespace") {
    //sparkSession.sql(s"CREATE NAMESPACE lightning.metastore")

    val df = sparkSession.sql(
      s"""
         |COMPILE USL IF NOT EXISTS crmdb DEPLOY NAMESPACE lightning.metastore DDL
         |-- create table customer
         |CREATE TABLE IF NOT EXISTS customer (
         | id int NOT NULL PRIMARY KEY,
         | name varchar(200),
         | /*+@AccessControl(accessType="REGEX", regex="ss", users = "*", groups = "*")*/
         | uid int UNIQUE,
         | address varchar(200),
         | part_id int FOREIGN KEY REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
         |);
         |
         |CREATE TABLE IF NOT EXISTS department (
         | id int NOT NULL,
         | name varchar(200),
         | CONSTRAINT pk_id PRIMARY KEY(id)
         |)
         |""".stripMargin)
    val srcJson = df.collect()(0).getString(0)
    val srcUSL = UnifiedSemanticLayer(srcJson)

    val loadJson = sparkSession.sql("LOAD USL crmdb NAMESPACE lightning.metastore").collect()(0).getString(0)
    val loadUSL = UnifiedSemanticLayer(loadJson)

    assert(srcUSL.namespace.mkString(".") == loadUSL.namespace.mkString("."))
    assert(srcUSL.name == loadUSL.name)

    assert(srcUSL.tables(0).name == loadUSL.tables(0).name)
    assert(srcUSL.tables(0).columnSpecs(0).name == loadUSL.tables(0).columnSpecs(0).name)
    assert(srcUSL.tables(0).columnSpecs(0).dataType == loadUSL.tables(0).columnSpecs(0).dataType)

    assert(srcUSL.tables(0).columnSpecs(1).name == loadUSL.tables(0).columnSpecs(1).name)
    assert(srcUSL.tables(0).columnSpecs(1).dataType == loadUSL.tables(0).columnSpecs(1).dataType)

    assert(srcUSL.tables(0).columnSpecs(2).name == loadUSL.tables(0).columnSpecs(2).name)
    assert(srcUSL.tables(0).columnSpecs(2).dataType == loadUSL.tables(0).columnSpecs(2).dataType)

    assert(srcUSL.tables(0).columnSpecs(3).name == loadUSL.tables(0).columnSpecs(3).name)
    assert(srcUSL.tables(0).columnSpecs(3).dataType == loadUSL.tables(0).columnSpecs(3).dataType)

    assert(srcUSL.tables(0).columnSpecs(4).name == loadUSL.tables(0).columnSpecs(4).name)
    assert(srcUSL.tables(0).columnSpecs(4).dataType == loadUSL.tables(0).columnSpecs(4).dataType)

    assert(srcUSL.tables(1).name == loadUSL.tables(1).name)
    assert(srcUSL.tables(1).columnSpecs(0).name == loadUSL.tables(1).columnSpecs(0).name)
    assert(srcUSL.tables(1).columnSpecs(0).dataType == loadUSL.tables(1).columnSpecs(0).dataType)

    assert(srcUSL.tables(1).columnSpecs(1).name == loadUSL.tables(1).columnSpecs(1).name)
    assert(srcUSL.tables(1).columnSpecs(1).dataType == loadUSL.tables(1).columnSpecs(1).dataType)

    sparkSession.sql("create namespace lightning.metastore.newname")
    checkAnswer(sparkSession.sql(s"SHOW namespaces  or   TABLES in lightning.metastore"), Seq(
      Row("crmdb", "usl"),
      Row("newname", "namespace")))


  }


  test("deploy DDLs & load, update USL") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    val df = sparkSession.sql(
      s"""
        |COMPILE USL IF NOT EXISTS crmdb DEPLOY NAMESPACE lightning.metastore.crm DDL
        |-- create table customer
        |CREATE TABLE IF NOT EXISTS customer (
        | id int NOT NULL PRIMARY KEY,
        | name varchar(200),
        | /*+@AccessControl(accessType="REGEX", regex="ss", users = "*", groups = "*")*/
        | uid int UNIQUE,
        | address varchar(200),
        | part_id int FOREIGN KEY REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
        |);
        |
        |CREATE TABLE IF NOT EXISTS department (
        | id int NOT NULL,
        | name varchar(200),
        | CONSTRAINT pk_id PRIMARY KEY(id)
        |)
        |""".stripMargin)
    val srcJson = df.collect()(0).getString(0)
    val srcUSL = UnifiedSemanticLayer(srcJson)

    val loadJson = sparkSession.sql("LOAD USL crmdb NAMESPACE lightning.metastore.crm").collect()(0).getString(0)
    val loadUSL = UnifiedSemanticLayer(loadJson)

    assert(srcUSL.namespace.mkString(".") == loadUSL.namespace.mkString("."))
    assert(srcUSL.name == loadUSL.name)

    assert(srcUSL.tables(0).name == loadUSL.tables(0).name)
    assert(srcUSL.tables(0).columnSpecs(0).name == loadUSL.tables(0).columnSpecs(0).name)
    assert(srcUSL.tables(0).columnSpecs(0).dataType == loadUSL.tables(0).columnSpecs(0).dataType)

    assert(srcUSL.tables(0).columnSpecs(1).name == loadUSL.tables(0).columnSpecs(1).name)
    assert(srcUSL.tables(0).columnSpecs(1).dataType == loadUSL.tables(0).columnSpecs(1).dataType)

    assert(srcUSL.tables(0).columnSpecs(2).name == loadUSL.tables(0).columnSpecs(2).name)
    assert(srcUSL.tables(0).columnSpecs(2).dataType == loadUSL.tables(0).columnSpecs(2).dataType)

    assert(srcUSL.tables(0).columnSpecs(3).name == loadUSL.tables(0).columnSpecs(3).name)
    assert(srcUSL.tables(0).columnSpecs(3).dataType == loadUSL.tables(0).columnSpecs(3).dataType)

    assert(srcUSL.tables(0).columnSpecs(4).name == loadUSL.tables(0).columnSpecs(4).name)
    assert(srcUSL.tables(0).columnSpecs(4).dataType == loadUSL.tables(0).columnSpecs(4).dataType)

    assert(srcUSL.tables(1).name == loadUSL.tables(1).name)
    assert(srcUSL.tables(1).columnSpecs(0).name == loadUSL.tables(1).columnSpecs(0).name)
    assert(srcUSL.tables(1).columnSpecs(0).dataType == loadUSL.tables(1).columnSpecs(0).dataType)

    assert(srcUSL.tables(1).columnSpecs(1).name == loadUSL.tables(1).columnSpecs(1).name)
    assert(srcUSL.tables(1).columnSpecs(1).dataType == loadUSL.tables(1).columnSpecs(1).dataType)
  }

  test("should list namespace, tables") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    sparkSession.sql(
      s"""
         |COMPILE USL IF NOT EXISTS crmdb DEPLOY NAMESPACE lightning.metastore.crm DDL
         |-- create table customer
         |CREATE TABLE IF NOT EXISTS customer (
         | id int NOT NULL PRIMARY KEY,
         | name varchar(200),
         | /*+@AccessControl(accessType="REGEX", regex="ss", users = "*", groups = "*")*/
         | uid int UNIQUE,
         | address varchar(200),
         | part_id int FOREIGN KEY REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
         |);
         |
         |CREATE TABLE IF NOT EXISTS department (
         | id int NOT NULL,
         | name varchar(200),
         | CONSTRAINT pk_id PRIMARY KEY(id)
         |)
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.crm"),
      Seq(Row("crmdb")))

    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm.otherone")
    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.crm"),
      Seq(Row("crmdb"), Row("otherone")))

    checkAnswer(sparkSession.sql(s"SHOW TABLES in lightning.metastore.crm.crmdb"), Seq(
      Row("crmdb", "customer", false),
      Row("crmdb", "department", false)))

    checkAnswer(sparkSession.sql(s"describe lightning.metastore.crm.crmdb.customer"),
      Seq(Row("id", "int", null),
        Row("name", "varchar(200)", null),
        Row("uid", "int", null),
        Row("address", "varchar(200)", null),
        Row("part_id", "int", null)))

    checkAnswer(sparkSession.sql(s"describe lightning.metastore.crm.crmdb.department"),
      Seq(Row("id", "int", null),
        Row("name", "varchar(200)", null)))

    intercept[TableNotActivatedException] {
      sparkSession.sql(s"select * from lightning.metastore.crm.crmdb.customer").show()
    }

  }

  test("should compile TEXT type and default") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    sparkSession.sql(
      s"""
         |COMPILE USL IF NOT EXISTS customer_sales DEPLOY NAMESPACE lightning.metastore.usldb DDL -- Reference/Master Data Tables
         |CREATE TABLE dim_time (
         |    time_key INT PRIMARY KEY,
         |    date_value DATE NOT NULL,
         |    year_number INT NOT NULL,
         |    quarter_number INT NOT NULL,
         |    month_number INT NOT NULL,
         |    day_of_week INT NOT NULL,
         |    is_weekend BOOLEAN DEFAULT FALSE,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE dim_customer (
         |    customer_key INT PRIMARY KEY,
         |    customer_id VARCHAR(50) UNIQUE NOT NULL,
         |    customer_name VARCHAR(255) NOT NULL,
         |    email VARCHAR(255),
         |    phone VARCHAR(50),
         |    address TEXT,
         |    customer_segment VARCHAR(50),
         |    registration_date DATE,
         |    is_active BOOLEAN DEFAULT TRUE,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE dim_product (
         |    product_key INT PRIMARY KEY,
         |    product_id VARCHAR(50) UNIQUE NOT NULL,
         |    product_name VARCHAR(255) NOT NULL,
         |    category VARCHAR(100),
         |    subcategory VARCHAR(100),
         |    brand VARCHAR(100),
         |    unit_price DECIMAL(10,2),
         |    cost DECIMAL(10,2),
         |    is_active BOOLEAN DEFAULT TRUE,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE warehouses (
         |    warehouse_id INT PRIMARY KEY,
         |    warehouse_code VARCHAR(20) UNIQUE NOT NULL,
         |    warehouse_name VARCHAR(255) NOT NULL,
         |    location VARCHAR(255),
         |    manager_name VARCHAR(255),
         |    capacity_units INT,
         |    is_active BOOLEAN DEFAULT TRUE,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |-- Transactional Tables
         |CREATE TABLE users (
         |    user_id INT PRIMARY KEY,
         |    username VARCHAR(100) UNIQUE NOT NULL,
         |    email VARCHAR(255) UNIQUE NOT NULL,
         |    password_hash VARCHAR(255) NOT NULL,
         |    first_name VARCHAR(100),
         |    last_name VARCHAR(100),
         |    role VARCHAR(50) DEFAULT 'user',
         |    is_active BOOLEAN DEFAULT TRUE,
         |    last_login TIMESTAMP,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE orders (
         |    order_id INT PRIMARY KEY,
         |    order_number VARCHAR(50) UNIQUE NOT NULL,
         |    customer_key INT NOT NULL,
         |    user_id INT,
         |    order_date DATE NOT NULL,
         |    order_status VARCHAR(50) DEFAULT 'pending',
         |    total_amount DECIMAL(12,2),
         |    tax_amount DECIMAL(10,2),
         |    shipping_amount DECIMAL(10,2),
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (customer_key) REFERENCES dim_customer(customer_key),
         |    FOREIGN KEY (user_id) REFERENCES users(user_id)
         |);
         |
         |CREATE TABLE order_items (
         |    order_item_id INT PRIMARY KEY,
         |    order_id INT NOT NULL,
         |    product_key INT NOT NULL,
         |    quantity INT NOT NULL CHECK (quantity > 0),
         |    unit_price DECIMAL(10,2) NOT NULL,
         |    line_total DECIMAL(12,2) NOT NULL,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
         |    FOREIGN KEY (product_key) REFERENCES dim_product(product_key)
         |);
         |
         |CREATE TABLE stock_levels (
         |    stock_id INT PRIMARY KEY,
         |    product_key INT NOT NULL,
         |    warehouse_id INT NOT NULL,
         |    quantity_on_hand INT DEFAULT 0,
         |    reserved_quantity INT DEFAULT 0,
         |    reorder_level INT DEFAULT 0,
         |    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (product_key) REFERENCES dim_product(product_key),
         |    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
         |    UNIQUE (product_key, warehouse_id)
         |);
         |
         |-- Fact Tables
         |CREATE TABLE fact_sales (
         |    sale_id INT PRIMARY KEY,
         |    order_id INT NOT NULL,
         |    customer_key INT NOT NULL,
         |    product_key INT NOT NULL,
         |    time_key INT NOT NULL,
         |    quantity_sold INT NOT NULL,
         |    unit_price DECIMAL(10,2) NOT NULL,
         |    total_sales_amount DECIMAL(12,2) NOT NULL,
         |    cost_amount DECIMAL(12,2),
         |    profit_amount DECIMAL(12,2),
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (order_id) REFERENCES orders(order_id),
         |    FOREIGN KEY (customer_key) REFERENCES dim_customer(customer_key),
         |    FOREIGN KEY (product_key) REFERENCES dim_product(product_key),
         |    FOREIGN KEY (time_key) REFERENCES dim_time(time_key)
         |);
         |
         |-- Configuration and Management Tables
         |CREATE TABLE feature_flags (
         |    flag_id INT PRIMARY KEY,
         |    flag_name VARCHAR(100) UNIQUE NOT NULL,
         |    flag_description TEXT,
         |    is_enabled BOOLEAN DEFAULT FALSE,
         |    environment VARCHAR(50) DEFAULT 'production',
         |    created_by INT,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (created_by) REFERENCES users(user_id)
         |);
         |
         |CREATE TABLE user_permissions (
         |    permission_id INT PRIMARY KEY,
         |    user_id INT NOT NULL,
         |    permission_name VARCHAR(100) NOT NULL,
         |    resource_type VARCHAR(100),
         |    resource_id VARCHAR(100),
         |    granted_by INT,
         |    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    expires_at TIMESTAMP,
         |    is_active BOOLEAN DEFAULT TRUE,
         |    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
         |    FOREIGN KEY (granted_by) REFERENCES users(user_id)
         |);
         |
         |-- Audit and Logging Tables
         |CREATE TABLE audit_logs (
         |    log_id INT PRIMARY KEY,
         |    user_id INT,
         |    action_type VARCHAR(100) NOT NULL,
         |    table_name VARCHAR(100),
         |    record_id VARCHAR(100),
         |    old_values TEXT,
         |    new_values TEXT,
         |    ip_address VARCHAR(45),
         |    user_agent TEXT,
         |    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (user_id) REFERENCES users(user_id)
         |);
         |
         |-- Data Quality and Governance Tables
         |CREATE TABLE data_quality_rules (
         |    rule_id INT PRIMARY KEY,
         |    rule_name VARCHAR(255) NOT NULL,
         |    table_name VARCHAR(100) NOT NULL,
         |    column_name VARCHAR(100),
         |    rule_type VARCHAR(50) NOT NULL,
         |    rule_definition TEXT NOT NULL,
         |    severity_level VARCHAR(20) DEFAULT 'medium',
         |    is_active BOOLEAN DEFAULT TRUE,
         |    created_by INT,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (created_by) REFERENCES users(user_id)
         |);
         |
         |CREATE TABLE data_quality_results (
         |    result_id INT PRIMARY KEY,
         |    rule_id INT NOT NULL,
         |    execution_date DATE NOT NULL,
         |    records_checked INT DEFAULT 0,
         |    records_failed INT DEFAULT 0,
         |    success_rate DECIMAL(5,2),
         |    execution_time_ms INT,
         |    error_details TEXT,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (rule_id) REFERENCES data_quality_rules(rule_id)
         |);
         |
         |-- Customer Analytics Tables
         |CREATE TABLE customer_segments (
         |    segment_id INT PRIMARY KEY,
         |    segment_name VARCHAR(100) NOT NULL,
         |    segment_description TEXT,
         |    segment_criteria TEXT,
         |    is_active BOOLEAN DEFAULT TRUE,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE customer_360 (
         |    customer_360_id INT PRIMARY KEY,
         |    customer_key INT NOT NULL,
         |    segment_id INT,
         |    total_orders INT DEFAULT 0,
         |    total_spent DECIMAL(12,2) DEFAULT 0,
         |    avg_order_value DECIMAL(10,2),
         |    last_order_date DATE,
         |    customer_lifetime_value DECIMAL(12,2),
         |    churn_probability DECIMAL(5,4),
         |    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (customer_key) REFERENCES dim_customer(customer_key),
         |    FOREIGN KEY (segment_id) REFERENCES customer_segments(segment_id)
         |);
         |
         |-- Performance and Monitoring Tables
         |CREATE TABLE performance_metrics (
         |    metric_id INT PRIMARY KEY,
         |    metric_name VARCHAR(100) NOT NULL,
         |    metric_category VARCHAR(50),
         |    metric_value DECIMAL(15,6),
         |    unit_of_measure VARCHAR(20),
         |    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    system_component VARCHAR(100),
         |    environment VARCHAR(50)
         |);
         |
         |-- ETL and Data Integration Tables
         |CREATE TABLE staging_customer_data (
         |    staging_id INT PRIMARY KEY,
         |    source_system VARCHAR(50) NOT NULL,
         |    batch_id VARCHAR(100),
         |    customer_external_id VARCHAR(100),
         |    raw_data TEXT,
         |    processing_status VARCHAR(20) DEFAULT 'pending',
         |    error_message TEXT,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    processed_at TIMESTAMP
         |);
         |
         |-- Master Data Management
         |CREATE TABLE master_customer (
         |    master_customer_id INT PRIMARY KEY,
         |    customer_key INT NOT NULL,
         |    source_system VARCHAR(50) NOT NULL,
         |    external_customer_id VARCHAR(100),
         |    confidence_score DECIMAL(3,2) DEFAULT 1.00,
         |    is_golden_record BOOLEAN DEFAULT FALSE,
         |    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    FOREIGN KEY (customer_key) REFERENCES dim_customer(customer_key)
         |);
         |
         |""".stripMargin)

  }

  test("should remove USL") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    val df = sparkSession.sql(
      s"""
         |COMPILE USL IF NOT EXISTS crmdb DEPLOY NAMESPACE lightning.metastore.crm DDL
         |-- create table customer
         |CREATE TABLE IF NOT EXISTS customer (
         | id int NOT NULL PRIMARY KEY,
         | name varchar(200),
         | /*+@AccessControl(accessType="REGEX", regex="ss", users = "*", groups = "*")*/
         | uid int UNIQUE,
         | address varchar(200)
         |);
         |""".stripMargin)

    sparkSession.sql("LOAD USL crmdb NAMESPACE lightning.metastore.crm").collect()(0).getString(0)

    sparkSession.sql("REMOVE USL crmdb NAMESPACE lightning.metastore.crm").collect()(0).getBoolean(0)
    intercept[UnifiedSemanticLayerFoundException] {
      sparkSession.sql("LOAD USL crmdb NAMESPACE lightning.metastore.crm").collect()(0).getString(0)
    }

  }

}
