import React, { useRef, useState, useEffect } from 'react';
import Resizable from 'react-resizable-layout';
import { initializeJsPlumb, setupTableForSelectedTable, connectEndpoints, handleOptimizeView, handleZoomIn, handleZoomOut, getColumnConstraint, getRowInfo, addForeignKeyIconToColumn } from '../configs/JsPlumbConfig';
import { v4 as uuidv4 } from 'uuid';
import { fetchApi, fetchActivateTableApi } from '../../utils/common';
import './Contents.css';
import './SemanticLayer.css'
import RelationshipModal from './RelationshipModal';
import { MaterialReactTable } from 'material-react-table';
import { ReactComponent as MinusIcon } from '../../assets/images/square-minus-regular.svg';
import { ReactComponent as PlusIcon } from '../../assets/images/square-plus-regular.svg';
import { ReactComponent as LocationIcon } from '../../assets/images/location-crosshairs-solid.svg';
import { ReactComponent as CircleCheck } from '../../assets/images/circle-check-solid.svg';
import { ReactComponent as Exclamation } from '../../assets/images/circle-exclamation-solid.svg';
import { ReactComponent as Spinner } from '../../assets/images/spinner-solid.svg';
import { TableInfoSlider } from './TableInfoSlider';
import DataQualityPopup from './components/DataQualityPopup.js';
import DataQualityListPopup from './components/DataQualityListPopup.js';
import ActivePopup from './components/ActivatePopup.js';

