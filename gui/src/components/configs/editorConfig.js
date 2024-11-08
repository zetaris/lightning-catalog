import ace from 'ace-builds';

// Define custom theme
export const defineCustomTheme = () => {
  ace.define('ace/theme/myCustomTheme', ['require', 'exports', 'module', 'ace/lib/dom'], function (require, exports) {
    exports.isDark = false;
    exports.cssClass = 'ace-myCustomTheme';
    exports.cssText = `
      .ace-myCustomTheme .ace_gutter {
        background: #FFFFFF;
        color: #27A7D2;
      }
      .ace-myCustomTheme .ace_print-margin {
        // display: none;
      }
      .ace-myCustomTheme {
        background-color: #FFFFFF;
        color: #27A7D2;
      }
      .ace-myCustomTheme .ace_cursor {
        color: #134B70;
      }
      .ace-myCustomTheme .ace_marker-layer .ace_selection {
        background: #CECECE;
      }

      /* Colors based on Ace's built-in classes */
      .ace-myCustomTheme .ace_keyword {
        color: #134B70;
        font-weight: bold;
      }
    `;
    var dom = require('ace/lib/dom');
    dom.importCssString(exports.cssText, exports.cssClass);
  });
};

let pathKeywords = {};
let currentSuggestions = [];

export const setPathKeywords = (paths) => {
  paths.forEach((path) => {
    pathKeywords[path] = [path];
  });
};

const onDotTyped = (editorInstance) => {
  let currentLine = editorInstance.getSession().getLine(editorInstance.getCursorPosition().row);

  const lightningIndex = currentLine.indexOf("lightning");
  if (lightningIndex !== -1) {
    currentLine = currentLine.slice(lightningIndex);
  }

  const pathSegments = currentLine.split('.');
  const currentPath = pathSegments.slice(0, -1).join('.');

  const matchingPaths = Object.keys(pathKeywords).filter((path) => path.startsWith(currentPath + "."));
  currentSuggestions = matchingPaths.map((path) => {
    const segments = path.split('.');
    return segments[pathSegments.length - 1];
  }).filter((suggestion, index, self) => suggestion && self.indexOf(suggestion) === index);

  // console.log("Suggestions for", currentPath, ":", currentSuggestions);

  setTimeout(() => {
    editorInstance.execCommand('startAutocomplete');
  }, 0);
};

export const customSQLCompleter = {
  getCompletions: (editor, session, pos, prefix, callback) => {
    const suggestions = currentSuggestions.length > 0
      ? currentSuggestions.map((suggestion) => ({
          caption: suggestion,
          value: suggestion,
          meta: 'suggestion',
          score: 1000
        }))
      : 
      [
        { name: 'SELECT', value: 'SELECT', score: 1000, meta: 'keyword' },
        { name: 'FROM', value: 'FROM', score: 1000, meta: 'keyword' },
        { name: 'WHERE', value: 'WHERE', score: 1000, meta: 'keyword' },
        { name: 'JOIN', value: 'JOIN', score: 1000, meta: 'keyword' },
        { name: 'IN', value: 'IN', score: 1000, meta: 'keyword' },
        { name: 'REGISTER', value: 'REGISTER', score: 1000, meta: 'keyword' },
        { name: 'CREATE', value: 'CREATE', score: 1000, meta: 'keyword' },
        { name: 'TABLE', value: 'TABLE', score: 1000, meta: 'keyword' },
        { name: 'SHOW', value: 'SHOW', score: 1000, meta: 'keyword' },
        { name: 'DESCRIBE', value: 'DESCRIBE', score: 1000, meta: 'keyword' },
        { name: 'INSERT', value: 'INSERT', score: 1000, meta: 'keyword' },
        { name: 'INTO', value: 'INTO', score: 1000, meta: 'keyword' },
        { name: 'VALUES', value: 'VALUES', score: 1000, meta: 'keyword' },
        { name: 'DROP', value: 'DROP', score: 1000, meta: 'keyword' },
        { name: 'IF', value: 'IF', score: 1000, meta: 'keyword' },
        { name: 'EXISTS', value: 'EXISTS', score: 1000, meta: 'keyword' },
        { name: 'REPLACE', value: 'REPLACE', score: 1000, meta: 'keyword' },
        { name: 'PARTITIONED', value: 'PARTITIONED', score: 1000, meta: 'keyword' },
        { name: 'BY', value: 'BY', score: 1000, meta: 'keyword' },
        { name: 'WITH', value: 'WITH', score: 1000, meta: 'keyword' },
        { name: 'ACTIVATE', value: 'ACTIVATE', score: 1000, meta: 'keyword' },
        
        // Functions
        { name: 'lightning', value: 'lightning', score: 1001, meta: 'function' },
        { name: 'datasource', value: 'datasource', score: 1001, meta: 'function' },
        { name: 'metastore', value: 'metastore', score: 1000, meta: 'function' },
        { name: 'NAMESPACES', value: 'NAMESPACES', score: 1000, meta: 'function' },
        { name: 'NAMESPACE', value: 'NAMESPACE', score: 1000, meta: 'function' },
        { name: 'OPTIONS', value: 'OPTIONS', score: 1000, meta: 'function' },
        { name: 'CATALOG', value: 'CATALOG', score: 1000, meta: 'function' },
        { name: 'USL', value: 'USL', score: 1000, meta: 'function' },
      
        // Data types
        { name: 'H2', value: 'H2', score: 1000, meta: 'data-type' },
        { name: 'AVRO', value: 'AVRO', score: 1000, meta: 'data-type' },
        { name: 'CSV', value: 'CSV', score: 1000, meta: 'data-type' },
        { name: 'ORC', value: 'ORC', score: 1000, meta: 'data-type' },
        { name: 'PARQUET', value: 'PARQUET', score: 1000, meta: 'data-type' },
        { name: 'JSON', value: 'JSON', score: 1000, meta: 'data-type' },
        { name: 'JDBC', value: 'JDBC', score: 1000, meta: 'data-type' }
      ];      

    // const dynamicSuggestions = currentSuggestions.map((suggestion) => ({
    //   caption: suggestion,
    //   value: suggestion,
    //   meta: 'dynamic',
    //   score: 1000
    // }));

    callback(null, suggestions);
  }
};

