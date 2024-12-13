import { fetchColumns, fetchTablesOrNamespaces, extractFromToWhere, extractSelectFromToWhere, analyzingFrom, tableAliases, getContext, initSuggestions, currentSuggestions as globalCurrentSuggestions } from './editorConfig';
import sqlKeywords from '../../utils/sql_keywords.json';


export class StateMachine {
    constructor() {
        this.currentState = 'INITIAL';
        this.currentSuggestions = [];
        this.tableAliases = {};
        this.onlyShowSuggestions = false;
    }

    async transition(event, context) {

        switch (event.type) {
            case 'CLAUSE_CHANGED':
                this.onlyShowSuggestions = false;
                this.handleClauseChanged(context); 
                break;
            case 'DOT_TYPED':
                this.onlyShowSuggestions = true;
                await this.handleDotTyped(context); 
                break;
            case 'SPACE_TYPED':
                this.onlyShowSuggestions = true;
                await this.handleSpaceTyped(context);
                break;
            case 'BRACKET_TYPED':
                this.onlyShowSuggestions = true;
                await this.handleBracketTyped(context);
                break;
            case 'CHAR_TYPED':
                this.onlyShowSuggestions = false;
                await this.handleCharTyped(context);
                break;
            default:
                break;
        }
    }

    handleClauseChanged(context) {
        if (context.type === 'SELECT') {
            this.currentState = 'SELECT';
            const fromToWhere = extractSelectFromToWhere(context.cursorRow, context.session);
            if (fromToWhere) {
                analyzingFrom(fromToWhere);
            }
        } else if (context.type === 'FROM') {
            this.currentState = 'FROM';
            const fromToWhere = extractFromToWhere(context.cursorRow, context.session);
            if (fromToWhere) {
                analyzingFrom(fromToWhere);
            }
        } else if (context.type === 'WHERE') {
            this.currentState = 'WHERE';
            const fromToWhere = extractFromToWhere(context.cursorRow, context.session);
            if (fromToWhere) {
                analyzingFrom(fromToWhere);
            }
        } else if (context.type === 'LIGHTNING') {
            this.currentState = 'LIGHTNING';
        } else {
            this.currentState = 'INITIAL';
        }

        this.currentSuggestions = [];
    }

    async handleDotTyped(context) {
        switch (this.currentState) {
            case 'SELECT':
            case 'WHERE':
                await this.fetchColumnsForContext(context);
                break;
            case 'FROM':
            case 'LIGHTNING':
                await this.fetchTablesForContext(context);
                break;
            default:
                break;
        }
    }

    async handleSpaceTyped(context) {
        switch (this.currentState) {
            case 'SELECT':
            case 'WHERE':
                await this.fetchColumnsForContext(context);
                break;
            case 'FROM':
            case 'LIGHTNING':
                await this.fetchTablesForContext(context);
                break;
            default:
                break;
        }
    }

    async handleBracketTyped(context) {
        switch (this.currentState) {
            case 'SELECT':
            case 'WHERE':
                await this.fetchColumnsForContext(context);
                break;
            case 'FROM':
            case 'LIGHTNING':
                await this.fetchTablesForContext(context);
                break;
            default:
                break;
        }
    }

    async handleCharTyped(context) {

        if (!context.tableAlias && !context.tablePath && context.type !== "LIGHTNING") {
            this.currentSuggestions = [];
            initSuggestions();
        }

        const newContext = getContext(context.editor);
        const userInput = newContext.beforeCursor.trim().split(/\s+/).pop().toLowerCase();

        const isKeywordAlreadySuggested = Array.isArray(this.currentSuggestions) && this.currentSuggestions.some((suggestion) =>
            suggestion.toLowerCase().startsWith(userInput)
        );

        if (isKeywordAlreadySuggested) { return; }

        if ((this.currentState === 'SELECT' || this.currentState === 'WHERE') && (newContext.tableAlias || newContext.tablePath)) {
            const queryPath = newContext.tableAlias
                ? (tableAliases[newContext.tableAlias] || newContext.tableAlias)
                : newContext.tablePath;
            if (queryPath) {
                await this.fetchColumnsForContext({ tablePath: queryPath });
            }
        }

        const matchedSuggestions = Array.isArray(this.currentSuggestions) && this.currentSuggestions.filter((suggestion) =>
            suggestion.toLowerCase().startsWith(userInput)
        );
      
        if (matchedSuggestions.length === 0) {
          context.editor.execCommand('startAutocomplete');
        }

        // if (matchedSuggestions.length === 0) {
        //     initSuggestions();
        //     const { keywords, lightning, dataTypes, builtIn } = sqlKeywords;
        //     this.currentSuggestions = [
        //         ...this.currentSuggestions,
        //         ...keywords,
        //         ...lightning,
        //         ...dataTypes,
        //         ...builtIn,
        //     ];
        //     context.editor.execCommand('startAutocomplete');
        // }
    }

    async fetchColumnsForContext(context) {
        const queryPath = context.tableAlias || context.tablePath;
        if (queryPath) {
            const columns = await fetchColumns(queryPath);
            this.currentSuggestions = columns;
        } else {
            this.currentSuggestions = [];
        }
    }

    async fetchTablesForContext(context) {
        const queryPath = context.tablePath || context.path;
        if (queryPath) {
            const tables = await fetchTablesOrNamespaces(queryPath);
            this.currentSuggestions = tables;
        } else {
            this.currentSuggestions = [];
        }
    }

    getSuggestions() {
        return this.currentSuggestions;
    }

    clearSuggestions() {
        this.currentSuggestions = [];
    }
}
