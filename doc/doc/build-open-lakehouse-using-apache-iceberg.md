## Data Lakehouse Revolution
The data lakehouse is a relatively new approach to data management and analytics that combines elements of both data lakes and data warehouses.
It's aimed at addressing the limitations and challenges associated with traditional data lakes and warehouses, offering a more unified and flexible platform for data storage, processing, and analytics.

* **Integration of Data Lake and Data Warehouse**:
  A data lakehouse brings together the scalability and cost-effectiveness of a data lake with the structure and performance of a data warehouse.  
  It allows organizations to store vast amounts of raw, unstructured data (like a data lake) while also providing capabilities for structured querying, schema enforcement, and optimized analytics (like a data warehouse).

* **Unified Data Platform**: 
  Instead of maintaining separate systems for storing and processing raw and processed data, a data lakehouse provides a single, unified platform where data engineers, data scientists, and analysts can access, transform, and analyze data across the entire data lifecycle.  
  This eliminates the need for data movement between disparate systems, reducing complexity and improving data agility.

* **Schema Enforcement and Evolution**: One of the challenges of traditional data lakes is the lack of schema enforcement, which can lead to data inconsistency and poor query performance.  
  In a data lakehouse, organizations can enforce schema on read or schema on write, ensuring data quality and compatibility with downstream analytics tools. Moreover, data schemas can evolve over time to accommodate changing business requirements without disrupting existing data pipelines.

* **Support for Real-Time and Batch Processing**: A data lakehouse supports both real-time and batch processing of data, enabling organizations to ingest, process, and analyze data in near real-time for time-sensitive applications such as fraud detection, recommendation systems, and operational monitoring. This versatility allows organizations to derive insights from data at varying velocities without relying on separate processing frameworks.

* **Scalability and Cost Efficiency**: By leveraging cloud-native technologies and storage services, a data lakehouse offers horizontal scalability and cost-efficient storage options, allowing organizations to scale their data infrastructure according to demand and optimize costs based on usage patterns. This scalability ensures that organizations can handle growing volumes of data without incurring significant infrastructure overhead.

* **Advanced Analytics Capabilities**: With a data lakehouse, organizations can perform advanced analytics tasks such as machine learning, AI, and predictive analytics directly on the same platform where data is stored, eliminating the need to extract and transform data for analysis in separate environments. This tight integration streamlines the analytics workflow and accelerates time-to-insight for data-driven decision-making.

## Open Lakehouse 
Open Lakehouse utilizes open sources, community driven components Apache Iceberg or Delta lake as table format, Apache Spark or Trino as a Query Engine  
By leveraging open lakehouse technologies, organizations can benefit the maximum flexibility to build their infrastructure according to their unique needs while eliminating the constraints of vendor lock-in  
  
Apache Spark and Apache Iceberg are two main players in this open source revolution, offering powerful tools and capabilities of Data Lakehouse.

## Apache Iceberg
https://iceberg.apache.org/
Here are some outstanding features that make Apache Iceberg adopted by most companies.
* **ACID Transactions**    
  Apache Iceberg brings ACID (Atomicity, Consistency, Isolation, Durability) transactions for the operations like creating, inserting, updating, and deleting data tables. 
  
* **Schema Evolution**  
  With Schema Evolution, a table can evolve seamlessly without the need for a full rewrite.
  it supports add columns, remove columns, rename columns, or change column types as your requirements change over time.
  
* **Partition Evolution**  
  table can be modified with different partition strategy without the need to rewrite the entire table.

* **Hidden Partition**  
  Typical partitioning patterns like month, day, hour, and year, or even custom truncation and bucketing, are seamlessly tracked

* **Time-Travel**  
  Apache Iceberg allow you to query tables at any valid snapshot in time. 
  
## Lakehouse Example : TPCDS table.

### 1. create namespace for iceberg lakehouse  
create iceberg.tpcds
```bash
CREATE NAMESPACE lightning.datasource.iceberg;
```

### 3. register iceberg datasource
```bash
REGISTER OR REPLACE ICEBERG DATASOURCE tpcds OPTIONS(
  type "hadoop",
  warehouse "/tpcds" -- endpoint of any object storage
) NAMESPACE lightning.datasource.iceberg
````

### 3. create tables
```bash
-- call_center table
CREATE TABLE lightning.datasource.iceberg.tpcds.call_center
(
cc_call_center_sk         int,
cc_call_center_id         string,
cc_rec_start_date         string,
cc_rec_end_date           string,
cc_closed_date_sk         int,
cc_open_date_sk           int,
cc_name                   string,
cc_class                  string,
cc_employees              int,
cc_sq_ft                  int,
cc_hours                  string,
cc_manager                string,
cc_mkt_id                 int,
cc_mkt_class              string,
cc_mkt_desc               string,
cc_market_manager         string,
cc_division               int,
cc_division_name          string,
cc_company                int,
cc_company_name           string,
cc_street_number          string,
cc_street_name            string,
cc_street_type            string,
cc_suite_number           string,
cc_city                   string,
cc_county                 string,
cc_state                  string,
cc_zip                    string,
cc_country                string,
cc_gmt_offset             double,
cc_tax_percentage         double
)
--partition clause
PARTITIONED BY (cc_name)
```

### 3-1. create tables ... as select
```bash
CREATE TABLE lightning.datasource.iceberg.tpcds.call_center
--partition clause
PARTITIONED BY (cc_name)
AS SELECT ....
```

### 4. insert records
```bash
INSERT INTO lightning.datasource.iceberg.tpcds.call_center
VALUES (1, ....), (2, ....), ....
```

### 4-1. insert records as select
```bash
INSERT INTO lightning.datasource.iceberg.tpcds.call_center
SELECT ....
```

### 5. Run queries
```bash
-- start query 1 in stream 0 using template query1.tpl and seed QUALIFICATION
with customer_total_return as (
    select sr_customer_sk as ctr_customer_sk,  sr_store_sk as ctr_store_sk,  sum(SR_RETURN_AMT) as ctr_total_return
    from lightning.datasource.iceberg.tpcds.store_returns,
         lightning.datasource.iceberg.tpcds.date_dim
    where sr_returned_date_sk = d_date_sk
    and d_year = 2000
    group by sr_customer_sk,  sr_store_sk
)
select c_customer_id
from customer_total_return ctr1,
     lightning.datasource.iceberg.tpcds.store,
     lightning.datasource.iceberg.tpcds.customer
where ctr1.ctr_total_return > (
    select avg(ctr_total_return) * 1.2
    from customer_total_return ctr2
    where ctr1.ctr_store_sk = ctr2.ctr_store_sk
)
and s_store_sk = ctr1.ctr_store_sk
and s_state = 'TN'
and ctr1.ctr_customer_sk = c_customer_sk
order by c_customer_id
limit 100;
```

## Iceberg resources
DDL  
https://iceberg.apache.org/docs/nightly/spark-ddl/  
Queries  
https://iceberg.apache.org/docs/nightly/spark-writes/  
Write(insert, update, delete)  
https://iceberg.apache.org/docs/nightly/spark-writes/
