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
         |COMPILE USL IF NOT EXISTS crmdb DEPLOY NAMESPACE lightning.metastore.crm DDL
         |-- create table customer
         |CREATE TABLE IF NOT EXISTS customer (
         | id int NOT NULL PRIMARY KEY,
         | name varchar(200),
         | /*+@AccessControl(accessType="REGEX", regex="ss", users = "*", groups = "*")*/
         | uid int UNIQUE,
         | address TEXT,
         | createAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         | balance DECIMAL(3,2) DEFAULT 1.00,
         | is_weekend BOOLEAN DEFAULT FALSE,
         | is_active BOOLEAN DEFAULT TRUE,
         | quantity_on_hand INT DEFAULT 0,
         | processing_status VARCHAR(20) DEFAULT 'pending',
         | address2 TEXT
         |)
         |""".stripMargin)

  }

  test("should compile real example") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    sparkSession.sql(
      s"""
         |COMPILE USL IF NOT EXISTS new_web_sales DEPLOY NAMESPACE lightning.metastore.crm DDL -- =============================================
         |-- DIMENSIONAL MODELING & DATA WAREHOUSING
         |-- =============================================
         |
         |-- Time dimension table
         |CREATE TABLE dim_time (
         |    time_key INT PRIMARY KEY,
         |    date_actual DATE NOT NULL,
         |    day_of_week INT,
         |    day_name VARCHAR(10),
         |    month_number INT,
         |    month_name VARCHAR(15),
         |    quarter_number INT,
         |    year_number INT,
         |    is_weekend BOOLEAN,
         |    is_holiday BOOLEAN,
         |    fiscal_year INT,
         |    fiscal_quarter INT
         |);
         |
         |-- Customer dimension table
         |CREATE TABLE dim_customer (
         |    customer_key INT PRIMARY KEY,
         |    customer_id VARCHAR(50) UNIQUE NOT NULL,
         |    customer_name VARCHAR(255) NOT NULL,
         |    customer_type VARCHAR(50),
         |    segment VARCHAR(50),
         |    region VARCHAR(100),
         |    country VARCHAR(100),
         |    registration_date DATE,
         |    status VARCHAR(20),
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |-- Product dimension table
         |CREATE TABLE dim_product (
         |    product_key INT PRIMARY KEY,
         |    product_id VARCHAR(50) UNIQUE NOT NULL,
         |    product_name VARCHAR(255) NOT NULL,
         |    category VARCHAR(100),
         |    subcategory VARCHAR(100),
         |    brand VARCHAR(100),
         |    manufacturer VARCHAR(100),
         |    unit_cost DECIMAL(10,2),
         |    list_price DECIMAL(10,2),
         |    status VARCHAR(20),
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |-- Sales fact table
         |CREATE TABLE fact_sales (
         |    sales_key BIGINT PRIMARY KEY,
         |    time_key INT NOT NULL,
         |    customer_key INT NOT NULL,
         |    product_key INT NOT NULL,
         |    transaction_id VARCHAR(100) NOT NULL,
         |    quantity INT NOT NULL,
         |    unit_price DECIMAL(10,2) NOT NULL,
         |    discount_amount DECIMAL(10,2) DEFAULT 0,
         |    tax_amount DECIMAL(10,2) DEFAULT 0,
         |    total_amount DECIMAL(12,2) NOT NULL,
         |    cost_amount DECIMAL(10,2),
         |    profit_amount DECIMAL(10,2),
         |    sales_channel VARCHAR(50),
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (time_key) REFERENCES dim_time(time_key),
         |    FOREIGN KEY (customer_key) REFERENCES dim_customer(customer_key),
         |    FOREIGN KEY (product_key) REFERENCES dim_product(product_key)
         |);
         |
         |-- =============================================
         |-- MICROSERVICES ARCHITECTURE
         |-- =============================================
         |
         |-- Users service
         |CREATE TABLE users (
         |    user_id BIGINT PRIMARY KEY,
         |    username VARCHAR(100) UNIQUE NOT NULL,
         |    email VARCHAR(255) UNIQUE NOT NULL,
         |    password_hash VARCHAR(255) NOT NULL,
         |    first_name VARCHAR(100),
         |    last_name VARCHAR(100),
         |    phone VARCHAR(20),
         |    status VARCHAR(20) DEFAULT 'ACTIVE',
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE user_preferences (
         |    preference_id BIGINT PRIMARY KEY,
         |    user_id BIGINT NOT NULL,
         |    preference_key VARCHAR(100) NOT NULL,
         |    preference_value TEXT,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
         |    UNIQUE(user_id, preference_key)
         |);
         |
         |CREATE TABLE user_sessions (
         |    session_id VARCHAR(255) PRIMARY KEY,
         |    user_id BIGINT NOT NULL,
         |    session_data TEXT,
         |    ip_address VARCHAR(45),
         |    user_agent TEXT,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    expires_date TIMESTAMP NOT NULL,
         |    is_active BOOLEAN DEFAULT TRUE,
         |
         |    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
         |);
         |
         |-- Warehouses for inventory service
         |CREATE TABLE warehouses (
         |    warehouse_id BIGINT PRIMARY KEY,
         |    warehouse_code VARCHAR(20) UNIQUE NOT NULL,
         |    warehouse_name VARCHAR(255) NOT NULL,
         |    address TEXT,
         |    city VARCHAR(100),
         |    state VARCHAR(100),
         |    country VARCHAR(100),
         |    postal_code VARCHAR(20),
         |    manager_name VARCHAR(255),
         |    capacity INT,
         |    status VARCHAR(20) DEFAULT 'ACTIVE',
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |-- Products for inventory service
         |CREATE TABLE products (
         |    product_id BIGINT PRIMARY KEY,
         |    sku VARCHAR(100) UNIQUE NOT NULL,
         |    product_name VARCHAR(255) NOT NULL,
         |    description TEXT,
         |    category_id INT,
         |    brand VARCHAR(100),
         |    unit_cost DECIMAL(10,2),
         |    selling_price DECIMAL(10,2),
         |    weight DECIMAL(8,3),
         |    dimensions VARCHAR(50),
         |    status VARCHAR(20) DEFAULT 'ACTIVE',
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |-- Stock levels
         |CREATE TABLE stock_levels (
         |    stock_id BIGINT PRIMARY KEY,
         |    product_id BIGINT NOT NULL,
         |    warehouse_id BIGINT NOT NULL,
         |    quantity_on_hand INT NOT NULL DEFAULT 0,
         |    quantity_reserved INT NOT NULL DEFAULT 0,
         |    quantity_available INT ,
         |    reorder_point INT,
         |    max_stock_level INT,
         |    last_stock_check TIMESTAMP,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (product_id) REFERENCES products(product_id),
         |    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
         |    UNIQUE(product_id, warehouse_id)
         |);
         |
         |-- Order status lookup
         |CREATE TABLE order_status (
         |    status_id INT PRIMARY KEY,
         |    status_code VARCHAR(20) UNIQUE NOT NULL,
         |    status_name VARCHAR(50) NOT NULL,
         |    description TEXT,
         |    is_active BOOLEAN DEFAULT TRUE
         |);
         |
         |-- Orders
         |CREATE TABLE orders (
         |    order_id BIGINT PRIMARY KEY,
         |    order_number VARCHAR(50) UNIQUE NOT NULL,
         |    user_id BIGINT NOT NULL,
         |    status_id INT NOT NULL,
         |    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    required_date DATE,
         |    shipped_date DATE,
         |    subtotal DECIMAL(12,2) NOT NULL,
         |    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
         |    shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
         |    total_amount DECIMAL(12,2) NOT NULL,
         |    shipping_address TEXT,
         |    billing_address TEXT,
         |    payment_method VARCHAR(50),
         |    notes TEXT,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (user_id) REFERENCES users(user_id),
         |    FOREIGN KEY (status_id) REFERENCES order_status(status_id)
         |);
         |
         |-- Order items
         |CREATE TABLE order_items (
         |    order_item_id BIGINT PRIMARY KEY,
         |    order_id BIGINT NOT NULL,
         |    product_id BIGINT NOT NULL,
         |    quantity INT NOT NULL,
         |    unit_price DECIMAL(10,2) NOT NULL,
         |    discount_amount DECIMAL(10,2) DEFAULT 0,
         |    line_total DECIMAL(12,2) NOT NULL,
         |    warehouse_id BIGINT,
         |
         |    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
         |    FOREIGN KEY (product_id) REFERENCES products(product_id),
         |    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
         |);
         |
         |-- =============================================
         |-- CONFIGURATION MANAGEMENT
         |-- =============================================
         |
         |CREATE TABLE feature_flags (
         |    flag_id BIGINT PRIMARY KEY,
         |    flag_key VARCHAR(100) UNIQUE NOT NULL,
         |    flag_name VARCHAR(255) NOT NULL,
         |    description TEXT,
         |    is_enabled BOOLEAN DEFAULT FALSE,
         |    environment VARCHAR(50),
         |    target_audience VARCHAR(100),
         |    start_date TIMESTAMP,
         |    end_date TIMESTAMP,
         |    created_by BIGINT,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (created_by) REFERENCES users(user_id)
         |);
         |
         |CREATE TABLE app_configurations (
         |    config_id BIGINT PRIMARY KEY,
         |    config_key VARCHAR(100) NOT NULL,
         |    config_value TEXT,
         |    environment VARCHAR(50) NOT NULL,
         |    data_type VARCHAR(20) DEFAULT 'STRING',
         |    description TEXT,
         |    is_encrypted BOOLEAN DEFAULT FALSE,
         |    is_active BOOLEAN DEFAULT TRUE,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    UNIQUE(config_key, environment)
         |);
         |
         |-- =============================================
         |-- AUDIT AND LOGGING
         |-- =============================================
         |
         |CREATE TABLE audit_logs (
         |    audit_id BIGINT PRIMARY KEY,
         |    user_id BIGINT,
         |    action VARCHAR(100) NOT NULL,
         |    table_name VARCHAR(100),
         |    record_id VARCHAR(100),
         |    old_values JSON,
         |    new_values JSON,
         |    ip_address VARCHAR(45),
         |    user_agent TEXT,
         |    session_id VARCHAR(255),
         |    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (user_id) REFERENCES users(user_id)
         |);
         |
         |CREATE TABLE error_logs (
         |    error_id BIGINT PRIMARY KEY,
         |    error_code VARCHAR(50),
         |    error_message TEXT NOT NULL,
         |    stack_trace TEXT,
         |    severity VARCHAR(20) DEFAULT 'ERROR',
         |    source_system VARCHAR(100),
         |    user_id BIGINT,
         |    session_id VARCHAR(255),
         |    request_url TEXT,
         |    request_method VARCHAR(10),
         |    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    resolved BOOLEAN DEFAULT FALSE,
         |
         |    FOREIGN KEY (user_id) REFERENCES users(user_id)
         |);
         |
         |-- =============================================
         |-- ETL AND DATA INTEGRATION
         |-- =============================================
         |
         |CREATE TABLE staging_customer_data (
         |    staging_id BIGINT PRIMARY KEY,
         |    customer_external_id VARCHAR(100),
         |    customer_name VARCHAR(255),
         |    email VARCHAR(255),
         |    phone VARCHAR(20),
         |    address TEXT,
         |    city VARCHAR(100),
         |    state VARCHAR(100),
         |    country VARCHAR(100),
         |    postal_code VARCHAR(20),
         |    source_system VARCHAR(50),
         |    batch_id VARCHAR(100),
         |    load_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    processed_flag BOOLEAN DEFAULT FALSE,
         |    error_message TEXT
         |);
         |
         |CREATE TABLE data_quality_rules (
         |    rule_id BIGINT PRIMARY KEY,
         |    rule_name VARCHAR(255) NOT NULL,
         |    rule_description TEXT,
         |    table_name VARCHAR(100) NOT NULL,
         |    column_name VARCHAR(100),
         |    rule_type VARCHAR(50) NOT NULL,
         |    rule_expression TEXT NOT NULL,
         |    severity VARCHAR(20) DEFAULT 'ERROR',
         |    is_active BOOLEAN DEFAULT TRUE,
         |    created_by BIGINT,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (created_by) REFERENCES users(user_id)
         |);
         |
         |CREATE TABLE data_quality_results (
         |    result_id BIGINT PRIMARY KEY,
         |    rule_id BIGINT NOT NULL,
         |    batch_id VARCHAR(100),
         |    execution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    total_records_checked INT,
         |    failed_records INT,
         |    success_rate DECIMAL(5,2),
         |    execution_time_ms INT,
         |    status VARCHAR(20),
         |
         |    FOREIGN KEY (rule_id) REFERENCES data_quality_rules(rule_id)
         |);
         |
         |-- =============================================
         |-- COMPLIANCE AND GOVERNANCE
         |-- =============================================
         |
         |CREATE TABLE consent_management (
         |    consent_id BIGINT PRIMARY KEY,
         |    user_id BIGINT NOT NULL,
         |    consent_type VARCHAR(100) NOT NULL,
         |    purpose VARCHAR(255),
         |    is_granted BOOLEAN NOT NULL,
         |    consent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    expiry_date DATE,
         |    withdrawal_date TIMESTAMP,
         |    source_channel VARCHAR(50),
         |    legal_basis VARCHAR(100),
         |
         |    FOREIGN KEY (user_id) REFERENCES users(user_id),
         |    UNIQUE(user_id, consent_type)
         |);
         |
         |CREATE TABLE data_subject_requests (
         |    request_id BIGINT PRIMARY KEY,
         |    user_id BIGINT NOT NULL,
         |    request_type VARCHAR(50) NOT NULL, -- DELETE, EXPORT, RECTIFY
         |    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    status VARCHAR(50) DEFAULT 'PENDING',
         |    completed_date TIMESTAMP,
         |    processed_by BIGINT,
         |    notes TEXT,
         |    verification_method VARCHAR(50),
         |
         |    FOREIGN KEY (user_id) REFERENCES users(user_id),
         |    FOREIGN KEY (processed_by) REFERENCES users(user_id)
         |);
         |
         |-- =============================================
         |-- PERFORMANCE AND MONITORING
         |-- =============================================
         |
         |CREATE TABLE performance_metrics (
         |    metric_id BIGINT PRIMARY KEY,
         |    metric_name VARCHAR(100) NOT NULL,
         |    metric_value DECIMAL(15,4),
         |    metric_unit VARCHAR(20),
         |    system_component VARCHAR(100),
         |    server_name VARCHAR(100),
         |    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    tags JSON
         |);
         |
         |CREATE TABLE real_time_metrics (
         |    metric_id BIGINT PRIMARY KEY,
         |    metric_key VARCHAR(100) NOT NULL,
         |    metric_value DECIMAL(15,4) NOT NULL,
         |    metric_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    dimension_1 VARCHAR(100),
         |    dimension_2 VARCHAR(100),
         |    dimension_3 VARCHAR(100),
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |-- =============================================
         |-- AGGREGATION AND REPORTING TABLES
         |-- =============================================
         |
         |CREATE TABLE daily_sales_summary (
         |    summary_id BIGINT PRIMARY KEY,
         |    summary_date DATE NOT NULL,
         |    product_key INT,
         |    customer_key INT,
         |    total_quantity INT,
         |    total_revenue DECIMAL(15,2),
         |    total_cost DECIMAL(15,2),
         |    total_profit DECIMAL(15,2),
         |    order_count INT,
         |    unique_customers INT,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |
         |    FOREIGN KEY (product_key) REFERENCES dim_product(product_key),
         |    FOREIGN KEY (customer_key) REFERENCES dim_customer(customer_key),
         |    UNIQUE(summary_date, product_key, customer_key)
         |);
         |
         |-- =============================================
         |-- IOT AND DEVICE MANAGEMENT
         |-- =============================================
         |
         |CREATE TABLE device_registry (
         |    device_id BIGINT PRIMARY KEY,
         |    device_serial VARCHAR(100) UNIQUE NOT NULL,
         |    device_type VARCHAR(50) NOT NULL,
         |    device_model VARCHAR(100),
         |    manufacturer VARCHAR(100),
         |    location VARCHAR(255),
         |    installation_date DATE,
         |    last_maintenance DATE,
         |    status VARCHAR(20) DEFAULT 'ACTIVE',
         |    firmware_version VARCHAR(50),
         |    configuration JSON,
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE sensor_readings (
         |    reading_id BIGINT PRIMARY KEY,
         |    device_id BIGINT NOT NULL,
         |    sensor_type VARCHAR(50) NOT NULL,
         |    reading_value DECIMAL(15,6) NOT NULL,
         |    reading_unit VARCHAR(20),
         |    reading_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         |    quality_score DECIMAL(3,2),
         |    is_anomaly BOOLEAN DEFAULT FALSE,
         |
         |    FOREIGN KEY (device_id) REFERENCES device_registry(device_id)
         |);
         |
         |
         |""".stripMargin)

  }

  test("should parse fk with only reference") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    sparkSession.sql(
      s"""
         |COMPILE USL IF NOT EXISTS testdb DEPLOY NAMESPACE lightning.metastore.crm DDL
         |CREATE TABLE dim_time (
         |    time_key SERIAL PRIMARY KEY,
         |    date_actual DATE NOT NULL,
         |    day_name VARCHAR(10),
         |    month_name VARCHAR(10),
         |    quarter_number INT,
         |    year_number INT,
         |    is_weekend BOOLEAN,
         |    is_holiday BOOLEAN
         |);
         |
         |CREATE TABLE dim_customer (
         |    customer_key SERIAL PRIMARY KEY,
         |    customer_id VARCHAR(50) UNIQUE,
         |    customer_name VARCHAR(100),
         |    email VARCHAR(100),
         |    segment VARCHAR(50),
         |    region VARCHAR(50),
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE dim_product (
         |    product_key SERIAL PRIMARY KEY,
         |    product_id VARCHAR(50) UNIQUE,
         |    product_name VARCHAR(100),
         |    category VARCHAR(50),
         |    subcategory VARCHAR(50),
         |    unit_price DECIMAL(10,2),
         |    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
         |
         |CREATE TABLE fact_sales (
         |    sale_key SERIAL PRIMARY KEY,
         |    time_key INT REFERENCES dim_time(time_key),
         |    customer_key INT REFERENCES dim_customer(customer_key),
         |    product_key INT REFERENCES dim_product(product_key),
         |    quantity INT,
         |    unit_price DECIMAL(10,2),
         |    total_amount DECIMAL(10,2),
         |    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
         |);
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
