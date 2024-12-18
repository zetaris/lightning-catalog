import React, { useRef, useState, useEffect, useMemo } from 'react';
import Resizable from 'react-resizable-layout';
import { initializeJsPlumb, setupTableForSelectedTable, connectEndpoints, handleOptimizeView, handleZoomIn, handleZoomOut, getColumnConstraint, getRowInfo, addForeignKeyIconToColumn, getOptimalEndpointPosition } from '../configs/JsPlumbConfig';
import { v4 as uuidv4 } from 'uuid';
import { fetchApi, fetchActivateTableApi, qdqApi, edqApi } from '../../utils/common';
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

function SemanticLayer({ selectedTable, semanticLayerInfo, uslNamebyClick, setIsLoading, previewTableName, isMouseLoading, navErrorMsg, setNavErrorMsg, setPreviewableTables }) {
    useEffect(() => {
        if (sessionStorage.getItem('selectedTab') === 'semanticLayer') {
            setViewMode('output');
            setQueryResult((prevQueryResult) => {
                if (navErrorMsg && (!prevQueryResult || prevQueryResult.error !== navErrorMsg)) {
                    return { error: navErrorMsg };
                }
                return prevQueryResult;
            });
        }
    }, [navErrorMsg]);

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
    const [sorting, setSorting] = useState([]);
    const [columnFilters, setColumnFilters] = useState([]);
    const [globalFilter, setGlobalFilter] = useState('');
    const [outputTabInfo, setOutputTabInfo] = useState(null);

    const reSizingOffset = 80;

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

    const updateUSLInfo = async (uslData) => {
        const dbname = uslName.split('.').pop()

        try {
            const parsedDslData = JSON.parse(JSON.parse(uslData).json);

            // localStorage.setItem(dbname, JSON.stringify(parsedDslData));
            localStorage.setItem(uslName, JSON.stringify(parsedDslData));
        } catch {

        }
    }

    const loadDQ = async () => {
        setCondition('');
        setQueryResult(null);

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
                const cleanDqName = dq.name.startsWith('`') && dq.name.endsWith('`') ? dq.name.slice(1, -1) : dq.name;

                const dqEntry = {
                    Name: cleanDqName,
                    Table: `${namespace}.${dq.table}`,
                    Type: dq.type,
                    Expression: dq.expression,
                    Total_record: 'N/A',
                    Valid_record: 'N/A',
                    Invalid_record: 'N/A',
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
        setViewMode('dq');
    };

    const runDQ = async () => {
        let updatedDQResults = dqResults.map((dqEntry) => ({
            ...dqEntry,
            Status: 'N/A',
            Total_record: 'N/A',
            Valid_record: 'N/A',
            Invalid_record: 'N/A',
            errorMessage: '',
        }));

        // setDQResults(updatedDQResults);
        setDQResults((prevResults) => {
            if (JSON.stringify(prevResults) === JSON.stringify(updatedDQResults)) {
                return prevResults;
            }
            return updatedDQResults;
        });

        for (const dqEntry of selectedRowData) {
            dqEntry.Status = 'loading';
            dqEntry.Total_record = 'fetching data...';
            dqEntry.Valid_record = 'fetching data...';
            dqEntry.Invalid_record = 'fetching data...';

            updatedDQResults = updatedDQResults.map((entry) =>
                entry.Name === dqEntry.Name ? dqEntry : entry
            );
            setDQResults([...updatedDQResults]);
            const formattedDqName = `\`${dqEntry.Name}\``;
            const runDQQuery = `RUN DQ ${formattedDqName} TABLE ${dqEntry.Table}`;

            try {
                const runDQResult = await fetchApi(runDQQuery);

                if (runDQResult?.error) {
                    dqEntry.Total_record = '';
                    dqEntry.Valid_record = '';
                    dqEntry.Invalid_record = '';
                    dqEntry.Status = 'error';
                    dqEntry.errorMessage = runDQResult.message;
                } else {
                    const resultData = runDQResult?.[0] && JSON.parse(runDQResult[0]);
                    dqEntry.Total_record = resultData?.total_record || '0';
                    dqEntry.Valid_record = resultData?.valid_record || '0';
                    dqEntry.Invalid_record = resultData?.invalid_record || '0';

                    dqEntry.Status = dqEntry.Invalid_record !== '0' ? 'warning' : 'success';
                    dqEntry.errorMessage = '';
                }
            } catch (error) {
                dqEntry.Total_record = '';
                dqEntry.Valid_record = '';
                dqEntry.Invalid_record = '';
                dqEntry.Status = 'error';
                dqEntry.errorMessage = error.message || 'An unexpected error occurred';
            }

            updatedDQResults = updatedDQResults.map((entry) =>
                entry.Name === dqEntry.Name ? dqEntry : entry
            );
            setDQResults([...updatedDQResults]);
        }
    };

    const removeDQ = async () => {
        if (!selectedRowData || selectedRowData.length === 0) {
            // setPopupMessage("Please select DQ item to delete");
            setViewMode('output');
            setQueryResult({ error: "Please select DQ item to delete" });
            return;
        }

        const dqEntriesToRemove = [...selectedRowData];

        const removalPromises = dqEntriesToRemove.map(async (dqEntry) => {
            const runDQQuery = `REMOVE DQ ${dqEntry.Name} TABLE ${dqEntry.Table}`;
            try {
                const removeDQResult = await fetchApi(runDQQuery);
                const parsedResult = JSON.parse(removeDQResult);

                if (parsedResult.remove === true) {
                    return { ...dqEntry, success: true };
                } else {
                    return { ...dqEntry, success: false, message: parsedResult.message || "Failed to delete" };
                }
            } catch (error) {
                return { ...dqEntry, success: false, message: error.message || "Unknown error" };
            }
        });

        const removalResults = await Promise.all(removalPromises);

        const successfullyRemoved = removalResults.filter(result => result.success).map(result => result.Name + result.Table);
        const failedToRemove = removalResults.filter(result => !result.success);

        if (successfullyRemoved.length > 0) {
            setDQResults(prevDQResults =>
                prevDQResults.filter(entry =>
                    !successfullyRemoved.includes(entry.Name + entry.Table)
                )
            );
        }

        if (failedToRemove.length > 0) {
            const errorMessages = failedToRemove.map(item => `${item.Name}: ${item.message}`).join("\n");
            // setPopupMessage(`Failed to delete the following DQ items : ${errorMessages}`);
            setViewMode('output');
            setQueryResult({ error: `Failed to delete the following DQ items : ${errorMessages}` });
        }
    };

    const handleTableDoubleClick = () => {
        // console.log("handleTableDoubleClick");
    }

    const handleListDQClick = async (table) => {

        const uslInfoKey = table.name.split('.').slice(-2, -1)[0];
        // const uslInfo = JSON.parse(localStorage.getItem(uslInfoKey));
        const uslInfo = JSON.parse(localStorage.getItem(table.name));

        if (uslInfo && Array.isArray(uslInfo.tables)) {
            const tableName = table.name.split('.').pop();
            const matchedTable = uslInfo.tables.find((t) => t.name === tableName);

            if (matchedTable) {
                table = matchedTable;
                const savedTableData = JSON.parse(localStorage.getItem('savedTable'));

                if (Array.isArray(savedTableData)) {
                    const savedTable = savedTableData.find((t) => t.name === table.name);

                    if (savedTable) {
                        savedTable.isActivated = true;
                        localStorage.setItem('savedTable', JSON.stringify(savedTableData));
                    }
                }
            }
        }

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
        const endIndex = query.expression.toUpperCase().indexOf(asKeyword);

        if (startIndex === -1 || endIndex === -1) {
            // setPopupMessage("Invalid query expression: Required keywords not found.");
            setViewMode('output');
            setQueryResult({ error: "Invalid query expression: Required keywords not found." });
            return;
        }

        const table = query.expression.substring(startIndex, endIndex).trim();
        const queryIndex = query.expression.toUpperCase().indexOf('SELECT');
        if (queryIndex === -1) {
            // setPopupMessage("Invalid query expression: SELECT statement not found.");
            setViewMode('output');
            setQueryResult({ error: "Invalid query expression: SELECT statement not found." });
            return;
        }

        const selectQuery = query.expression.substring(queryIndex);
        const requestData = {
            table: table,
            query: selectQuery
        };

        try {
            let result = await fetchActivateTableApi(requestData);
            if (!result.error) {
                const parsedResult = JSON.parse(result)
                const registered = JSON.parse(parsedResult.registered);
                const registeredName = registered.name;
                const registeredQuery = registered.query;

                const savedTablesData = JSON.parse(localStorage.getItem('savedTables'));

                if (Array.isArray(savedTablesData)) {

                    const tableToUpdate = savedTablesData.find(t => t.name.split('.').pop() === registeredName);

                    if (tableToUpdate) {
                        tableToUpdate.isActivated = true;
                        tableToUpdate.activateQuery = registeredQuery;

                        localStorage.setItem('savedTables', JSON.stringify(savedTablesData));
                    }
                }

                setPreviewableTables((prev) => new Set(prev).add(table));

                updateUSLInfo();
                setActivateTables((prevTables) => {
                    const updatedTables = [...prevTables, table];
                    updateActivatedTables(true, updatedTables);
                    return updatedTables;
                });
            } else {
                // setPopupMessage(result.message);
                setViewMode('output');
                setQueryResult({ error: result.message });
                updateActivatedTables(false);
            }
        } catch (error) {
            // setPopupMessage("Error : ", error);
            setViewMode('output');
            setQueryResult({ error: error.message });
            updateActivatedTables(false);
        }

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
        setIsLoading(true);
        if (semanticLayerInfo && semanticLayerInfo.length > 0) {
            clearJsPlumbAndLocalStorage();

            const fetchAndParseDDL = async () => {
                try {
                    const name = semanticLayerInfo[0].name;
                    const ddl = semanticLayerInfo[0].ddl;
                    const selectedUSLPath = semanticLayerInfo[0].selectedUSLPath;
                    const parsedDDLResult = await compileUSL(name, ddl, selectedUSLPath, true);
                    if (parsedDDLResult) {
                        // window.location.reload();
                        deleteAllTables();
                        const { savedTables, savedConnections, rulesData } = getSettingDataFromJson(parsedDDLResult);
                        reFreshScreen();
                        handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset);
                        // restoreFromTablesAndConnections(savedTables, savedConnections);
                        // window.location.reload();
                    }
                } catch (error) {
                    console.error("Error while parsing DDL:", error);
                }
            };

            fetchAndParseDDL();
            handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset);
        }
        setIsLoading(false);
    }, [semanticLayerInfo]);

    useEffect(() => {
        const runPreviewQuery = async () => {
            if (!previewTableName || !previewTableName.startsWith('lightning.metastore')) return;

            try {
                setViewMode('output');
                setCondition('preview');
                setLoading(true);
                const query = `SELECT * FROM ${previewTableName} LIMIT 100`;
                const result = await fetchApi(query);
                setLoading(false);

                if (result) {
                    if (result.error) {
                        setQueryResult({ error: result.message });
                    } else {
                        const parsedResult = result.map((item) => JSON.parse(item));
                        if (Array.isArray(parsedResult) && parsedResult.length > 0) {
                            setQueryResult(<RenderTableForApi data={parsedResult} />);
                        } else {
                            setQueryResult({ error: "There is no data to display." });
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

        runPreviewQuery();
    }, [previewTableName]);

    useEffect(() => {
        const fetchUSLContent = async () => {
            // clearJsPlumbAndLocalStorage();
            // window.location.reload();
            deleteAllTables();
            const { savedTables, savedConnections, rulesData } = getSettingDataFromJson(uslNamebyClick);
            reFreshScreen()
            // restoreFromTablesAndConnections(savedTables, savedConnections);

            if (savedTables && savedTables.length > 0) {
                setUslName(savedTables[0].name.split('.').slice(0, -1).join('.'));
            }
        }

        if (uslNamebyClick) {
            setIsLoading(true);
            fetchUSLContent();
            setIsLoading(false);
            handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset);
            // const savedZoomLevel = localStorage.getItem('zoomLevel');
            // const savedOffsetX = localStorage.getItem('offsetX');
            // const savedOffsetY = localStorage.getItem('offsetY');

            // if (savedZoomLevel) {
            //     setZoomLevel(parseFloat(savedZoomLevel));
            // }

            // if (savedOffsetX && savedOffsetY) {
            //     setOffset({ x: parseFloat(savedOffsetX), y: parseFloat(savedOffsetY) });
            // }
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
        setIsLoading(true);
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

        setIsLoading(false);

    }, []);

    useEffect(() => {
        updateActivatedTables(true, activateTables);
    }, [activateTables]);

    useEffect(() => {
        setIsLoading(true);
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
        setIsLoading(false);

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
        const savedConnections = JSON.parse(localStorage.getItem('connections')) || [];

        if (savedTables.length > 0) {
            const firstTableName = savedTables[0]?.name?.split('.')?.slice(0, -1)?.join('.');
            if (firstTableName) {
                setUslName(firstTableName);
            } else {
                // console.error('The table name is incorrect:', savedTables[0]);
            }
        }

        if (!jsPlumbInstanceRef.current && jsPlumbRef.current) {
            jsPlumbInstanceRef.current = initializeJsPlumb(
                jsPlumbRef.current,
                [],
                openModal,
                handlePreViewButtonClick,
                handleTableInfoClick,
                handleActivateTableClick,
                handleActivateQueryClick,
                handleDataQualityButtonClick,
                handleTableDoubleClick,
                handleListDQClick
            );
        }

        if (savedTables.length > 0 && jsPlumbInstanceRef.current) {
            const activatedTableNames = [];

            savedTables.forEach((table) => {
                if (!table || !table.desc) {
                    // console.warn('Invalid table data:', table);
                    return;
                }

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
                    handleListDQClick
                );

                if (table.isActivated) {
                    activatedTableNames.push(table.name);
                }

                table.desc.forEach((col) => {
                    if (col.foreignKey) {
                        const refTableNames = col.foreignKey.refTable;
                        const refColumnNames = col.foreignKey.refColumns;

                        if (
                            Array.isArray(refTableNames) &&
                            Array.isArray(refColumnNames) &&
                            refTableNames.length === refColumnNames.length
                        ) {
                            refTableNames.forEach((refTableName, index) => {
                                const refColumnName = refColumnNames[index];
                                const targetTable = savedTables.find((t) => t.name === refTableName);

                                if (targetTable) {
                                    const targetColumn = targetTable.desc.find((c) => c.col_name === refColumnName);

                                    if (targetColumn && targetColumn.element) {
                                        const tooltipData = `References: ${table.name}.${col.col_name}`;
                                        addForeignKeyIconToColumn(targetColumn.element, tooltipData, `foreignKey(${col.col_name})`);
                                    } else {
                                        // console.error('There is no target column:', refTableName, refColumnName);
                                    }
                                } else {
                                    // console.error('There is no target table:', refTableName);
                                }
                            });
                        } else {
                            // console.error('The lengths of refTable and refColumns do not match:', col.col_name);
                        }
                    }
                });
            });

            setActivateTables(activatedTableNames);

            requestAnimationFrame(() => {
                savedConnections.forEach(({ sourceId, targetId, relationship, relationship_type }) => {
                    const optimalEndpoints = getOptimalEndpointPosition(sourceId, targetId);

                    if (optimalEndpoints?.sourceId && optimalEndpoints?.targetId) {
                        connectEndpoints(
                            jsPlumbInstanceRef.current,
                            optimalEndpoints.sourceId,
                            optimalEndpoints.targetId,
                            relationship,
                            relationship_type,
                            false
                        );

                        const { sourceColumn, targetColumn } = getRowInfo(optimalEndpoints.sourceId, optimalEndpoints.targetId);

                        if (sourceColumn && targetColumn) {
                            const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
                            const targetColumnName = targetColumn.querySelector('td')?.innerText || '';
                            const sourceColumnClass = sourceColumn.children[0]?.classList[0];
                            const constraints = getColumnConstraint(sourceColumnClass);

                            let tooltipData;
                            if (constraints) {
                                tooltipData = constraints
                                    .map((constraint) => {
                                        const reference = constraint.references ? `(${constraint.references})` : '';
                                        return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
                                    })
                                    .join(', ');
                            }

                            const targetForeignKeyText = `foreignKey(${targetColumnName})`;

                            const newTooltipData = tooltipData
                                ? `${tooltipData}, ${targetForeignKeyText}`
                                : targetForeignKeyText;

                            if (relationship === 'fk') {
                                const existingTooltipData = sourceColumn.getAttribute('data-tooltip') || '';
                                const combinedTooltipArray = [
                                    ...new Set([...existingTooltipData.split(', '), ...newTooltipData.split(', ')].filter(Boolean)),
                                ];
                                const combinedTooltipData = combinedTooltipArray.join(', ');

                                sourceColumn.setAttribute('data-tooltip', combinedTooltipData);
                                addForeignKeyIconToColumn(sourceColumn, combinedTooltipData, tooltipData);
                            }
                        } else {
                            // console.error('There are no source or target columns:', sourceId, targetId);
                        }
                    } else {
                        // console.error('The endpoint is invalid:', sourceId, targetId);
                    }
                });

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
            // console.log(jsPlumbInstanceRef.current)
            requestAnimationFrame(() => {
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
            });

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

                        const targetForeignKeyText = `foreignKey(${targetColumnName})`;

                        const newTooltipData = tooltipData
                            ? `${tooltipData}, ${targetForeignKeyText}`
                            : targetForeignKeyText;

                        if (relationship === 'fk') {
                            const existingTooltipData = sourceColumn.getAttribute('data-tooltip') || '';

                            // Split existing tooltip data into an array and remove duplicates
                            const combinedTooltipArray = [
                                ...new Set([...existingTooltipData.split(', '), ...newTooltipData.split(', ')].filter(Boolean)),
                            ];

                            // Rejoin the array into a string
                            const combinedTooltipData = combinedTooltipArray.join(', ');

                            sourceColumn.setAttribute('data-tooltip', combinedTooltipData);

                            addForeignKeyIconToColumn(sourceColumn, combinedTooltipData, tooltipData);
                        }
                    });
                }

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
            // setPopupMessage();
            setViewMode('output');
            setQueryResult({ error: `Invalid DDL JSON format: ${e}` });
            return null;
        }

        localStorage.removeItem('savedTables');
        localStorage.removeItem('connections');

        const activatedTableNames = [];

        const newTables = ddlJson.tables.map((table) => {
            const foreignKeyConstraints = [];

            const columns = table.columnSpecs.map((col) => {
                return {
                    col_name: col.name,
                    data_type: col.dataType,
                    ...(col.primaryKey ? { primaryKey: { columns: [] } } : {}),
                    ...(col.notNull ? { notNull: { columns: [] } } : {}),
                    ...(col.unique ? { unique: { columns: [] } } : {}),
                    ...(col.foreignKey ? { foreignKey: col.foreignKey } : {})
                };
            });

            // Handle table-level primary key
            if (table.primaryKey && table.primaryKey.columns) {
                table.primaryKey.columns.forEach((pkColumnName) => {
                    const column = columns.find((col) => col.col_name === pkColumnName);
                    if (column) {
                        column.primaryKey = {
                            columns: table.primaryKey.columns,
                            name: table.primaryKey.name
                        };
                    } else {
                        console.warn(`Primary key column '${pkColumnName}' not found in table '${table.name}'.`);
                    }
                });
            }

            // Handle table-level foreign keys
            if (table.foreignKeys && table.foreignKeys.length > 0) {
                table.foreignKeys.forEach((fk) => {
                    fk.columns.forEach((fkColumnName, index) => {
                        const column = columns.find((col) => col.col_name === fkColumnName);
                        if (column) {
                            column.foreignKey = {
                                refTable: fk.refTable,
                                refColumns: [fk.refColumns[index]],
                                name: fk.name
                            };
                        } else {
                            console.warn(`Foreign key column '${fkColumnName}' not found in table '${table.name}'.`);
                        }
                    });

                    // Add to foreignKeyConstraints array
                    foreignKeyConstraints.push({
                        columns: fk.columns,
                        refTable: fk.refTable,
                        refColumns: fk.refColumns,
                        name: fk.name
                    });
                });
            }

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
                    const sourceTableUuid = newTables.find((t) => t.name === sourceTable)?.uuid;

                    const targetTableName = col.foreignKey.refTable.join('.');
                    const targetTableObj = newTables.find((t) => t.name === targetTableName);

                    if (sourceTableUuid && targetTableObj) {
                        const sourceId = `table-${sourceTableUuid}-col-${table.desc.findIndex((c) => c.col_name === col.col_name) + 1}-left`;
                        const targetColumn = col.foreignKey.refColumns[0];
                        const targetId = `table-${targetTableObj.uuid}-col-${targetTableObj.desc.findIndex((c) => c.col_name === targetColumn) + 1}-right`;

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
        setViewMode('output');
        const result = await runQuery(`SELECT * FROM ${tableName} LIMIT 100`);
        if (result.error) {
            setQueryResult({ error: result.message });
        }
        setCondition('preview');
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

            const targetForeignKeyText = `foreignKey(${targetColumnName})`;

            const newTooltipData = tooltipData
                ? `${tooltipData}, ${targetForeignKeyText}`
                : targetForeignKeyText;

            if (relationship === 'fk') {
                const existingTooltipData = sourceColumn.getAttribute('data-tooltip') || '';

                // Split existing tooltip data into an array and remove duplicates
                const combinedTooltipArray = [
                    ...new Set([...existingTooltipData.split(', '), ...newTooltipData.split(', ')].filter(Boolean)),
                ];

                // Rejoin the array into a string
                const combinedTooltipData = combinedTooltipArray.join(', ');

                sourceColumn.setAttribute('data-tooltip', combinedTooltipData);

                addForeignKeyIconToColumn(sourceColumn, combinedTooltipData, tooltipData);
            }
        }

        setIsModalOpen(false);
    };

    const expandDuplicateKeys = (jsonStringArray) => {
        return jsonStringArray.map((item) => {
            const matches = [];
            const regex = /"([^"]+)":\s*("[^"]*"|\d+|null|true|false)/g;
            let match;

            while ((match = regex.exec(item)) !== null) {
                matches.push([match[1], match[2].replace(/^"|"$/g, "")]);
            }

            const expandedRow = {};
            let keyCounters = {};

            matches.forEach(([key, value]) => {
                const count = keyCounters[key] || 0;
                const newKey = count === 0 ? key : `${key}_${count}`;
                expandedRow[newKey] = value;
                keyCounters[key] = count + 1;
            });

            return expandedRow;
        });
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
                    return result;
                } else {
                    // const parsedResult = result.map((item) => JSON.parse(item));
                    const parsedResult = expandDuplicateKeys(result);
                    if (Array.isArray(parsedResult) && parsedResult.length > 0) {
                        setQueryResult(<RenderTableForApi data={parsedResult} />);
                    } else {
                        setQueryResult("There is no data to display.");
                    }
                    return parsedResult;
                }
            } else {
                setQueryResult({ error: 'Failed to run query or received empty response.' });
            }
        } catch (error) {
            setLoading(false);
            setQueryResult({ error: 'Failed to run query or received empty response.' });
        }
    };

    const convertToCSV = (data) => {
        if (!Array.isArray(data) || data.length === 0) {
            throw new Error('No data available to export.');
        }

        const headers = Object.keys(data[0]).join(',');

        const rows = data.map((row) =>
            Object.values(row)
                .map((value) =>
                    typeof value === 'string'
                        ? `"${value.replace(/"/g, '""')}"`
                        : value
                )
                .join(',')
        );

        return `${headers}\n${rows.join('\n')}`;
    };

    const normalizeData = (data) => {
        const allKeys = new Set();
        data.forEach((row) => {
            Object.keys(row).forEach((key) => allKeys.add(key));
        });

        const normalizedData = data.map((row) => {
            const normalizedRow = {};
            allKeys.forEach((key) => {
                normalizedRow[key] = row[key] !== undefined ? row[key] : null;
            });
            return normalizedRow;
        });

        return normalizedData;
    };

    const RenderTableForApi = ({ data, outputTabInfo }) => {
        if (!Array.isArray(data)) {
            // console.log(data)
            // console.error("Expected an array, but received:", data);
            return [];
        }

        if (!data || data.length === 0) {
            return <div>No data available</div>;
        }

        const normalizedData = normalizeData(data);

        const handleExport = () => {
            if (!outputTabInfo) return;

            const { name, table, validRecord } = outputTabInfo;
            edqApi(name, table, validRecord).then((result) => {
                if (result.error) {
                    alert(`Error exporting data: ${result.message}`);
                } else {
                    try {
                        const csvContent = convertToCSV(result);

                        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
                        const url = URL.createObjectURL(blob);

                        const link = document.createElement('a');
                        link.href = url;
                        link.setAttribute('download', `${name}.csv`);
                        document.body.appendChild(link);
                        link.click();

                        document.body.removeChild(link);
                        URL.revokeObjectURL(url);

                    } catch (error) {
                        // setPopupMessage();
                        setViewMode('output');
                        setQueryResult({ error: error });
                    }
                }
            });
        };

        const columns = Object.keys(normalizedData[0]).map((key) => ({
            accessorKey: key,
            header: key.charAt(0).toUpperCase() + key.slice(1),
            muiTableHeadCellProps: {
                align: 'center',
                style: key === (outputTabInfo?.name || '') ? { backgroundColor: '#E9EFEC' } : {},
            },
            muiTableBodyCellProps: {
                style: key === (outputTabInfo?.name || '') ? { backgroundColor: '#E9EFEC' } : {},
            },
            Cell: ({ cell }) => {
                const value = cell.getValue();
                if (!isNaN(value) && value !== null && value !== '') {
                    return (
                        <div style={{ textAlign: 'right' }}>
                            {Number(value).toLocaleString()}
                        </div>
                    );
                }
                return <div style={{ textAlign: 'left' }}>{value}</div>;
            },
        }));

        return (
            <>
                <div style={{ position: 'relative' }}>
                    {outputTabInfo && (
                        <button
                            className="btn-primary"
                            style={{
                                position: 'absolute',
                                top: '10px',
                                left: '10px',
                                zIndex: 100,
                            }}
                            onClick={handleExport}
                        >
                            Export
                        </button>
                    )}
                    <MaterialReactTable
                        columns={columns}
                        data={normalizedData}
                        enableSorting={true}
                        enableColumnFilters={true}
                        initialState={{
                            density: 'compact',
                            pagination: { pageSize: 30, pageIndex: 0 },
                        }}
                    />
                </div>
            </>
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

        useEffect(() => {
            if (JSON.stringify(dqData) !== JSON.stringify(data)) {
                setDQData(data);
            }
        }, [data]);

        const processedData = useMemo(() => dqData, [dqData]);

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
                ),
            },
            {
                accessorKey: 'Status',
                header: 'Status',
                size: 80,
                enableSorting: false,
                enableColumnActions: false,
                Cell: ({ cell, row }) => renderStatus(cell.getValue(), row.original.errorMessage),
            },
            { accessorKey: 'Name', header: 'Name', muiTableHeadCellProps: { align: 'center' } },
            { accessorKey: 'Table', header: 'Table', muiTableHeadCellProps: { align: 'center' } },
            { accessorKey: 'Type', header: 'Type', muiTableHeadCellProps: { align: 'center' } },
            { accessorKey: 'Expression', header: 'Expression', muiTableHeadCellProps: { align: 'center' } },
            {
                accessorKey: 'Total_record',
                header: 'Total Records',
                muiTableHeadCellProps: { align: 'center' },
                Cell: ({ cell }) => (
                    <div style={{ textAlign: 'right' }}>
                        {typeof cell.getValue() === 'number'
                            ? cell.getValue().toLocaleString()
                            : renderFetchingStatus(cell.getValue())}
                    </div>
                ),
            },
            {
                accessorKey: 'Valid_record',
                header: 'Valid record',
                muiTableHeadCellProps: { align: 'center' },
                Cell: ({ cell, row }) => (
                    <div style={{ textAlign: 'right' }}>
                        {typeof cell.getValue() === 'number' ? (
                            <a
                                href="#"
                                onClick={(e) => {
                                    e.preventDefault();
                                    const { Name, Table } = row.original;
                                    handleOutputTabWithData(qdqApi, Name, Table, true, 100);
                                }}
                                style={{ fontWeight: 'bold', color: 'blue', textDecoration: 'underline' }}
                            >
                                {cell.getValue().toLocaleString()}
                            </a>
                        ) : (
                            renderFetchingStatus(cell.getValue())
                        )}
                    </div>
                ),
            },
            {
                accessorKey: 'Invalid_record',
                header: 'Invalid record',
                muiTableHeadCellProps: { align: 'center' },
                Cell: ({ cell, row }) => (
                    <div style={{ textAlign: 'right' }}>
                        {typeof cell.getValue() === 'number' ? (
                            <a
                                href="#"
                                onClick={(e) => {
                                    e.preventDefault();
                                    const { Name, Table } = row.original;
                                    handleOutputTabWithData(qdqApi, Name, Table, false, 100);
                                }}
                                style={{ fontWeight: 'bold', color: 'red', textDecoration: 'underline' }}
                            >
                                {cell.getValue().toLocaleString()}
                            </a>
                        ) : (
                            renderFetchingStatus(cell.getValue())
                        )}
                    </div>
                ),
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
                        onClick={runDQ}
                    >
                        Run
                    </button>
                    {/* <button
                        className='btn-primary'
                        style={{
                            position: 'absolute',
                            top: '10px',
                            left: '85px',
                            zIndex: 100,
                        }}
                        onClick={removeDQ}
                    >
                        Remove
                    </button> */}
                    {/* <MaterialReactTable
                        columns={dqColumns}
                        // data={dqData}
                        data={processedData}
                        enableSorting
                        enableColumnFilters
                        onPaginationChange={setPagination}
                        // onPaginationChange={(newPagination) => {
                        //     setPagination(newPagination);
                        // }}
                        state={{ pagination }}
                        initialState={{
                            density: 'compact',
                            pagination: { pageSize: 30, pageIndex: 0 }
                        }}
                    /> */}
                    <MaterialReactTable
                        columns={dqColumns}
                        data={processedData}
                        enableSorting
                        enableGlobalFilter={false}
                        enableColumnFilters={false}
                        state={{
                            pagination,
                            sorting,
                            // columnFilters,
                            // globalFilter,
                        }}
                        onPaginationChange={setPagination}
                        onSortingChange={setSorting}
                        // onColumnFiltersChange={setColumnFilters}
                        // onGlobalFilterChange={setGlobalFilter}
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

    const handleOutputTabWithData = (apiFunction, name, table, validRecord, limit = 100) => {
        setLoading(true);
        setOutputTabInfo({ name, table, validRecord });

        apiFunction(name, table, validRecord, limit)
            .then((result) => {
                if (result.error) {
                    // setPopupMessage();
                    setViewMode('output');
                    setQueryResult({ error: `Error fetching data: ${result.message}` });
                } else {
                    try {
                        const parsedData = result.map((item) =>
                            typeof item === 'string' ? JSON.parse(item) : item
                        );
                        setViewMode('output');

                        setQueryResult(
                            <RenderTableForApi
                                data={parsedData}
                                outputTabInfo={{ name, table, validRecord }}
                            />
                        );
                        setCondition('preview');
                    } catch (parseError) {
                        console.error('Error parsing data:', parseError);
                        alert('Failed to parse fetched data.');
                    }
                }
            })
            .catch((error) => {
                // console.error('Error fetching data:', error);
                // setPopupMessage(`Failed to fetch data: ${error.message}`);
                setViewMode('output');
                setQueryResult({ error: `Failed to fetch data: ${error.message}` });
            })
            .finally(() => {
                setLoading(false);
            });
    };

    const createDynamicColumns = () => {
        const dynamicColumns = [{
            accessorKey: 'title',
            header: 'original Column Name',
            muiTableHeadCellProps: { align: 'center' },
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
                muiTableHeadCellProps: { align: 'center' },
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
                enableHiding={false}
                enableColumnActions={false}
                enableGlobalFilter={false}
                enableDensityToggle={false}
                enableFullScreenToggle={false}
                initialState={{ density: 'compact' }}
            />
        );
    };

    const handleInputChange = (colIndex, rowIndex, value) => {
        const updatedColumns = [...rulesData.columns];

        updatedColumns[colIndex][rowIndex] = value;

        setRulesData({ ...rulesData, columns: updatedColumns });
    };

    const renderResults = useMemo(() => {
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
                    <div>
                        {queryResult ? queryResult : ""}
                    </div>
                );
            } else if (condition === 'build') {
                return <div>{queryResult ? queryResult : "Generating SQL..."}</div>;
            } else if (condition === 'rules') {
                return renderRulesTable();
            }
        }
    }, [loading, queryResult, viewMode, condition, dqResults, pagination, sorting,
        // columnFilters,
        // globalFilter,
    ]);

    const compileUSL = async (name = "noname", ddlQuery, selectedUSLPath, isDeploy = true) => {
        try {

            let createNamespaceQuery;
            let namespaceResponse;
            // First, run the query to create the namespace if it doesn't exist
            // let createNamespaceQuery = "CREATE NAMESPACE IF NOT EXISTS lightning.metastore.usldb;";
            // let namespaceResponse = await fetchApi(createNamespaceQuery);

            // if (namespaceResponse.error) {
            //     console.error('Failed to create namespace:', namespaceResponse.message);
            //     return; // If namespace creation fails, stop further execution
            // }

            if (selectedUSLPath) {
                createNamespaceQuery = `CREATE NAMESPACE IF NOT EXISTS ${selectedUSLPath}.${name};`;
            } else {
                setNavErrorMsg(`Please select the correct namespace.`);
                return;
            }

            namespaceResponse = await fetchApi(createNamespaceQuery);

            if (namespaceResponse.error) {
                setPopupMessage('Failed to create namespace:', namespaceResponse.message);
                return;
            }

            // Create the USL query
            let uslQuery = `COMPILE USL IF NOT EXISTS ${name} DEPLOY NAMESPACE ${selectedUSLPath} DDL ${ddlQuery}`;

            // Call the API to compile the USL
            const response = await fetchApi(uslQuery);

            if (response.error) {
                // console.log(response.message)
                // setPopupMessage(`Failed to compile USL: ${response.message}`);
                setViewMode('output');
                setQueryResult({ error: `Failed to compile USL: ${response.message}` });
            } else {
                // setPopupMessage('USL compiled successfully');
                return response;
            }
        } catch (error) {
            // setPopupMessage(`Error in compileUSL:, ${error}`);
            setViewMode('output');
            setQueryResult({ error: `Error in compileUSL:, ${error}` });
        }
    };

    const onSaveChanges = (updatedTableInfo) => {
        let savedTable = JSON.parse(localStorage.getItem('savedTables')) || [];
        const updatedTable = savedTable.map(item => {
            if (item.uuid === updatedTableInfo.uuid) {
                return updatedTableInfo;
            }
            return item;
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
    };

    const handleOutputButton = () => {
        setViewMode('output')
        setCondition('preview')
    }

    const clickDQTab = () => {
        setViewMode('dq');
        if (queryResult?.error === navErrorMsg) {
            setQueryResult('');
        }
    };

    return (
        <Resizable
            axis="y"
            initial={710}
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
                                cursor: isMouseLoading ? 'wait' : (isDragging ? 'grabbing' : 'grab'),
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
                    <div
                        className="result-box"
                        style={{ '--position-offset': `${position - reSizingOffset}px` }}
                    >
                        {/* Tabs */}
                        <div className="tabs">
                            <button
                                className={`tab-button ${viewMode === 'dq' ? 'active' : ''}`}
                                // onClick={() => setViewMode('dq')}
                                onClick={() => clickDQTab()}
                            >
                                Data Quality
                            </button>
                            <button
                                className={`tab-button ${viewMode === 'output' ? 'active' : ''}`}
                                onClick={() => setViewMode('output')}
                            // onClick={() => clickOutputTab()}
                            >
                                Output
                            </button>
                        </div>

                        {/* Tab Content */}
                        <div className="tab-content">
                            {viewMode === 'dq' && (
                                <div>
                                    {renderResults}
                                </div>
                            )}
                            {viewMode === 'output' && (
                                <div>
                                    {renderResults}
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
                            updateUSLInfo={updateUSLInfo}
                        />
                    )}

                    {showDQListPopup && (
                        <DataQualityListPopup
                            onClose={handleCloseDQListPopup}
                            table={activateTargetTable}
                            setPopupMessage={setPopupMessage}
                            updateUSLInfo={updateUSLInfo}
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