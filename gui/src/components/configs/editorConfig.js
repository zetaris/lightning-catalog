import ace from 'ace-builds';
import Fuse from 'fuse.js';
import sqlKeywords from '../../utils/sql_keywords.json';
import { fetchApi } from '../../utils/common';

export const defineCustomTheme = () => {
  ace.define('ace/theme/myCustomTheme', ['require', 'exports', 'module', 'ace/lib/dom'], function (require, exports) {
    exports.isDark = false;
    exports.cssClass = 'ace-myCustomTheme';
    exports.cssText = `
      /* Background and Gutter */
      .ace-myCustomTheme .ace_gutter {
        color: #A3C0CE;
      }
      .ace-myCustomTheme {
        background-color: #FFFFFF;
        color: #134B70;
      }
      .ace-myCustomTheme .ace_cursor {
        color: #134B70;
      }
      .ace-myCustomTheme .ace_marker-layer .ace_selection {
        background: rgba(39, 167, 210, 0.2);
      }

      /* Highlighted Text */
      .ace-myCustomTheme .ace_marker-layer .ace_active-line {
        background: #EAF5FB;
      }

      /* Custom Colors for Syntax */
      .ace-myCustomTheme .ace_keyword {
        color: #C96868; /* Keywords in dark blue */
        font-weight: bold;
      }
      .ace-myCustomTheme .ace_reserved-word {
        color: #1C7794; /* Reserved words in a medium blue */
        font-weight: bold;
      }
      .ace-myCustomTheme .ace_lightning {
        color: #27A7D2; /* Project main color for functions */
        font-weight: bold;
      }
      .ace-myCustomTheme .ace_suggestion {
        color: #27A7D2; /* Project main color for functions */
        font-weight: bold;
      }
      .ace-myCustomTheme .ace_function {
        color: #27A7D2; /* Project main color for functions */
        font-weight: bold;
      }
      .ace-myCustomTheme .ace_dataType {
        color: #15B392; /* Data types in project color, italicized */
        font-style: italic;
        font-weight: bold;
      }
      .ace-myCustomTheme .ace_builtIn {
        color: #D28445; /* Data types in project color, italicized */
        font-style: italic;
        font-weight: bold;
      }
      .ace-myCustomTheme .ace_identifier {
        color: #134B70;
      }

      /* Comments */
      .ace-myCustomTheme .ace_comment {
        color: #606676;
        font-style: italic;
      }

      /* String literals */
      .ace-myCustomTheme .ace_string {
        color: #D28445;
      }

      /* Constants like numbers */
      .ace-myCustomTheme .ace_constant.ace_numeric {
        color: #C47F26;
      }
    `;
    var dom = require('ace/lib/dom');
    dom.importCssString(exports.cssText, exports.cssClass);
  });
};

const updateHighlightRulesFromJSON = (sqlKeywords) => {
  const { keywords, lightning, dataTypes, builtIn } = sqlKeywords;

  return [
    {
      token: "keyword",
      regex: `\\b(?:${keywords.join('|')})\\b`
    },
    {
      token: "lightning",
      regex: `\\b(?:${lightning.join('|')})\\b`
    },
    {
      token: "dataType",
      regex: `\\b(?:${dataTypes.join('|')})\\b`
    },
    {
      token: "builtIn",
      regex: `\\b(?:${builtIn.join('|')})\\b`
    },
    {
      token: "identifier",
      regex: "\\w+"
    },
    {
      token: "comment",
      regex: "--.*$"
    },
    {
      token: "comment",
      start: "/\\*",
      end: "\\*/"
    }
  ];
};

ace.define("ace/mode/custom_sql_highlight_rules", ["require", "exports", "ace/mode/text_highlight_rules"], function (require, exports) {
  const TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

  const CustomSqlHighlightRules = function () {
    this.$rules = {
      "start": updateHighlightRulesFromJSON(sqlKeywords)
    };

    this.normalizeRules();
  };

  CustomSqlHighlightRules.metaData = {
    fileTypes: ["sql"],
    name: "CustomSqlHighlightRules"
  };

  ace.require("ace/lib/oop").inherits(CustomSqlHighlightRules, TextHighlightRules);
  exports.CustomSqlHighlightRules = CustomSqlHighlightRules;
});

