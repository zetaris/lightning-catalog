# Data Quality
Lightning Catalog have two different type of data quality; one is database constraints check over any data sources,
the other is business data quality by leveraging SQL expression.

## Database constraints:
User can build unified semantic layer by compiling and deploying DDL. A Create table statement can have database constraints, for example,
```bash
create table customer
(
    c_customer_sk             int primary key,
    c_customer_id             string,
    c_current_cdemo_sk        int foreign key references customer_demographics(cd_demo_sk),
    c_current_hdemo_sk        int foreign key references household_demographics(hd_demo_sk),
    c_current_addr_sk         int foreign key references customer_address(ca_address_sk),
    c_first_shipto_date_sk    int foreign key references date_dim(d_date_sk),
    c_first_sales_date_sk     int foreign key references date_dim(d_date_sk),
    c_salutation              string,
    c_first_name              string,
    c_last_name               string,
    c_preferred_cust_flag     string,
    c_birth_day               int,
    c_birth_month             int,
    c_birth_year              int,
    c_birth_country           string,
    c_login                   string,
    c_email_address           string,
    c_last_review_date        string
);
```

All these database constraints(PK, FK, unique) can be checked in either GUI or CLI

### List DQ(CLI)
```bash
LIST DQ USL lightning.metastore.usl.customer_catalog

will output all the registerd database constraints

Name                    Table            Type
---------------------------------------------------------------
c_customer_sk           customer         Primary key constraints
c_current_cdemo_sk      customer         Foreign key constraints
c_current_hdemo_sk      customer         Foreign key constraints
c_current_addr_sk       customer         Foreign key constraints
c_first_shipto_date_sk  customer         Foreign key constraints
c_first_sales_date_sk   customer         Foreign key constraints
```

### Run DQ(CLI)
```bash
RUN DQ `c_customer_sk` TABLE lightning.metastore.usl.customer_catalog.customer

will output DQ result

Name                    Table            Type                     Total_record        Valid_record           invalid_record
-------------------------------------------------------------------------------------------------------------------------
c_customer_sk           customer         Primary key constraints  100,000             100,000                0
```

In this way, user can check database constraints over any data sources (rdbms, iceberg, delta, parquet, json, avro, etc)

## Business Data Quality(CLI)
### use sql expression
```bash
(Register DQ)
REGISTER DQ check_customer_name TABLE lightning.metastore.usl.customer_catalog.customer AS 
c_first_name is not null and length(c_first_name) > 0 and c_last_name is not null and length(c_last_name) > 0;

(Run DQ)
RUN DQ `check_customer_name` TABLE lightning.metastore.usl.customer_catalog.customer

```

### reference other table
```bash
(catalog_sales table)
create table catalog_sales
(
    cs_sold_date_sk           int foreign key references date_dim(d_date_sk),
    cs_sold_time_sk           int foreign key references time_dim(t_time_sk),
    cs_ship_date_sk           int foreign key references date_dim(d_date_sk),
    cs_bill_customer_sk       int foreign key references customer(c_customer_sk),
    cs_bill_cdemo_sk          int foreign key references customer_demographics(cd_demo_sk),
    cs_bill_hdemo_sk          int foreign key references household_demographics(hd_demo_sk),
    cs_bill_addr_sk           int,
    cs_ship_customer_sk       int foreign key references customer(c_customer_sk),
    cs_ship_cdemo_sk          int foreign key references customer_demographics(cd_demo_sk),
    cs_ship_hdemo_sk          int foreign key references household_demographics(hd_demo_sk),
    cs_ship_addr_sk           int,
    cs_call_center_sk         int,
    cs_catalog_page_sk        int,
    cs_ship_mode_sk           int,
    cs_warehouse_sk           int foreign key references warehouse(w_warehouse_sk),
    cs_item_sk                int foreign key references item(i_item_sk),
    cs_promo_sk               int foreign key references promotion(p_promo_sk),
    cs_order_number           int,
    cs_quantity               int,
    cs_wholesale_cost         double,
    cs_list_price             double,
    cs_sales_price            double,
    cs_ext_discount_amt       double,
    cs_ext_sales_price        double,
    cs_ext_wholesale_cost     double,
    cs_ext_list_price         double,
    cs_ext_tax                double,
    cs_coupon_amt             double,
    cs_ext_ship_cost          double,
    cs_net_paid               double,
    cs_net_paid_inc_tax       double,
    cs_net_paid_inc_ship      double,
    cs_net_paid_inc_ship_tax  double,
    cs_net_profit             double
);
  
(warehouse table)
create table warehouse
(
    w_warehouse_sk            int  primary key,
    w_warehouse_id            string,
    w_warehouse_name          string,
    w_warehouse_sq_ft         int,
    w_street_number           string,
    w_street_name             string,
    w_street_type             string,
    w_suite_number            string,
    w_city                    string,
    w_county                  string,
    w_state                   string,
    w_zip                     string,
    w_country                 string,
    w_gmt_offset              double
);

The below DQ checks warehouse id(cs_warehouse_sk) should exist in warehouse table
   
REGISTER DQ warehouse_should_exist TABLE lightning.metastore.usl.customer_catalog.catalog_sales AS
cs_warehouse_sk IN (SELECT w_warehouse_sk FROM lightning.metastore.usl.customer_catalog.warehouse)

RUN DQ warehouse_should_exist TABLE lightning.metastore.usl.customer_catalog.catalog_sales
```

## export (in)valid records
Lighting catalog provide API endpoint to export (in)valid records :
```bash
http://localhost:8080/api/edq?name=c_customer_sk&table=lightning.metastore.usl.customer_catalog.customer&validRecord=true
```
