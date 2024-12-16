# Unified Semantic Layer(USL)
A unified semantic layer is a business-friendly abstraction of data that provides a consistent view of data across an organization. 
It maps data from various sources into a single, unified view that can be used for analytics and other business purposes

## A unified semantic layer can:
### Simplify querying
By mapping complex data into business terms, a semantic layer makes data more useful and easier to query.

### Improve collaboration
A semantic layer can help data teams and business users work together more effectively by fostering a common understanding of data.

### Enhance data governance
A semantic layer can help ensure consistent definitions and metrics, which can lead to better communication and alignment.

### Empower self-service analytics
Business users can explore and analyze data independently without relying on IT.
Some benefits of a unified semantic layer include: Accelerated time to insight, Enhanced data freshness, Increased agility and flexibility, and Cost savings.

## USL in Lightning Catalog 
In Lightning Catalog, a user should come up with DDLs, Create table statement, with context awareness name(table, column, constraints).
And then, Lighting Catalog can compile DDLs into JSON representation, deploy it over underlying data source.

## Compile DDLs
![Compile DDL](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/compile-ddl.png)

## Load ERD
![Load ERD](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/usl-main.png)

## Deploy(Activate) table
![Deploy](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/activate-table.png)

## Run query over activated table
![Running query](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/preview-table.png)

## Register Data Quality
![Register DQ](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/register-dq.png)

## Run Data Quality
![Run DQ](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/run-dq.png)

## Export Invalid(valid) records
![Export (in)valid records](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/export-invalid-records.png)