ace.define("ace/mode/custom_sql", ["require", "exports", "ace/mode/sql", "ace/mode/custom_sql_highlight_rules"], function (require, exports) {
  const oop = require("ace/lib/oop");
  const SqlMode = require("ace/mode/sql").Mode;
  const CustomSqlHighlightRules = require("ace/mode/custom_sql_highlight_rules").CustomSqlHighlightRules;

  const CustomSqlMode = function () {
    SqlMode.call(this);
    this.HighlightRules = CustomSqlHighlightRules;
  };

  oop.inherits(CustomSqlMode, SqlMode);

  exports.Mode = CustomSqlMode;
});

let fuse;
let pathKeywords = {};
let currentSuggestions = [];

const initializeFuse = (paths) => {
  const options = {
    includeScore: true,
    threshold: 0.7,
    keys: ['path'],
  };

  fuse = new Fuse(paths.map((path) => ({ path })), options);
};

export const setPathKeywords = (paths) => {
  pathKeywords = {};
  paths.forEach((path) => {
    pathKeywords[path] = path;
  });

  initializeFuse(Object.keys(pathKeywords));
};

// const onDotTyped = async (editorInstance) => {
//   const editorPosition = editorInstance.getCursorPosition();
//   const currentLine = editorInstance.getSession().getLine(editorPosition.row);
//   currentSuggestions = [];

//   const exportPath = (() => {
//     const match = currentLine.trim().match(/.*lightning\..+/);
//     if (match) {
//       const lastLightningIndex = match[0].lastIndexOf('lightning.');
//       const lastLightningSegment = match[0].slice(lastLightningIndex);
//       return lastLightningSegment.split('.').slice(0, -1).join('.');
//     }
//     return '';
//   })();  

//   let queryPath = exportPath;

//   const beforeCursor = currentLine.slice(0, editorPosition.column);
//   const lastSegment = beforeCursor.split('.').slice(-1)[0].trim().split(/[\s]+/).pop();

//   if (tableAliases[lastSegment]) {
//     queryPath = tableAliases[lastSegment];
//   }

//   if (queryPath === '') {
//     currentSuggestions = ['datasource', 'metastore']
//   } else if (queryPath) {
//     try {
//       const query = `SHOW NAMESPACES OR TABLES IN ${queryPath};`;
//       const response = await fetchApi(query);

//       if (response.message && response.message.startsWith("[SCHEMA_NOT_FOUND]")) {

//         const descQuery = `DESC ${queryPath};`;
//         const descResponse = await fetchApi(descQuery);

//         if (descResponse) {
//           const descResults = descResponse.map((item) => JSON.parse(item));

//           currentSuggestions = descResults.map((result) => result.col_name || '');
//         }
//       } else {
//         const results = response.map((item) => JSON.parse(item));

//         currentSuggestions = results.map((result) => result.name || '');
//       }
//     } catch (error) {
//       console.error('Error fetching namespaces or tables:', error);
//     }
//   }

//   if (currentSuggestions.length > 0) {
//     setTimeout(() => {
//       editorInstance.execCommand('startAutocomplete');
//     }, 0);
//   }
// };

const onDotTyped = async (editorInstance) => {
  const editorPosition = editorInstance.getCursorPosition();
  const currentLine = editorInstance.getSession().getLine(editorPosition.row).trim();
  const beforeCursor = currentLine.slice(0, editorPosition.column).trim();

  currentSuggestions = [];
  let queryPath = '';
  let context = getContext(beforeCursor);

  if (context.type === "SELECT") {
    queryPath = context.tableAlias || context.tablePath;

    if (queryPath) {
      await fetchColumns(queryPath);
    }
  } else if (context.type === "FROM") {
    queryPath = context.path;

    if (queryPath) {
      await fetchTablesOrNamespaces(queryPath);
    }
  } else if (context.type === "WHERE") {
    queryPath = context.tableAlias || context.tablePath;

    if (queryPath) {
      await fetchColumns(queryPath);
    }
  }

  if (currentSuggestions.length > 0) {
    setTimeout(() => {
      editorInstance.execCommand('startAutocomplete');
    }, 0);
  }
};

