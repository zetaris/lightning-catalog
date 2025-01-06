import ace from 'ace-builds';
import Fuse from 'fuse.js';
import sqlKeywords from '../../utils/sql_keywords.json';
import { fetchApi } from '../../utils/common';
import { StateMachine } from './stateMachine';

export const defineCustomTheme = () => {
  ace.define('ace/theme/myCustomTheme', ['require', 'exports', 'module', 'ace/lib/dom'], function (require, exports) {
    exports.isDark = false;
    exports.cssClass = 'ace-myCustomTheme';
    exports.cssText = `
      /* Background and Gutter */
      .ace-myCustomTheme .ace_gutter {
        color: #A3C0CE;
        letter-spacing: normal !important;
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
export let currentSuggestions = [];
export const initSuggestions = () => {
  currentSuggestions = [];
}

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

export const getContext = (editorInstance) => {
  const cursorPosition = editorInstance.getCursorPosition();
  const cursorRow = cursorPosition.row;
  const cursorColumn = cursorPosition.column;
  const session = editorInstance.getSession();

  const allLines = session.getLines(0, cursorRow);
  const currentLineBefore = session.getLine(cursorRow).slice(0, cursorColumn);
  allLines.push(currentLineBefore);
  const beforeCursor = allLines.join('\n');

  const totalLines = session.getLength();
  const afterLines = session.getLines(cursorRow, totalLines);
  const currentLineAfter = session.getLine(cursorRow).slice(cursorColumn);
  afterLines.unshift(currentLineAfter);
  const afterCursor = afterLines.join('\n');

  const context = { type: null, path: null, tableAlias: null, tablePath: null, insideFunction: false, session, cursorRow, beforeCursor };

  const patterns = {
    "LIGHTNING": /\bLIGHTNING\b(?:\.(\w+(?:\.\w+)*))?/ig,
    "SELECT": /SELECT\s+(.*?)(?=\bFROM\b|$)/ig,
    "FROM": /FROM\s+([\w]+(?:\.[\w]+)*)(?:\s+AS\s+(\w+))?/ig,
    // "WHERE": /WHERE\s+([^FROM]+)/ig,
    // "WHERE": /WHERE\s+([^\n]+)/ig,
    "WHERE": /WHERE\s+([^;]*?)(?=\s+(?:SELECT|GROUP BY|HAVING|ORDER BY|LIMIT)|$)/ig,
    "GROUP BY": /GROUP\s+BY\s+([^;]*?)(?=\s+(?:SELECT|WHERE|HAVING|ORDER BY|LIMIT)|$)/ig,
    "HAVING": /HAVING\s+([^;]*?)(?=\s+(?:SELECT|WHERE|GROUP BY|ORDER BY|LIMIT)|$)/ig,
    "LIMIT": /LIMIT\s+([^;]*?)(?=\s+(?:SELECT|WHERE|GROUP BY|HAVING|ORDER BY)|$)/ig
    // "GROUP BY": /GROUP\s+BY\s+([^FROM]+)/ig,
    // "HAVING": /HAVING\s+([^FROM]+)/ig
  };

  const matches = [];

  for (const [type, regex] of Object.entries(patterns)) {
    let match;
    while ((match = regex.exec(beforeCursor)) !== null) {
      const matchRow = beforeCursor.slice(0, match.index).split('\n').length - 1;
      const matchColumn = match.index - beforeCursor.lastIndexOf('\n', match.index) - 1;
      let distance = Math.abs(cursorRow - matchRow) * 100 + Math.abs(cursorColumn - matchColumn);

      if (["SELECT", "FROM", "WHERE"].includes(type)) {
        distance = distance - (match[0].length || 0);
        if (distance < 0) distance = 0;
      }

      matches.push({ type, match, row: matchRow, column: matchColumn, distance });
    }
  }

  // console.log(matches)

  for (const [type, regex] of Object.entries(patterns)) {
    let match;
    while ((match = regex.exec(afterCursor)) !== null) {
      const matchRow = cursorRow + afterCursor.slice(0, match.index).split('\n').length - 1;
      const matchColumn = match.index - afterCursor.lastIndexOf('\n', match.index) - 1;
      let distance = Math.abs(cursorRow - matchRow) * 100 + Math.abs(cursorColumn - matchColumn);

      if (["SELECT", "FROM", "WHERE"].includes(type)) {
        distance = distance - (match[0].length || 0);
        if (distance < 0) distance = 0;
      }

      matches.push({ type, match, row: matchRow, column: matchColumn, distance });
    }
  }

  if (matches.length === 0) {
    return context;
  }

  // Find the closest match based on row and column
  const closestMatch = matches.reduce((prev, current) => {
    return prev.distance <= current.distance ? prev : current;
  });

  switch (closestMatch.type) {
    case "LIGHTNING":
      context.type = "LIGHTNING";
      const matchedPath = closestMatch.match[1];
      context.path = matchedPath ? `lightning.${matchedPath}` : "lightning";
      break;
    case "FROM":
      context.type = "FROM";
      context.path = closestMatch.match[1];
      context.tableAlias = closestMatch.match[2] || null;
      break;
    case "SELECT":
      context.type = "SELECT";
      const selectedColumns = closestMatch.match[1].trim();

      const selectAliasMatches = [...selectedColumns.matchAll(/(\w+)\./gi)];
      let lastSelectAliasMatch = selectAliasMatches.length > 0
        ? selectAliasMatches[selectAliasMatches.length - 1][1]
        : null;

      const tokens = selectedColumns.split(/[, ]+/).filter(t => t.length > 0);
      const lastToken = tokens[tokens.length - 1];

      if (lastToken && tableAliases[lastToken] && lastToken !== lastSelectAliasMatch) {
        lastSelectAliasMatch = lastToken;
      }

      if (lastSelectAliasMatch) {
        context.tableAlias = tableAliases[lastSelectAliasMatch] || null;
      } else {
        const fromMatch = afterCursor.match(/FROM\s+([\w\.]+)(?:\s+AS\s+(\w+))?/i);
        if (fromMatch) {
          context.tablePath = fromMatch[1];
        }
      }
      break;
    case "WHERE":
    case "GROUP BY":
    case "HAVING":
    case "LIMIT":
      context.type = "WHERE";
      const whereConditions = closestMatch.match[1];

      // console.log(closestMatch)
      // console.log(whereConditions)

      const cleanedConditions = whereConditions
        .split(/\s*(GROUP\s+BY|HAVING|LIMIT|WHERE)\s+/i)[0]
        .trim();

      const whereAliasMatches = [...cleanedConditions.matchAll(/(\w+)\./g)];

      const lastWord = whereConditions.trim().split(/\s+/).pop().replace(/\.$/, '');

      const sqlKeywords = ['WHERE', 'GROUP', 'BY', 'HAVING', 'SELECT', 'FROM', 'AND', 'OR', 'ON', 'WHE', 'G'];
      const whereAliasList = [
        ...whereAliasMatches.map(match => match[1]),
        lastWord
      ].filter(word => !sqlKeywords.includes(word.toUpperCase()));

      let lastWhereAliasMatch = whereAliasList.length > 0
        ? whereAliasList[whereAliasList.length - 1]
        : null;

      if (tableAliases[cleanedConditions]) {
        lastWhereAliasMatch = cleanedConditions;
      }

      // console.log(tableAliases)
      // console.log(cleanedConditions)
      // console.log(lastWhereAliasMatch)

      if (lastWhereAliasMatch) {
        context.tableAlias = tableAliases[lastWhereAliasMatch] || null;
      } else if (Object.keys(tableAliases).length === 0) {
        const fromMatches = [...beforeCursor.matchAll(/FROM\s+([\w\.]+)(?:\s+AS\s+(\w+))?/ig)];
        const lastFromMatch = fromMatches[fromMatches.length - 1];
        if (lastFromMatch) {
          context.tablePath = lastFromMatch[1];
        }
      }
      break;
    default:
      break;
  }

  let clauseText = '';
  if (context.type === "SELECT") {
    const selectMatch = beforeCursor.match(/SELECT\s+([^FROM]+)/i);
    clauseText = selectMatch ? selectMatch[1] : '';
  } else if (context.type === "WHERE") {
    const whereMatch = beforeCursor.match(/WHERE\s+([^FROM]+)/i);
    clauseText = whereMatch ? whereMatch[1] : '';
  }

  const textUpToCursor = clauseText.slice(0, closestMatch.match.index + closestMatch.match[0].length);
  const openParens = (textUpToCursor.match(/\(/g) || []).length;
  const closeParens = (textUpToCursor.match(/\)/g) || []).length;

  context.insideFunction = openParens > closeParens;

  return context;
};

export const fetchTablesOrNamespaces = async (queryPath) => {
  try {
    if (queryPath === "lightning" || queryPath === "lightning.") {
      currentSuggestions = ["datasource", "metastore"];
      return currentSuggestions;
    }

    const query = `SHOW NAMESPACES OR TABLES IN ${queryPath};`;
    const response = await fetchApi(query);

    if (response.message?.startsWith("[SCHEMA_NOT_FOUND]") || response.message?.includes("doesn't support list namespaces")) {
      const columnsResponse = await fetchColumns(queryPath);
      return currentSuggestions;
    } else {
      const results = response.map((item) => JSON.parse(item));
      currentSuggestions = results.map((result) => result.name || '');
      return currentSuggestions;
    }
  } catch (error) {
  }
};

export const fetchColumns = async (tablePath) => {
  try {
    const query = `DESC ${tablePath};`;
    const response = await fetchApi(query);

    if (response) {
      const results = response.map((item) => JSON.parse(item));
      currentSuggestions = results.map((result) => result.col_name || '');
      return currentSuggestions;
    }
  } catch (error) {
  }
};

export const customSQLCompleter = {
  getCompletions: (editor, session, pos, prefix, callback) => {
    const { keywords, lightning, dataTypes, builtIn } = sqlKeywords;
    const onlyShowSuggestions = globalStateMachine?.onlyShowSuggestions;
    const suggestionsFromServer = currentSuggestions;

    let suggestions;
    if (onlyShowSuggestions && suggestionsFromServer.length > 0) {
      suggestions = suggestionsFromServer.map((suggestion) => ({
        caption: suggestion,
        value: suggestion,
        meta: 'suggestion',
        score: 1000
      }));
    } else {
      const keywordSuggestions = [
        ...keywords.map((kw) => ({ caption: kw, value: kw, meta: "keyword" })),
        ...lightning.map((lt) => ({ caption: lt, value: lt, meta: "lightning" })),
        ...dataTypes.map((dt) => ({ caption: dt, value: dt, meta: "dataType" })),
        ...builtIn.map((ct) => ({ caption: ct, value: ct, meta: "builtIn" })),
      ];

      const dynamicSuggestions = suggestionsFromServer.map((suggestion) => ({
        caption: suggestion,
        value: suggestion,
        meta: 'suggestion',
        score: 1000
      }));

      suggestions = [...keywordSuggestions, ...dynamicSuggestions];
    }

    callback(null, suggestions);
  },
};

let globalStateMachine;
export const setupAceEditor = (editor) => {
  if (!editor) return;

  editor.session.setMode("ace/mode/custom_sql");

  const languageTools = ace.require('ace/ext/language_tools');
  languageTools.setCompleters([customSQLCompleter]);

  editor.setOptions({
    enableBasicAutocompletion: true,
    enableLiveAutocompletion: true,
  });

  let lastAction = null;
  let debounceTimer = null;
  const stateMachine = new StateMachine();
  globalStateMachine = stateMachine;

  editor.getSession().on("change", (delta) => {
    const { action, lines } = delta;
    const insertedText = lines.join('');
    const context = getContext(editor);
    stateMachine.transition({ type: 'CLAUSE_CHANGED' }, context);

    if (action === "insert") {
      const insertedText = lines.join('');

      let eventType = null;
      if (insertedText === ".") {
        eventType = 'DOT_TYPED';
      } else if (insertedText === " ") {
        eventType = 'SPACE_TYPED';
      } else if (insertedText === "(" || insertedText === "()") {
        eventType = 'BRACKET_TYPED';
      } else if (/[\w]/.test(insertedText)) {
        // CHAR_TYPED debounce
        if (debounceTimer) clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
          stateMachine.transition({ type: 'CHAR_TYPED' }, { ...context, editor })
            .then(() => {
              const suggestions = stateMachine.getSuggestions();
              if (suggestions && suggestions.length > 0) {
                editor.execCommand('startAutocomplete');
              }
            });
        }, 100);
        return;
      } else {
        eventType = 'CLAUSE_CHANGED';
      }

      // DOT_TYPED, SPACE_TYPED, BRACKET_TYPED, CLAUSE_CHANGED
      if (eventType) {
        stateMachine.transition({ type: eventType }, context).then(() => {
          const suggestions = stateMachine.getSuggestions();
          if (suggestions && suggestions.length > 0) {
            editor.execCommand('startAutocomplete');
          }
        });
      }
    }

    if (action === 'remove') {
      stateMachine.clearSuggestions();
      if (debounceTimer) clearTimeout(debounceTimer);
    }
  });
};

export const extractSelectFromToWhere = (cursorRow, session) => {
  const allLines = session.getLines(0, cursorRow + 1);
  const upperContent = allLines.join('\n').toUpperCase();
  const originalContent = allLines.join('\n');

  const selectIndex = upperContent.lastIndexOf('SELECT');
  if (selectIndex === -1) return null;

  const fromIndex = upperContent.indexOf('FROM', selectIndex);
  if (fromIndex === -1) return null;

  const whereIndex = upperContent.indexOf('WHERE', fromIndex);

  const fromClause = originalContent.substring(
    fromIndex,
    whereIndex !== -1 ? whereIndex : undefined
  ).trim();

  return fromClause;
};

export let tableAliases = {};

export const analyzingFrom = (fromToWhere) => {
  Object.keys(tableAliases).forEach(key => delete tableAliases[key]);

  const tables = fromToWhere.split(',').map((table) => table.trim());

  tables.forEach((table) => {
    const aliasMatch = table.match(/(\S+)\s+(?:AS\s+)?(\w+)$/i);
    if (aliasMatch) {
      const tablePath = aliasMatch[1];
      const alias = aliasMatch[2];
      tableAliases[alias] = tablePath;
    }
  });
};

export const extractFromToWhere = (cursorRow, session) => {
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

export const editorOptions = {
  enableBasicAutocompletion: true,
  enableLiveAutocompletion: true,
  enableSnippets: false,
  showLineNumbers: true,
  wrap: true,
  wrapBehavioursEnabled: true,
  behavioursEnabled: true,
  // tabSize: 2,
  tabSize: 4,
  useSoftTabs: true,
  cursorStyle: 'ace',
};

export const tablePathSet = new Set();

export const fetchPathData = async (path) => {
  try {
    if (path === "lightning" || path === "lightning.") {
      currentSuggestions = ["datasource", "metastore"];
      return currentSuggestions;
    }

    if (tablePathSet.has(path)) {
      const query = `DESC ${path};`;
      const response = await fetchApi(query);
      if (response) {
        const results = response.map((item) => JSON.parse(item));
        currentSuggestions = results.map((result) => result.col_name || '');
        return currentSuggestions;
      }
    }

    const query = `SHOW NAMESPACES OR TABLES IN ${path};`;
    const response = await fetchApi(query);

    if (!response?.length || response.message?.startsWith("[SCHEMA_NOT_FOUND]") || response.message?.includes("doesn't support list namespaces")) {
      const descResponse = await fetchColumns(path);

      if (descResponse) {
        tablePathSet.add(path);
      }

      return currentSuggestions;
    } else {
      const results = response.map((item) => JSON.parse(item));

      results.forEach(result => {
        if (result.type === 'table') {
          tablePathSet.add(`${path}.${result.name}`);
        }
      });

      currentSuggestions = results.map((result) => result.name || '');
      return currentSuggestions;
    }
  } catch (error) {
    // console.error('Error in fetchPathData:', error);
    return [];
  }
};

export const clearTablePathSet = () => {
  tablePathSet.clear();
};

export const removeTablePath = (path) => {
  tablePathSet.delete(path);
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