function SemanticLayer({ selectedTable, semanticLayerInfo, uslNamebyClick }) {
    const jsPlumbRef = useRef(null);
    const jsPlumbInstanceRef = useRef(null);
    const [condition, setCondition] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedConnection, setSelectedConnection] = useState(null);
    const [selectedRelationship, setSelectedRelationship] = useState('');
    const [relationshipType, setRelationshipType] = useState('');
    const [queryResult, setQueryResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [rulesData, setRulesData] = useState({
        title: ["new Column Name", "sort Order", "condition"],
        columns: [],
        columnTableIds: [],
    });
    const [zoomLevel, setZoomLevel] = useState(1);
    const [isDragging, setIsDragging] = useState(false);
    const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
    const [offset, setOffset] = useState({
        x: parseFloat(localStorage.getItem('offsetX')) || 0,
        y: parseFloat(localStorage.getItem('offsetY')) || 0,
    });
    const [isSliderOpen, setIsSliderOpen] = useState(false);
    const [selectedTableInfo, setSelectedTableInfo] = useState(null);
    const [editorContent, setEditorContent] = useState('');
    const [showDQPopup, setShowDQPopup] = useState(false);
    const [showDQListPopup, setShowDQListPopup] = useState(false);
    const [showActivePopup, setShowActivePopup] = useState(false);
    const [activateTable, setactivateTable] = useState(false);
    const [activateTargetTable, setActivateTargetTable] = useState(null);
    const [popupMessage, setPopupMessage] = useState(null);
    const [activateTables, setActivateTables] = useState(null);
    const resizingRef = useRef(false);
    const [dqResults, setDQResults] = useState([]);
    const [uslName, setUslName] = useState('');
    const [viewMode, setViewMode] = useState('');
    let selectedRowData = [];
    const [pagination, setPagination] = useState({
        pageIndex: 0,
        pageSize: 30,
    });

    const reSizingOffset = 115;

    const closePopup = () => setPopupMessage(null);

    const handleCloseDQPopup = () => {
        setShowDQPopup(false);
    };

    const handleCloseDQListPopup = () => {
        setShowDQListPopup(false);
    };

    const handleCloseActivePopup = () => {
        setShowActivePopup(false);
    };

    const handlePopupSubmit = (data) => {
    };

    const loadDQ = async () => {
        setCondition('');
        const savedTables = JSON.parse(localStorage.getItem("savedTables"));
        const processedNamespaces = new Set();
        const dqResults = [];

        for (const table of savedTables) {
            const tableName = table.name;
            const namespace = tableName.split('.').slice(0, 4).join('.');

            if (processedNamespaces.has(namespace)) continue;

            const listDQQuery = `LIST DQ USL ${namespace}`;
            const listDQResult = await fetchApi(listDQQuery);
            if (!listDQResult) continue;

            const dqRules = listDQResult.map(rule => JSON.parse(rule));

            for (const dq of dqRules) {
                const dqEntry = {
                    Name: dq.name,
                    Table: `${namespace}.${dq.table}`,
                    Type: dq.type,
                    Expression: dq.expression,
                    Total_record: 'N/A',
                    Good_record: 'N/A',
                    Bad_record: 'N/A',
                    Status: 'N/A',
                    isChecked: false
                };

                if (dq.type === 'Custom Data Quality') {
                    dqResults.unshift(dqEntry);
                } else {
                    dqResults.push(dqEntry);
                }
            }

            processedNamespaces.add(namespace);
        }

        setDQResults(dqResults);
        setViewMode('dq')
    };

    const runDQ = async () => {
        let updatedDQResults = dqResults.map((dqEntry) => ({
            ...dqEntry,
            Status: 'N/A',
            Total_record: 'N/A',
            Good_record: 'N/A',
            Bad_record: 'N/A',
            errorMessage: '',
        }));

        setDQResults(updatedDQResults);

        for (const dqEntry of selectedRowData) {
            dqEntry.Status = 'loading';
            dqEntry.Total_record = 'fetching data...';
            dqEntry.Good_record = 'fetching data...';
            dqEntry.Bad_record = 'fetching data...';

            updatedDQResults = updatedDQResults.map((entry) =>
                entry.Name === dqEntry.Name ? dqEntry : entry
            );
            setDQResults([...updatedDQResults]);

            const runDQQuery = `RUN DQ ${dqEntry.Name} TABLE ${dqEntry.Table}`;

            try {
                const runDQResult = await fetchApi(runDQQuery);

                if (runDQResult?.error) {
                    dqEntry.Total_record = '';
                    dqEntry.Good_record = '';
                    dqEntry.Bad_record = '';
                    dqEntry.Status = 'error';
                    dqEntry.errorMessage = runDQResult.message;
                } else {
                    const resultData = runDQResult?.[0] && JSON.parse(runDQResult[0]);
                    dqEntry.Total_record = resultData?.total_record || '0';
                    dqEntry.Good_record = resultData?.good_record || '0';
                    dqEntry.Bad_record = resultData?.bad_record || '0';

                    dqEntry.Status = dqEntry.Bad_record !== '0' ? 'warning' : 'success';
                    dqEntry.errorMessage = '';
                }
            } catch (error) {
                dqEntry.Total_record = '';
                dqEntry.Good_record = '';
                dqEntry.Bad_record = '';
                dqEntry.Status = 'error';
                dqEntry.errorMessage = error.message || 'An unexpected error occurred';
            }

            updatedDQResults = updatedDQResults.map((entry) =>
                entry.Name === dqEntry.Name ? dqEntry : entry
            );
            setDQResults([...updatedDQResults]);
        }
    };

    const handleTableDoubleClick = () => {
        // console.log("handleTableDoubleClick");
    }

    const handleListDQClick = (table) => {
        setActivateTargetTable(table);
        setShowDQListPopup(true);
    }

    const handleDataQualityButtonClick = (table) => {
        setActivateTargetTable(table);
        setShowDQPopup(true);
    };

    const handleEditorChange = (newContent) => {
        setEditorContent(newContent);
    };

    const handleActivateQueryClick = async (table) => {
        setActivateTargetTable(table);
        setactivateTable(true);

        if (!activateTable) {
            setShowActivePopup(true);
        }
    }

    const handleActivateTableClick = (table) => {
        setActivateTargetTable(table);
        setactivateTable(true);

        if (!activateTable) {
            setShowActivePopup(true);
        }
    };

    const handleSubmitActivateQuery = async (query) => {
        const activateKeyword = 'ACTIVATE USL TABLE ';
        const asKeyword = ' AS ';

        const startIndex = query.expression.indexOf(activateKeyword) + activateKeyword.length;
        const endIndex = query.expression.indexOf(asKeyword);

        if (startIndex === -1 || endIndex === -1) {
            console.error("Invalid query expression: Required keywords not found.");
            return;
        }

        const table = query.expression.substring(startIndex, endIndex).trim();
        const queryIndex = query.expression.indexOf('SELECT');
        if (queryIndex === -1) {
            console.error("Invalid query expression: SELECT statement not found.");
            return;
        }

        const selectQuery = query.expression.substring(queryIndex);
        const requestData = {
            table: table,
            query: selectQuery
        };

        let result = await fetchActivateTableApi(requestData);
        if (!result.error) {
            setActivateTables((prevTables) => {
                const updatedTables = [...prevTables, table];
                updateActivatedTables(true, updatedTables);
                return updatedTables;
            });
        } else {
            setPopupMessage(result.message);
            updateActivatedTables(false);
        }
        // fetchActivateTableData();
        setShowActivePopup(false);
    };

    const handleTableInfoClick = (table) => {
        showTableInfo(table);
    };

    const showTableInfo = (tableInfo) => {
        setSelectedTableInfo(tableInfo);
        setIsSliderOpen(true);
    };

    const closeSlider = () => {
        setIsSliderOpen(false);
        setSelectedTableInfo(null);
    };

    const handleMouseDown = (e) => {
        setIsDragging(true);
        setDragStart({ x: e.clientX, y: e.clientY });
    };

    const handleMouseUpForDrag = () => {
        setIsDragging(false);

        localStorage.setItem('offsetX', offset.x);
        localStorage.setItem('offsetY', offset.y);
    };

    const handleMouseMoveForDrag = (e) => {
        if (isDragging) {
            const deltaX = e.clientX - dragStart.x;
            const deltaY = e.clientY - dragStart.y;

            setOffset((prevOffset) => ({
                x: prevOffset.x + deltaX,
                y: prevOffset.y + deltaY,
            }));

            setDragStart({ x: e.clientX, y: e.clientY });
        }
    };

    const handleMouseMove = (e) => {
        if (resizingRef.current) {
            e.preventDefault();
        }
    };

    const handleMouseUp = () => {
        resizingRef.current = false;
    };

    useEffect(() => {
        window.addEventListener('mousemove', handleMouseMove);
        window.addEventListener('mouseup', handleMouseUp);

        return () => {
            window.removeEventListener('mousemove', handleMouseMove);
            window.removeEventListener('mouseup', handleMouseUp);
        };
    }, []);

    useEffect(() => {
        if (semanticLayerInfo && semanticLayerInfo.length > 0) {
            clearJsPlumbAndLocalStorage();

            const fetchAndParseDDL = async () => {
                try {
                    const name = semanticLayerInfo[0].name;
                    const ddl = semanticLayerInfo[0].ddl;
                    const parsedDDLResult = await compileUSL(name, ddl, true);
                    if (parsedDDLResult) {
                        const { savedTables, savedConnections, rulesData } = getSettingDataFromJson(parsedDDLResult);
                        restoreFromTablesAndConnections(savedTables, savedConnections);
                        window.location.reload();
                    }
                } catch (error) {
                    console.error("Error while parsing DDL:", error);
                }
            };

            fetchAndParseDDL();
        }
    }, [semanticLayerInfo]);

    useEffect(() => {
        const fetchUSLContent = async () => {
            clearJsPlumbAndLocalStorage();

            const dbname = uslNamebyClick.split('.').pop();
            const path = uslNamebyClick.split('.').slice(0, -1).join('.');

            if (!uslNamebyClick) return;

            let query = `LOAD USL ${dbname} NAMESPACE ${path}`;

            try {
                const result = await fetchApi(query);
                if (result) {
                    const { savedTables, savedConnections, rulesData } = getSettingDataFromJson(result);
                    restoreFromTablesAndConnections(savedTables, savedConnections);
                }
            } catch (error) {
                console.error("Error fetching USL file content:", error);
            }
        };

        if (uslNamebyClick) {
            fetchUSLContent();

            const savedTables = JSON.parse(localStorage.getItem('savedTables'));

            if (savedTables && savedTables.length > 0) {
                setUslName(savedTables[0].name.split('.').slice(0, -1).join('.'));
            }

            handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset);
        }
    }, [uslNamebyClick]);

    useEffect(() => {
        window.addEventListener('mousemove', handleMouseMoveForDrag);
        window.addEventListener('mouseup', handleMouseUpForDrag);

        return () => {
            window.removeEventListener('mousemove', handleMouseMoveForDrag);
            window.removeEventListener('mouseup', handleMouseUpForDrag);
        };
    }, [isDragging, dragStart]);

    useEffect(() => {
        reFreshScreen();

        const savedZoomLevel = localStorage.getItem('zoomLevel');
        const savedOffsetX = localStorage.getItem('offsetX');
        const savedOffsetY = localStorage.getItem('offsetY');

        if (savedZoomLevel) {
            setZoomLevel(parseFloat(savedZoomLevel));
        }

        if (savedOffsetX && savedOffsetY) {
            setOffset({ x: parseFloat(savedOffsetX), y: parseFloat(savedOffsetY) });
        }

        const tableContainers = document.querySelectorAll('.table-container');

        tableContainers.forEach((table) => {
            const tableId = table.id;
            const savedPosition = localStorage.getItem(tableId);

            if (savedPosition) {
                const { top, left } = JSON.parse(savedPosition);
                table.style.top = `${top}px`;
                table.style.left = `${left}px`;
            }
        });

        if (!savedZoomLevel || !savedOffsetX || !savedOffsetY) {
            handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset);
        }

    }, []);

    useEffect(() => {
        updateActivatedTables(true, activateTables);
    }, [activateTables]);

    useEffect(() => {
        const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];

        if (selectedTable && jsPlumbInstanceRef.current) {
            const isAlreadySaved = savedTables.some(
                (table) => table.name === selectedTable.name
            );

            if (!isAlreadySaved) {
                const uuid = uuidv4();
                const tableWithUuid = { ...selectedTable, uuid };

                requestAnimationFrame(() => {
                    setupTableForSelectedTable(jsPlumbRef.current, tableWithUuid, jsPlumbInstanceRef.current, uuid, false, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleActivateQueryClick, handleDataQualityButtonClick, handleTableDoubleClick, handleListDQClick);
                    jsPlumbInstanceRef.current.recalculateOffsets(jsPlumbRef.current);
                    jsPlumbInstanceRef.current.repaint();

                    savedTables.push(tableWithUuid);
                    localStorage.setItem('savedTables', JSON.stringify(savedTables));

                    const updatedDDL = generateDDLJsonFromSettingDatas(savedTables, JSON.parse(localStorage.getItem('connections')) || []);
                });
            }
        }

    }, [selectedTable]);

    function updateActivatedTables(isActivate, activeTables = []) {
        const captions = document.querySelectorAll('.caption-text');
        const validActiveTables = Array.isArray(activeTables) ? activeTables : [];

        captions.forEach((caption) => {
            const tableElement = caption.closest('.table-container');
            if (tableElement) {
                if (isActivate && validActiveTables.includes(tableElement.classList[1])) {
                    tableElement.classList.add('activated-table');
                    const header = tableElement.querySelector('.table-header');
                    const titles = tableElement.querySelectorAll('.table-title');

                    if (header) {
                        header.classList.replace('table-header', 'table-activate-header');
                    }
                    titles.forEach((title) => {
                        title.classList.replace('table-title', 'table-activate-title');
                    });
                } else if (!isActivate) {
                    tableElement.classList.remove('activated-table');
                    const header = tableElement.querySelector('.table-activate-header');
                    const titles = tableElement.querySelectorAll('.table-activate-title');

                    if (header) {
                        header.classList.replace('table-activate-header', 'table-header');
                    }
                    titles.forEach((title) => {
                        title.classList.replace('table-activate-title', 'table-title');
                    });
                }
            }
        });
    }


    const reFreshScreen = () => {
        const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];
        let savedConnections = JSON.parse(localStorage.getItem('connections')) || [];

        if (!jsPlumbInstanceRef.current && jsPlumbRef.current) {
            jsPlumbInstanceRef.current = initializeJsPlumb(jsPlumbRef.current, [], openModal, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleActivateQueryClick, handleDataQualityButtonClick, handleTableDoubleClick, handleListDQClick);
        }

        if (savedTables.length > 0 && jsPlumbInstanceRef.current) {
            const activatedTableNames = [];
            savedTables.forEach((table) => {
                setupTableForSelectedTable(jsPlumbRef.current, table, jsPlumbInstanceRef.current, table.uuid, false, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleActivateQueryClick, handleDataQualityButtonClick, handleTableDoubleClick, handleListDQClick);

                if (table.isActivated) {
                    activatedTableNames.push(table.name);
                }

                // Add tooltips to referenced columns
                table.desc.forEach((col) => {
                    if (col.foreignKey) {
                        const refTableName = col.foreignKey.refTable.join('.');
                        const refColumnName = col.foreignKey.refColumns[0]; // Assume a single-column foreign key

                        // Find the target table and column
                        const targetTable = savedTables.find(t => t.name === refTableName);
                        if (targetTable) {
                            const targetColumn = targetTable.desc.find(c => c.col_name === refColumnName);
                            if (targetColumn) {
                                // Add tooltip to the referenced column
                                const tooltipData = `References: ${table.name}.${col.col_name}`;
                                addForeignKeyIconToColumn(
                                    targetColumn.element, // Ensure targetColumn includes an `element` property linked to DOM
                                    tooltipData,
                                    `foreignKey (${col.col_name})`
                                );
                            }
                        }
                    }
                });
            });
            setActivateTables(activatedTableNames);

            requestAnimationFrame(() => {
                if (savedConnections.length > 0) {
                    savedConnections.forEach(({ sourceId, targetId, relationship, relationship_type }, index) => {
                        connectEndpoints(jsPlumbInstanceRef.current, sourceId, targetId, relationship, relationship_type, false);
                        const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);
                    
                        const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
                        const targetColumnName = targetColumn.querySelector('td')?.innerText || '';
                        const sourceColumnClass = sourceColumn.children[0].classList[0];
                        const constraints = getColumnConstraint(sourceColumnClass);
                    
                        let tooltipData;
                        if (constraints) {
                            tooltipData = constraints.map((constraint) => {
                                const reference = constraint.references ? `(${constraint.references})` : '';
                                return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
                            }).join(', ');
                        }
                    
                        const newTooltipData = tooltipData
                            ? `${tooltipData}, foreignKey: (${targetColumnName})`
                            : `foreignKey: (${targetColumnName})`;
                    
                        if (relationship === 'fk') {
                            const existingTooltipData = sourceColumn.getAttribute('data-tooltip') || '';
                    
                            const combinedTooltipArray = [
                                ...new Set([...existingTooltipData.split(', '), newTooltipData].filter(Boolean)),
                            ];
                    
                            const combinedTooltipData = combinedTooltipArray.join(', ');
                    
                            sourceColumn.setAttribute('data-tooltip', combinedTooltipData);
                    
                            addForeignKeyIconToColumn(sourceColumn, combinedTooltipData, tooltipData);
                        }
                    });                                 
                }

                jsPlumbInstanceRef.current.recalculateOffsets(jsPlumbRef.current);
            });

            jsPlumbInstanceRef.current.repaintEverything();
        }
    };

    const clearJsPlumbAndLocalStorage = () => {
        if (jsPlumbInstanceRef.current) {
            jsPlumbInstanceRef.current.deleteEveryEndpoint();
            jsPlumbInstanceRef.current.reset();
        }

        document.querySelectorAll('.table-container').forEach((element) => {
            element.remove();
        });

        Object.keys(localStorage).forEach((key) => {
            if (key.startsWith('table-')) {
                localStorage.removeItem(key);
            }
        });

        localStorage.removeItem('savedTables');
        localStorage.removeItem('connections');
    };

    const restoreFromTablesAndConnections = (savedTables, savedConnections) => {
        if (!savedTables || !savedConnections || !Array.isArray(savedTables) || !Array.isArray(savedConnections)) {
            console.error("Invalid saved tables or connections input");
            return;
        }

        if (jsPlumbInstanceRef.current) {
            savedTables.forEach((table) => {
                setupTableForSelectedTable(
                    jsPlumbRef.current,
                    table,
                    jsPlumbInstanceRef.current,
                    table.uuid,
                    false,
                    handlePreViewButtonClick,
                    handleTableInfoClick,
                    handleActivateTableClick,
                    handleActivateQueryClick,
                    handleDataQualityButtonClick,
                    handleTableDoubleClick,
                    handleListDQClick,
                );
            });

            requestAnimationFrame(() => {
                savedConnections.forEach(({ sourceId, targetId, relationship, relationship_type }, index) => {
                    connectEndpoints(jsPlumbInstanceRef.current, sourceId, targetId, relationship, relationship_type, false);
                    const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);
                
                    const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
                    const targetColumnName = targetColumn.querySelector('td')?.innerText || '';
                    const sourceColumnClass = sourceColumn.children[0].classList[0];
                    const constraints = getColumnConstraint(sourceColumnClass);
                
                    let tooltipData;
                    if (constraints) {
                        tooltipData = constraints.map((constraint) => {
                            const reference = constraint.references ? `(${constraint.references})` : '';
                            return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
                        }).join(', ');
                    }
                
                    const newTooltipData = tooltipData
                        ? `${tooltipData}, foreignKey: (${targetColumnName})`
                        : `foreignKey: (${targetColumnName})`;
                
                    if (relationship === 'fk') {
                        const existingTooltipData = sourceColumn.getAttribute('data-tooltip') || '';
                
                        const combinedTooltipArray = [
                            ...new Set([...existingTooltipData.split(', '), newTooltipData].filter(Boolean)),
                        ];
                
                        const combinedTooltipData = combinedTooltipArray.join(', ');
                
                        sourceColumn.setAttribute('data-tooltip', combinedTooltipData);
                
                        addForeignKeyIconToColumn(sourceColumn, combinedTooltipData, tooltipData);
                    }
                });

                jsPlumbInstanceRef.current.recalculateOffsets(jsPlumbRef.current);
                jsPlumbInstanceRef.current.repaint();
            });
        } else {
            console.error("jsPlumb instance is not initialized");
        }
    };

    const generateDDLJsonFromSettingDatas = (tables = [], connections = [], name = "Default", description = "Default") => {
        if (!Array.isArray(tables) || !Array.isArray(connections)) {
            console.error("Invalid input for generateDDLJson: tables or connections are not arrays.");
            return {};
        }

        const result = {
            name: name,
            namespace: ["lightning", "catalog", name],
            tables: tables.map((table) => {
                if (!table.desc) {
                    console.warn(`Table ${table.name} does not have a 'desc' property.`);
                    return {
                        fqn: table.name.split('.'),
                        columnSpecs: [],
                    };
                }

                const tableElement = document.getElementById(`table-${table.uuid}`);
                const left = tableElement ? parseFloat(tableElement.style.left) : 0;
                const top = tableElement ? parseFloat(tableElement.style.top) : 0;

                const columnSpecs = table.desc.map((col) => {
                    const columnObj = {
                        name: col.col_name,
                        dataType: col.data_type,
                    };

                    if (col.primaryKey && col.primaryKey.columns.length === 0) {
                        columnObj.primaryKey = { columns: [] };
                    }

                    if (col.notNull && col.notNull.columns.length === 0) {
                        columnObj.notNull = { columns: [] };
                    }

                    if (table.uniqueConstraints && table.uniqueConstraints.some(u => u.column === col.col_name)) {
                        columnObj.unique = { columns: [] };
                    }

                    return columnObj;
                });

                const foreignKeys = connections
                    .filter(conn => conn.sourceId.includes(`table-${table.uuid}`))
                    .map(conn => {
                        const sourceColumnIndex = parseInt(conn.sourceId.match(/-col-(\d+)/)[1], 10) - 1;
                        const sourceColumnName = table.desc[sourceColumnIndex].col_name;
                        const targetTable = tables.find(t => `table-${t.uuid}` === conn.targetId.split("-col-")[0]);
                        const targetColumnIndex = parseInt(conn.targetId.match(/-col-(\d+)/)[1], 10) - 1;
                        const targetColumnName = targetTable.desc[targetColumnIndex].col_name;
                        return {
                            columns: [sourceColumnName],
                            refTable: targetTable.name.split('.'),
                            refColumns: [targetColumnName],
                        };
                    });

                return {
                    fqn: table.name.split('.'),
                    columnSpecs: columnSpecs,
                    ifNotExit: table.ifNotExit || false,
                    namespace: table.namespace || [],
                    unique: table.uniqueConstraints || [],
                    foreignKeys: foreignKeys,
                    dqAnnotations: [],
                    acAnnotations: [],
                    position: { left, top },
                };
            }),
        };

        return result;
    };

    const getSettingDataFromJson = (ddlText) => {
        let ddlJson;
        try {
            ddlJson = JSON.parse(ddlText);
            ddlJson = JSON.parse(ddlJson.json);
        } catch (e) {
            setPopupMessage(`Invalid DDL JSON format: ${e}`);
            return null;
        }

        localStorage.removeItem('savedTables');
        localStorage.removeItem('connections');

        const activatedTableNames = [];

        const newTables = ddlJson.tables.map((table) => {
            const foreignKeyConstraints = [];

            const columns = table.columnSpecs.map((col) => {
                if (col.foreignKey) {
                    const refTableName = col.foreignKey.refTable.join('.');
                    const refColumnName = col.foreignKey.refColumns[0];
                    const targetTable = ddlJson.tables.find(t => `lightning.${t.namespace.join('.')}.${t.name}` === refTableName);

                    if (targetTable) {
                        const targetColumn = targetTable.columnSpecs.find(c => c.name === refColumnName);
                        if (targetColumn) {
                            targetColumn.foreignKey = {
                                columns: targetColumn.foreignKey?.columns || [],
                                refTable: [table.name],
                                refColumns: [col.name],
                            };
                        }
                    }
                }

                return {
                    col_name: col.name,
                    data_type: col.dataType,
                    ...(col.primaryKey ? { primaryKey: { columns: [] } } : {}),
                    ...(col.notNull ? { notNull: { columns: [] } } : {}),
                    ...(col.unique ? { unique: { columns: [] } } : {}),
                    ...(col.foreignKey ? { foreignKey: col.foreignKey } : {})
                };
            });

            const position = table.position || { left: 100 + Math.random() * 100, top: 100 + Math.random() * 100 };

            const isActivated = table.hasOwnProperty('activateQuery');
            const tableName = `lightning.${ddlJson.namespace.join('.')}.${ddlJson.name}.${table.name}`;

            if (isActivated) {
                activatedTableNames.push(tableName);
            }

            return {
                name: tableName,
                desc: columns,
                foreignKeyConstraints: foreignKeyConstraints.length ? foreignKeyConstraints : [],
                dqAnnotations: table.dqAnnotations,
                uuid: `${uuidv4()}`,
                position: position,
                isActivated: isActivated,
                ...(isActivated ? { activateQuery: table.activateQuery } : {})
            };
        });

        const savedConnections = [];
        newTables.forEach((table) => {
            table.desc.forEach((col) => {
                if (col.foreignKey) {
                    const sourceTable = table.name;
                    const sourceTableUuid = newTables.find(t => t.name === sourceTable)?.uuid;

                    const targetTableName = col.foreignKey.refTable.join('.');
                    const targetTableObj = newTables.find(t => t.name === targetTableName);

                    if (sourceTableUuid && targetTableObj) {
                        const sourceId = `table-${sourceTableUuid}-col-${table.desc.findIndex(c => c.col_name === col.col_name) + 1}-left`;
                        const targetColumn = col.foreignKey.refColumns[0];
                        const targetId = `table-${targetTableObj.uuid}-col-${targetTableObj.desc.findIndex(c => c.col_name === targetColumn) + 1}-right`;

                        savedConnections.push({
                            sourceId,
                            targetId,
                            relationship: 'fk',
                        });
                    } else {
                        console.warn(`Target table ${targetTableName} not found for foreign key.`);
                    }
                }
            });
        });

        setActivateTables(activatedTableNames);
        localStorage.setItem('savedTables', JSON.stringify(newTables));
        localStorage.setItem('connections', JSON.stringify(savedConnections));

        return { savedTables: newTables, savedConnections, rulesData: {} };
    };

    const handlePreViewButtonClick = async (tableName) => {
        await runQuery(`SELECT * FROM ${tableName} LIMIT 100`);
        setCondition('preview');
        setViewMode('output');
    };

    const openModal = (info) => {
        setSelectedConnection(info);
        setIsModalOpen(true);
    };

    const handleSubmitRelationship = (relationship, type) => {
        setSelectedRelationship(relationship);
        setRelationshipType(type);

        if (selectedConnection) {
            const { sourceId, targetId } = selectedConnection;
        
            connectEndpoints(jsPlumbInstanceRef.current, sourceId, targetId, relationship, type, true);
            const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);
            const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
            const targetColumnName = targetColumn.querySelector('td')?.innerText || '';
            const sourceColumnClass = sourceColumn.children[0].classList[0];
            const constraints = getColumnConstraint(sourceColumnClass);
        
            let tooltipData;
            if (constraints) {
                tooltipData = constraints.map((constraint) => {
                    const reference = constraint.references ? `(${constraint.references})` : '';
                    return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
                }).join(', ');
            }
        
            const newTooltipData = tooltipData 
                ? `${tooltipData}, foreignKey: (${targetColumnName})` 
                : `foreignKey: (${targetColumnName})`;
        
            if (relationship === 'fk') {
                const existingTooltipData = sourceColumn.getAttribute('data-tooltip') || '';
        
                const combinedTooltipArray = [
                    ...new Set([...existingTooltipData.split(', '), newTooltipData].filter(Boolean)),
                ];
        
                const combinedTooltipData = combinedTooltipArray.join(', ');
        
                sourceColumn.setAttribute('data-tooltip', combinedTooltipData);
        
                addForeignKeyIconToColumn(sourceColumn, combinedTooltipData, tooltipData);
            }
        }            

        setIsModalOpen(false);
    };

    const runQuery = async (sqlQuery) => {
        if (!sqlQuery || sqlQuery.trim() === "") {
            setQueryResult({ error: "No table connections. Please check." });
            return;
        }

        try {
            setLoading(true);
            const result = await fetchApi(sqlQuery);
            setLoading(false);

            if (result) {
                if (result.error) {
                    setQueryResult({ error: result.message });
                } else {
                    const parsedResult = result.map((item) => JSON.parse(item));
                    if (Array.isArray(parsedResult) && parsedResult.length > 0) {
                        setQueryResult(<RenderTableForApi data={parsedResult} />);
                    } else {
                        setQueryResult("There is no data to display.");
                    }
                }
            } else {
                setQueryResult({ error: 'Failed to run query or received empty response.' });
            }
        } catch (error) {
            setLoading(false);
            setQueryResult({ error: 'Failed to run query or received empty response.' });
        }
    };

    const normalizeData = (data) => {
        // Find all possible keys from the entire data set
        const allKeys = new Set();
        data.forEach((row) => {
            Object.keys(row).forEach((key) => allKeys.add(key));
        });

        // Normalize each row to ensure all keys are present with default value of null
        const normalizedData = data.map((row) => {
            const normalizedRow = {};
            allKeys.forEach((key) => {
                normalizedRow[key] = row[key] !== undefined ? row[key] : null; // Set missing keys to null
            });
            return normalizedRow;
        });

        return normalizedData;
    };

    const RenderTableForApi = ({ data }) => {
        if (!data || data.length === 0) {
            return <div>No data available</div>;
        }

        // Normalize data to ensure all rows have the same keys
        const normalizedData = normalizeData(data);

        // Extract table headers dynamically from the first object in the data array
        const columns = Object.keys(normalizedData[0]).map((key) => ({
            accessorKey: key,
            header: key.charAt(0).toUpperCase() + key.slice(1),
            Cell: ({ cell }) => {
                const value = cell.getValue();
                if (!isNaN(value) && value !== null && value !== '') {
                    // Format number with commas and align right
                    return (
                        <div style={{ textAlign: 'right' }}>
                            {Number(value).toLocaleString()}
                        </div>
                    );
                }
                // Default rendering for non-numeric values
                return <div style={{ textAlign: 'left' }}>{value}</div>;
            },
        }));

        return (
            <MaterialReactTable
                columns={columns}
                data={normalizedData}
                enableSorting={true}
                enableColumnFilters={true}
                onPaginationChange={setPagination}
                state={{ pagination }}
                initialState={{
                    density: 'compact',
                    pagination: { pageSize: 30, pageIndex: 0 },
                }}
            />
        );
    };

    const Tooltip = ({ visible, message, position }) => {
        if (!visible) return null;
        return (
            <div
                style={{
                    position: 'fixed',
                    top: position.y,
                    left: position.x,
                    backgroundColor: 'black',
                    color: 'white',
                    padding: '5px',
                    borderRadius: '4px',
                    whiteSpace: 'nowrap',
                    zIndex: 1000,
                    fontSize: '14px'
                }}
            >
                {message}
            </div>
        );
    };

    const RenderDQTable = ({ data }) => {
        const [dqData, setDQData] = useState(data);
        const [selectedRows, setSelectedRows] = useState({});
        const [tooltip, setTooltip] = useState({ visible: false, message: '', position: { x: 0, y: 0 } });

        const handleCheckboxChange = (rowId) => {
            setSelectedRows((prevSelectedRows) => {
                const newSelectedRows = {
                    ...prevSelectedRows,
                    [rowId]: !prevSelectedRows[rowId]
                };

                selectedRowData = dqData.filter((row) => newSelectedRows[row.Name]);
                return newSelectedRows;
            });
        };

        const toggleSelectAll = () => {
            const allSelected = Object.values(selectedRows).every((isChecked) => isChecked);
            const newSelectedRows = {};
            dqData.forEach((item) => {
                newSelectedRows[item.Name] = !allSelected;
            });
            setSelectedRows(newSelectedRows);

            selectedRowData = dqData.filter((row) => newSelectedRows[row.Name]);
        };

        const dqColumns = [
            {
                accessorKey: 'Checkbox',
                header: (
                    <input
                        type="checkbox"
                        checked={dqData.every((item) => selectedRows[item.Name])}
                        onChange={toggleSelectAll}
                        id="selectAll"
                    />
                ),
                size: 50,
                enableSorting: false,
                enableColumnActions: false,
                Cell: ({ row }) => (
                    <input
                        type="checkbox"
                        checked={!!selectedRows[row.original.Name]}
                        onChange={() => handleCheckboxChange(row.original.Name)}
                        id={`checkbox-${row.original.Name}`}
                        name={`checkbox-${row.original.Name}`}
                    />
                )
            },
            {
                accessorKey: 'Status',
                header: 'Status',
                size: 80,
                Cell: ({ cell, row }) => renderStatus(cell.getValue(), row.original.errorMessage)
            },
            { accessorKey: 'Name', header: 'Name' },
            { accessorKey: 'Table', header: 'Table' },
            { accessorKey: 'Type', header: 'Type' },
            { accessorKey: 'Expression', header: 'Expression' },
            {
                accessorKey: 'Total_record',
                header: 'Total Records',
                Cell: ({ cell }) => (
                    <div style={{ textAlign: 'right' }}>
                        {typeof cell.getValue() === 'number'
                            ? cell.getValue().toLocaleString()
                            : renderFetchingStatus(cell.getValue())}
                    </div>
                )
            },
            {
                accessorKey: 'Good_record',
                header: 'Good Records',
                Cell: ({ cell }) => (
                    <div style={{ textAlign: 'right' }}>
                        {typeof cell.getValue() === 'number'
                            ? cell.getValue().toLocaleString()
                            : renderFetchingStatus(cell.getValue())}
                    </div>
                )
            },
            {
                accessorKey: 'Bad_record',
                header: 'Bad Records',
                Cell: ({ cell }) => (
                    <div style={{ textAlign: 'right' }}>
                        {typeof cell.getValue() === 'number'
                            ? cell.getValue().toLocaleString()
                            : renderFetchingStatus(cell.getValue())}
                    </div>
                )
            },
        ];

        const renderFetchingStatus = (value) => {
            if (value === 'fetching data...') {
                return <span>fetching data...</span>;
            }
            return value || '';
        };

        const renderStatus = (status, errorMessage) => {
            const handleMouseEnter = (event) => {
                const rect = event.target.getBoundingClientRect();
                setTooltip({
                    visible: true,
                    message: errorMessage,
                    position: { x: rect.left + window.scrollX, y: rect.top + window.scrollY - 30 }
                });
            };

            const handleMouseLeave = () => setTooltip({ visible: false, message: '', position: { x: 0, y: 0 } });

            if (status === 'loading') {
                return <Spinner className="spinner" style={{ width: '20px', height: '20px', fill: '#27A7D2' }} />;
            } else if (status === 'success') {
                return <CircleCheck style={{ width: '20px', height: '20px', fill: 'green' }} />;
            } else if (status === 'error') {
                return (
                    <div onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                        <Exclamation style={{ width: '20px', height: '20px', fill: 'red' }} />
                    </div>
                );
            } else if (status === 'warning') {
                return <CircleCheck style={{ width: '20px', height: '20px', fill: 'green' }} />;
            }
            return null;
        };

        return (
            <>
                <div style={{ position: 'relative' }}>
                    <button
                        className='btn-primary'
                        style={{
                            position: 'absolute',
                            top: '10px',
                            left: '10px',
                            zIndex: 100,
                        }}
                        onClick={(runDQ)}
                    >
                        Run DQ
                    </button>
                    <MaterialReactTable
                        columns={dqColumns}
                        data={dqData}
                        enableSorting
                        enableColumnFilters
                        onPaginationChange={setPagination}
                        state={{ pagination }}
                        initialState={{
                            density: 'compact',
                            pagination: { pageSize: 30, pageIndex: 0 }
                        }}
                    />
                </div>
                <Tooltip visible={tooltip.visible} message={tooltip.message} position={tooltip.position} />
            </>
        );
    };

    const createDynamicColumns = () => {
        const dynamicColumns = [{
            accessorKey: 'title',
            header: 'original Column Name',
            Cell: ({ cell }) => <strong>{cell.getValue()}</strong>
        }];

        rulesData.columns.forEach((colData, index) => {
            colData[0] = colData[0].split('.').pop();
            const fullHeader = rulesData.columnHeaders?.[index] || `Column ${index + 1}`;
            const headerName = fullHeader.split('.').pop();
            const headerPrefix = fullHeader.split('.').slice(0, 2).join('.');
            dynamicColumns.push({
                accessorKey: `column_${index}`,
                header: `${headerName} (${headerPrefix})` || `Column ${index + 1}`,
                Cell: ({ row }) => (
                    row.index === 1 ? (
                        <select
                            value={colData[row.index] || '0'}
                            onChange={(e) => handleInputChange(index, row.index, e.target.value)}
                            style={{ width: '50%', minWidth: '150px' }}
                        >
                            <option value="0">No Sorting</option>
                            <option value="1">Ascending</option>
                            <option value="2">Descending</option>
                        </select>
                    ) : (
                        <input
                            type="text"
                            value={colData[row.index] || ''}
                            onChange={(e) => handleInputChange(index, row.index, e.target.value)}
                            style={{ width: '50%', minWidth: '150px' }}
                            onKeyDown={(e) => {
                                e.stopPropagation();
                            }}
                        />
                    )
                )
            });
        });

        return dynamicColumns;
    };

    const renderRulesTable = () => {
        const dynamicColumns = createDynamicColumns();
        const tableData = rulesData.title.map((title, index) => {
            const rowData = { title };
            rulesData.columns.forEach((colData, colIndex) => {
                rowData[`column_${colIndex}`] = colData[index] || '';
            });
            return rowData;
        });

        return (
            <MaterialReactTable
                columns={dynamicColumns}
                data={tableData}
                enableColumnFilters={false}
                enableSorting={false}
                enableHiding={false}  // Disable the column hiding functionality
                enableColumnActions={false}  // Disable the column actions button entirely
                enableGlobalFilter={false}  // Disable the search bar
                enableDensityToggle={false}  // Disable the density toggle
                enableFullScreenToggle={false}  // Disable the full-screen toggle
                initialState={{ density: 'compact' }}  // Set default density
            />
        );
    };

    const handleInputChange = (colIndex, rowIndex, value) => {
        const updatedColumns = [...rulesData.columns];

        // Update the value in the specified column and row
        updatedColumns[colIndex][rowIndex] = value;

        setRulesData({ ...rulesData, columns: updatedColumns });
    };

    const renderResults = (position) => {
        if (loading) {
            return <div>Fetching data...</div>;
        }

        if (queryResult?.error) {
            return <div>{queryResult.error}</div>;
        }

        if (viewMode === 'dq') {
            return dqResults.length > 0 ? <RenderDQTable data={dqResults} /> : null;
        } else {
            if (condition === 'preview') {
                const isTextResult = typeof queryResult === 'string';

                return (
                    <div style={{ marginTop: isTextResult ? '30px' : '0' }}>
                        {queryResult ? queryResult : "Generating SQL..."}
                    </div>
                );
            } else if (condition === 'build') {
                return <div>{queryResult ? queryResult : "Generating SQL..."}</div>;
            } else if (condition === 'rules') {
                return renderRulesTable();
            }
        }
    };


    const compileUSL = async (name = "noname", ddlQuery, isDeploy = true) => {
        try {
            // First, run the query to create the namespace if it doesn't exist
            const createNamespaceQuery = "CREATE NAMESPACE IF NOT EXISTS lightning.metastore.usldb;";
            const namespaceResponse = await fetchApi(createNamespaceQuery);

            if (namespaceResponse.error) {
                console.error('Failed to create namespace:', namespaceResponse.message);
                return; // If namespace creation fails, stop further execution
            }

            // Create the USL query
            let uslQuery = `COMPILE USL IF NOT EXISTS ${name} DEPLOY NAMESPACE lightning.metastore.usldb DDL ${ddlQuery}`;

            // Call the API to compile the USL
            const response = await fetchApi(uslQuery);

            if (response.error) {
                // console.log(response.message)
                setPopupMessage(`Failed to compile USL: ${response.message}`);
            } else {
                // setPopupMessage('USL compiled successfully');
                return response;
            }
        } catch (error) {
            setPopupMessage(`Error in compileUSL:, ${error}`);
        }
    };

    const onSaveChanges = (updatedTableInfo) => {
        // console.log(updatedTableInfo)
        // Retrieve savedTables from localStorage or initialize an empty array
        let savedTable = JSON.parse(localStorage.getItem('savedTables')) || [];

        // Update the saved table with the new table info by matching uuid
        const updatedTable = savedTable.map(item => {
            if (item.uuid === updatedTableInfo.uuid) {
                return updatedTableInfo;  // Replace with updatedTableInfo if uuid matches
            }
            return item;  // Otherwise, return the original table info
        });

        localStorage.setItem('savedTables', JSON.stringify(updatedTable));

        window.location.reload();

    };

    const deleteAllTables = () => {
        localStorage.removeItem('savedTables');
        localStorage.removeItem('connections');

        Object.keys(localStorage).forEach((key) => {
            if (key.startsWith('table-')) {
                localStorage.removeItem(key);
            }
        });

        localStorage.removeItem('zoomLevel');
        localStorage.removeItem('offsetX');
        localStorage.removeItem('offsetY');

        const tableContainers = document.querySelectorAll('.table-container');
        tableContainers.forEach(table => table.remove());

        if (jsPlumbInstanceRef.current) {
            jsPlumbInstanceRef.current.reset();
        }

        setActivateTables([]);
        updateActivatedTables(false);

        // console.log("All tables and connections have been deleted.");
    };

    const handleOutputButton = () => {
        setViewMode('output')
        setCondition('preview')
    }

    return (
        <Resizable
            axis="y"
            initial={737}
            min={400}
            max={900}
            onResizeStart={() => {
                resizingRef.current = true;
            }}
            onResizeStop={() => {
                resizingRef.current = false;
            }}
        >
            {({ position, separatorProps }) => (
                <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                    <div className="btn-line">
                        <div className='usl-name'>
                            {uslName != '' && (
                                `USL : ${uslName}`
                            )}
                        </div>
                        {/* <div className="button-group2">
                            <button className="btn-primary" onClick={(loadDQ)}>Load Data Quality</button>
                        </div> */}
                    </div>
                    <div
                        style={{
                            position: 'relative',
                            height: `${position - reSizingOffset}px`,
                            overflow: 'auto',
                        }}
                    >
                        <div
                            ref={jsPlumbRef}
                            style={{
                                width: '100000px',
                                height: '100000px',
                                position: 'absolute',
                                transform: `scale(${zoomLevel})`,
                                transformOrigin: '0 0',
                                top: offset.y,
                                left: offset.x,
                                cursor: isDragging ? 'grabbing' : 'grab',
                                backgroundColor: 'white',
                            }}
                            onMouseDown={handleMouseDown}
                        >
                        </div>
                        <div className="zoom-controls">
                            <PlusIcon onClick={() => handleZoomIn(jsPlumbRef.current, setZoomLevel, setOffset, jsPlumbInstanceRef.current)} style={{ width: '30px', height: '30px', cursor: 'pointer' }} />
                            {/* <div className="zoom-level">{Math.round(zoomLevel * 100)}%</div> */}
                            <MinusIcon onClick={() => handleZoomOut(jsPlumbRef.current, setZoomLevel, setOffset, jsPlumbInstanceRef.current)} style={{ width: '30px', height: '30px', cursor: 'pointer' }} />
                            <LocationIcon onClick={() => handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset)} style={{ width: '30px', height: '30px', cursor: 'pointer' }} />
                            <button className="btn-primary" style={{ padding: '10px' }} onClick={(loadDQ)}>DQ</button>
                        </div>
                    </div>
                    <div
                        {...separatorProps}
                        style={{
                            height: '1px',
                            backgroundColor: '#ccc',
                            cursor: 'row-resize',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            position: 'relative',
                            zIndex: '10'
                        }}
                    >
                        <div
                            style={{
                                width: '50px',
                                height: '8px',
                                backgroundColor: '#888',
                                borderRadius: '4px',
                                position: 'absolute',
                            }}
                        />
                    </div>
                    {/* <div className='result-box'
                        style={{ '--position-offset': `${position - reSizingOffset}px` }}
                    >
                        <button className="btn-primary" onClick={(() => setViewMode('output'))}>Output</button>
                        {renderResults(position)}
                    </div> */}
                    <div
                        className="result-box"
                        style={{ '--position-offset': `${position - reSizingOffset}px` }}
                    >
                        {/* Tabs */}
                        <div className="tabs">
                            <button
                                className={`tab-button ${viewMode === 'dq' ? 'active' : ''}`}
                                onClick={() => setViewMode('dq')}
                            >
                                Data Quality
                            </button>
                            <button
                                className={`tab-button ${viewMode === 'output' ? 'active' : ''}`}
                                onClick={() => setViewMode('output')}
                            >
                                Output
                            </button>
                        </div>

                        {/* Tab Content */}
                        <div className="tab-content">
                            {viewMode === 'dq' && (
                                <div>
                                    {renderResults(position)}
                                </div>
                            )}
                            {viewMode === 'output' && (
                                <div>
                                    {renderResults(position)}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Relationship Modal */}
                    <RelationshipModal
                        isOpen={isModalOpen}
                        onClose={() => setIsModalOpen(false)}
                        onSubmit={handleSubmitRelationship}
                    />

                    <TableInfoSlider
                        tableInfo={selectedTableInfo}
                        isOpen={isSliderOpen}
                        onClose={closeSlider}
                        onSaveChanges={onSaveChanges}
                    />

                    {showDQPopup && (
                        <DataQualityPopup
                            onClose={handleCloseDQPopup}
                            onSubmit={handlePopupSubmit}
                            table={activateTargetTable}
                            setPopupMessage={setPopupMessage}
                        />
                    )}

                    {showDQListPopup && (
                        <DataQualityListPopup
                            onClose={handleCloseDQListPopup}
                            table={activateTargetTable}
                            setPopupMessage={setPopupMessage}
                        />
                    )}

                    {activateTable && showActivePopup && (
                        <ActivePopup
                            onClose={handleCloseActivePopup}
                            onSubmit={(query) => handleSubmitActivateQuery(query)}
                            table={activateTargetTable}
                        />
                    )}

                    {popupMessage && (
                        <div className="popup-overlay" onClick={closePopup}>
                            <div className="popup-message" onClick={(e) => e.stopPropagation()}>
                                <p>{popupMessage}</p>
                                <div className="popup-buttons">
                                    <button className="btn-primary" onClick={closePopup}>Close</button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </Resizable>
    );
}

export default SemanticLayer;