const getContext = (beforeCursor) => {
  const context = { type: null, path: null, tableAlias: null, tablePath: null };

  const upperCaseCursor = beforeCursor.toUpperCase();

  if (upperCaseCursor.includes("WHERE")) {
    context.type = "WHERE";

    const lastSegment = beforeCursor.split(/[\s.]+/).pop();
    context.tableAlias = tableAliases[lastSegment] || null;
  }else if (upperCaseCursor.includes("FROM")) {
    context.type = "FROM";

    const match = beforeCursor.match(/FROM\s+([\w.]+)$/i);
    if (match) {
      context.path = match[1];
    }
  }else if (upperCaseCursor.includes("SELECT")) {
    context.type = "SELECT";

    const lastSegment = beforeCursor.split(/[\s.]+/).pop();
    context.tableAlias = tableAliases[lastSegment] || null;
  }

  return context;
};

const fetchTablesOrNamespaces = async (queryPath) => {
  try {
    const query = `SHOW NAMESPACES OR TABLES IN ${queryPath};`;
    const response = await fetchApi(query);

    if (response.message?.startsWith("[SCHEMA_NOT_FOUND]")) {
      console.error("Schema not found:", response.message);
    } else {
      const results = response.map((item) => JSON.parse(item));
      currentSuggestions = results.map((result) => result.name || '');
    }
  } catch (error) {
    console.error("Error fetching tables or namespaces:", error);
  }
};

const fetchColumns = async (tablePath) => {
  try {
    const query = `DESC ${tablePath};`;
    const response = await fetchApi(query);

    if (response) {
      const results = response.map((item) => JSON.parse(item));
      currentSuggestions = results.map((result) => result.col_name || '');
    }
  } catch (error) {
    console.error("Error fetching columns:", error);
  }
};

const nonDotTyped = async (editorInstance, tableName = '') => {
  let currentLine = editorInstance.getSession().getLine(editorInstance.getCursorPosition().row);
  currentSuggestions = [];

  if (tableName) {
    // const pathSegments = currentLine.trim().split(/[\s.]+/);
    // const aliasOrPath = pathSegments.slice(-2, -1).join('.');

    // resolvedPath = pathKeywords[aliasOrPath] || aliasOrPath;

    // if (resolvedPath.includes('.')) {
    //   const segments = resolvedPath.split('.');
    //   resolvedPath = segments[segments.length - 1];
    // }

    const descQuery = `DESC ${queryPath};`;
    const descResponse = await fetchApi(descQuery);

    if (descResponse) {
      const descResults = descResponse.map((item) => JSON.parse(item));

      currentSuggestions = descResults.map((result) => result.col_name || '');
    }
  }

  // Extract the path to query
  const queryPath = currentLine.trim().split('.').slice(0, -1).join('.');

  if (queryPath) {
    try {
      const query = `SHOW NAMESPACES OR TABLES IN ${queryPath};`;
      const response = await fetchApi(query);

      if (response.message && response.message.startsWith("[SCHEMA_NOT_FOUND]")) {

        const descQuery = `DESC ${queryPath};`;
        const descResponse = await fetchApi(descQuery);

        if (descResponse) {
          const descResults = descResponse.map((item) => JSON.parse(item));

          currentSuggestions = descResults.map((result) => result.col_name || '');
        }
      } else {
        const results = response.map((item) => JSON.parse(item));

        currentSuggestions = results.map((result) => result.name || '');
      }

      if (currentSuggestions.length > 0) {
        setTimeout(() => {
          editorInstance.execCommand('startAutocomplete');
        }, 5);
      }
    } catch (error) {
      console.error('Error fetching namespaces or tables:', error);
    }
  }
};