// Add completer to Ace editor
export const setupAceEditor = (editor) => {
  if (!editor) return;

  const languageTools = ace.require('ace/ext/language_tools');
  languageTools.setCompleters([customSQLCompleter]);

  editor.setOptions({
    enableBasicAutocompletion: true,
    enableLiveAutocompletion: true,
  });

  editor.getSession().on('change', (delta) => {
    if (delta.action === 'insert' && delta.lines.join('').includes('.')) {
      onDotTyped(editor);
    }
  });
};

// Editor default options
export const editorOptions = {
  enableBasicAutocompletion: true,
  enableLiveAutocompletion: true,
  enableSnippets: false,
  showLineNumbers: true,
  tabSize: 2,
};

export const queryBookContents = [
  { queryName: 'Create Namespace', description: 'Create a new datasource namespace', query: 'CREATE NAMESPACE lightning.datasource.identifier' },
  { queryName: 'Drop Namespace', description: 'Drop a datasource namespace', query: 'DROP NAMESPACE lightning.datasource.identifier' },
  {
    queryName: 'Register JDBC Datasource', description: 'Register an H2 JDBC datasource', query: `REGISTER [OR REPLACE] JDBC DATASOURCE h2 OPTIONS(
    url "jdbc:h2:mem:dbname;DB_CLOSE_DELAY=-1", user "admin", password "") NAMESPACE lightning.datasource.rdbms` },
  {
    queryName: 'Register CSV Datasource', description: 'Register a CSV datasource', query: `REGISTER OR REPLACE CSV DATASOURCE customers OPTIONS (
    header "true", inferSchema "true", path "/home/zetaris/data/csv/customer.csv") NAMESPACE lightning.datasource.file.csv` },
  {
    queryName: 'Register ORC Datasource', description: 'Register an ORC datasource', query: `REGISTER OR REPLACE ORC DATASOURCE customers OPTIONS (
    header "true", inferSchema "true", path "/home/zetaris/data/orc/customer.orc") NAMESPACE lightning.datasource.file.orc` },
  {
    queryName: 'Register JSON Datasource', description: 'Register a JSON datasource', query: `REGISTER OR REPLACE JSON DATASOURCE customers OPTIONS (
    header "true", inferSchema "true", path "/home/zetaris/data/json/customer.json") NAMESPACE lightning.datasource.file.json` },
  {
    queryName: 'Register PARQUET Datasource', description: 'Register a PARQUET datasource', query: `REGISTER OR REPLACE PARQUET DATASOURCE customers OPTIONS (
    header "true", inferSchema "true", path "/home/zetaris/data/paquet/customer.parquet") NAMESPACE lightning.datasource.file.parquet` },
  {
    queryName: 'Register AVRO Datasource', description: 'Register an AVRO datasource', query: `REGISTER OR REPLACE AVRO DATASOURCE customers OPTIONS (
    header "true", inferSchema "true", path "/home/zetaris/data/avro/customer.avro") NAMESPACE lightning.datasource.file.avro` },
  { queryName: 'Show Namespaces in RDBMS', description: 'Show all namespaces in RDBMS datasource', query: 'SHOW NAMESPACES IN lightning.datasource.rdbms' },
  { queryName: 'Show Namespaces in Iceberg', description: 'Show all namespaces in Iceberg datasource', query: 'SHOW NAMESPACES IN lightning.datasource.iceberg' },
  { queryName: 'Show Namespaces in File', description: 'Show all namespaces in File datasource', query: 'SHOW NAMESPACES IN lightning.datasource.file' },
  { queryName: 'Show Tables in RDBMS Schema', description: 'Show all tables in a particular registered database or datalake', query: 'SHOW TABLES IN lightning.datasource.rdbms.postgres_db' },
  { queryName: 'Show Tables in RDBMS HR Schema', description: 'Show all tables in the HR schema', query: 'SHOW TABLES IN lightning.datasource.rdbms.postgres_db.HR_schema' },
  { queryName: 'Describe Table in RDBMS', description: 'Describe the columns inside the employee table', query: 'DESCRIBE TABLE lightning.datasource.rdbms.postgres_db.HR_schema.employee_table' },
  { queryName: 'Run Query on RDBMS Table', description: 'Run a query on a particular registered database, datalake, or filesource table', query: 'SELECT * FROM lightning.datasource.rdbms.postgres_db.HR_schema.employee_table' },
  { queryName: 'Show Namespaces', description: 'Show all namespaces in the registered H2 database', query: 'SHOW NAMESPACES IN lightning.datasource.rdbms.h2' },
  { queryName: 'Show Tables', description: 'Show all tables in the public schema', query: 'SHOW TABLES in lightning.datasource.rdbms.h2.public' },
  { queryName: 'Describe Table', description: 'Describe the test_users table', query: 'DESCRIBE TABLE lightning.datasource.rdbms.h2.public.test_users' },
  {
    queryName: 'Create Table', description: 'Create a taxis table under NYC schema in H2 database', query: `CREATE TABLE lightning.datasource.rdbms.h2.nyc.taxis (
    vendor_id bigint, trip_id bigint, trip_distance float, fare_amount double, store_and_fwd_flag string)` },
  {
    queryName: 'Insert Data', description: 'Insert records into the taxis table', query: `INSERT INTO lightning.datasource.rdbms.h2.nyc.taxis VALUES 
    (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y")` },
  {
    queryName: 'Federated Query', description: 'Run federated query over multiple source systems', query: `SELECT n_name, o_orderdate, sum(l_extendedprice * (1 - l_discount)) as revenue
    FROM slwh.mssql.azure_mssql.tpch1.customer, lightning.datasource.rdbms.azure_mssql.tpch1.orders,
    lightning.datasource.rdbms.azure_mssql.tpch1.lineitem, lightning.datasource.rdbms.azure_mssql.tpch1.supplier, 
    lightning.datasource.rdbms.azure_mssql.tpch1.nation, lightning.datasource.rdbms.azure_mssql.tpch1.region
    WHERE c_custkey = o_custkey AND l_orderkey = o_orderkey AND l_suppkey = s_suppkey
    AND c_nationkey = s_nationkey AND s_nationkey = n_nationkey AND n_regionkey = r_regionkey
    GROUP BY n_name, o_orderdate` },
  {
    queryName: 'Register Iceberg Datasource', description: 'Register an Iceberg datasource', query: `REGISTER [OR REPLACE] ICEBERG DATASOURCE icebergdb OPTIONS(
    type "hadoop", warehouse "/tmp/iceberg-warehouse") NAMESPACE lightning.datasource.iceberg` },
  {
    queryName: 'Create Iceberg Table', description: 'Create a partitioned taxis table in Iceberg', query: `CREATE TABLE lightning.datasource.iceberg.icebergdb.nytaxis.taxis (
    vendor_id bigint, trip_id bigint, trip_distance float, fare_amount double, store_and_fwd_flag string) PARTITIONED BY (vendor_id)` },
  {
    queryName: 'Insert Iceberg Data', description: 'Insert records into the taxis table in Iceberg', query: `INSERT INTO lightning.datasource.iceberg.icebergdb.nytaxis.taxis VALUES 
    (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y")` },
  { queryName: 'Register H2 to Metastore', description: 'Register H2 schema into the metastore', query: `REGISTER CATALOG all_schema SOURCE lightning.datasource.h2.$dbName NAMESPACE lightning.metastore.h2` },
];

export const queryBookColumns = [
  { accessorKey: 'queryName', header: 'Query Name' },
  { accessorKey: 'description', header: 'Description' },
  { accessorKey: 'query', header: 'Query' },
];