export const customSQLCompleter = {
  getCompletions: (editor, session, pos, prefix, callback) => {
    const { keywords, lightning, dataTypes, builtIn } = sqlKeywords;

    const suggestions = currentSuggestions.length > 0
      ? currentSuggestions.map((suggestion) => ({
        caption: suggestion,
        value: suggestion,
        meta: 'suggestion',
        score: 1000
      }))
      : [
        ...currentSuggestions.map((suggestion) => ({
          caption: suggestion,
          value: suggestion,
          meta: 'suggestion',
          score: 1000,
        })),
        ...keywords.map((kw) => ({ caption: kw, value: kw, meta: "keyword" })),
        ...lightning.map((lt) => ({ caption: lt, value: lt, meta: "lightning" })),
        ...dataTypes.map((dt) => ({ caption: dt, value: dt, meta: "dataType" })),
        ...builtIn.map((ct) => ({ caption: ct, value: ct, meta: "builtIn" })),
      ];

    callback(null, suggestions);
  },
};

export const setupAceEditor = (editor) => {
  if (!editor) return;

  editor.session.setMode("ace/mode/custom_sql");

  const languageTools = ace.require('ace/ext/language_tools');
  languageTools.setCompleters([customSQLCompleter]);

  editor.setOptions({
    enableBasicAutocompletion: true,
    enableLiveAutocompletion: true,
  });

  editor.getSession().on("change", (delta) => {
    const { action, lines } = delta;
    const cursorPosition = editor.getCursorPosition();
    const cursorRow = cursorPosition.row;
    const session = editor.getSession();
    let fromToWhere;

    if (action === "insert") {
      const content = editor.getValue();
      const upperContent = content.toUpperCase();

      if (upperContent.includes("WHERE")) {
        fromToWhere = extractFromToWhere(content, cursorRow, session);
        if (fromToWhere) {
          analyzingFrom(fromToWhere);
        }
      }
    }

    if (action === 'insert') {
      const currentLine = lines.join("");
      onDotTyped(editor);
      // if (fromToWhere && !currentLine.endsWith(".")) {
      //   onDotTyped(editor);
      // }
    } else if (action === 'remove') {
      resetSuggestions();
    }
  });
};

const tableAliases = {};
const analyzingFrom = (fromToWhere) => {
  const tables = fromToWhere.split(',').map((table) => table.trim());

  tables.forEach((table) => {
    const aliasMatch = table.match(/(\S+)\s+(AS\s+)?(\w+)$/i);

    if (aliasMatch) {
      const tablePath = aliasMatch[1];
      const alias = aliasMatch[3];
      tableAliases[alias] = tablePath;

      addColumnsToSuggestions(tablePath, alias);
    } else {
      const segments = table.split('.');
      const tableName = segments[segments.length - 1];
      tableAliases[tableName] = table;

      addColumnsToSuggestions(table, tableName);
    }
  });

  return tableAliases;
};

const addColumnsToSuggestions = (tablePath, alias) => {
  const columns = pathKeywords[tablePath];

  if (alias) {
    pathKeywords[alias] = tablePath;
  }

  if (Array.isArray(columns)) {
    columns.forEach((column) => {
      const suggestion = alias ? `${alias}.${column}` : `${tablePath}.${column}`;
      currentSuggestions.push({
        caption: column,
        value: suggestion,
        meta: "column",
        score: 1000,
      });
    });
  }
};

const extractFromToWhere = (content, cursorRow, session) => {
  const allLines = session.getLines(0, cursorRow);
  const upperLines = allLines.map((line) => line.toUpperCase());

  let fromLine = null;
  let whereLine = null;

  for (let i = upperLines.length - 1; i >= 0; i--) {
    if (!whereLine && upperLines[i].includes("WHERE")) {
      whereLine = i;
    }
    if (!fromLine && upperLines[i].includes("FROM")) {
      fromLine = i;
    }
    if (fromLine !== null && whereLine !== null) break;
  }

  if (fromLine !== null && (whereLine === null || fromLine < whereLine)) {
    const fromToWhereText = allLines.slice(fromLine, whereLine === null ? cursorRow + 1 : whereLine).join(" ");
    return fromToWhereText.trim();
  }

  return null;
};

const resetSuggestions = () => {
  currentSuggestions = [];
};

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
