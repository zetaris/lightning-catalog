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
import { ReactComponent as Spinner } from '../../assets/images/spinner-solid.svg';
import { TableInfoSlider } from './TableInfoSlider';
import DataQualityPopup from './components/DataQualityPopup.js';
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
    const [loading, setLoading] = useState(false); // State to track loading status
    const [currentColumnIndex, setCurrentColumnIndex] = useState(0);
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
    const [showActivePopup, setShowActivePopup] = useState(false);
    const [activateTable, setactivateTable] = useState(false);
    const [activateTargetTable, setActivateTargetTable] = useState(null);
    const [popupMessage, setPopupMessage] = useState(null);
    const [activateTables, setActivateTables] = useState(null);
    const resizingRef = useRef(false);
    const [dqResults, setDQResults] = useState([]);
    let selectedRowData = [];

    const reSizingOffset = 115;

    const closePopup = () => setPopupMessage(null);

    const handleCloseDQPopup = () => {
        setShowDQPopup(false);
    };

    const handleCloseActivePopup = () => {
        setShowActivePopup(false);
    };

    const handlePopupSubmit = (data) => {
        console.log('Popup submitted data:', data);
        // Handle submitted data (name and expression)
    };

    const getLineage = () => {
        setPopupMessage("Feature under development.");
    }

    const getQueryMapping = () => {
        setPopupMessage("Feature under development.");
    }

    // const runDQCheck = async () => {
    //     const savedTables = JSON.parse(localStorage.getItem("savedTables"));
    //     const processedNamespaces = new Set();
    //     const dqResults = [];

    //     // Step 1: Fetch LIST DQ USL results first
    //     for (const table of savedTables) {
    //         const tableName = table.name;
    //         const namespace = tableName.split('.').slice(0, 4).join('.');

    //         const listDQQuery = `LIST DQ USL ${namespace}`;
    //         const listDQResult = await fetchApi(listDQQuery);
    //         if (!listDQResult) continue;

    //         const dqRules = listDQResult.map(rule => JSON.parse(rule));

    //         // Step 2: For each rule, run DQ and update results one by one
    //         for (const dq of dqRules) {
    //             // Check if dq entry is already processed
    //             const existingEntry = dqResults.find(entry => entry.Name === dq.name);
    //             if (existingEntry && existingEntry.isProcessed) continue; // Skip if already processed

    //             const dqEntry = {
    //                 Name: dq.name,
    //                 Table: `${namespace}.${dq.table}`,
    //                 Type: dq.type,
    //                 Expression: dq.expression,
    //                 Total_record: 'fetching data...',
    //                 Good_record: 'fetching data...',
    //                 Bad_record: 'fetching data...',
    //                 Status: 'loading',  // Initial status is 'loading'
    //                 isProcessed: false, // Flag to track if the entry has been processed
    //             };
    //             dqResults.unshift(dqEntry);
    //             setDQResults([...dqResults]);  // Render each new LIST DQ entry

    //             // Step 3: Run DQ query for the current entry and update its results
    //             const runDQQuery = `RUN DQ ${dqEntry.Name} TABLE ${dqEntry.Table}`;

    //             try {
    //                 const runDQResult = await fetchApi(runDQQuery);
    //                 if (runDQResult?.error) {
    //                     dqEntry.Total_record = '';
    //                     dqEntry.Good_record = '';
    //                     dqEntry.Bad_record = '';
    //                     dqEntry.Status = 'error';  // Set status to error if RUN DQ fails
    //                 } else {
    //                     const resultData = runDQResult?.[0] && JSON.parse(runDQResult[0]);
    //                     dqEntry.Total_record = resultData?.total_record || '0';
    //                     dqEntry.Good_record = resultData?.good_record || '0';
    //                     dqEntry.Bad_record = resultData?.bad_record || '0';
    //                     if(dqEntry.Bad_record !== '0'){
    //                         dqEntry.Status = 'warning';
    //                     }else{
    //                         dqEntry.Status = 'success';
    //                     }
    //                 }
    //             } catch (error) {
    //                 dqEntry.Total_record = '';
    //                 dqEntry.Good_record = '';
    //                 dqEntry.Bad_record = '';
    //                 dqEntry.Status = 'error';  // Set status to error if the fetch fails
    //             }

    //             // Mark this entry as processed
    //             dqEntry.isProcessed = true;

    //             // Update the DQ results with the current RUN DQ entry
    //             setDQResults([...dqResults]);  // force re-render
    //         }
    //     }
    // };    

    const loadDQ = async () => {
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
                    Total_record: 'N/A',       // Placeholder values
                    Good_record: 'N/A',        // Placeholder values
                    Bad_record: 'N/A',         // Placeholder values
                    Status: 'N/A',             // No processing status
                    isChecked: false           // Checkbox state
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
        setCondition('dqResult')
    };

    const runDQ = async () => {
        if(condition !== 'dqResult'){
            setPopupMessage('Please proceed with the load first.')
        }
        const updatedDQResults = [...dqResults];

        for (const dqEntry of selectedRowData) {
            dqEntry.Status = 'loading';
            dqEntry.Total_record = 'fetching data...';
            dqEntry.Good_record = 'fetching data...';
            dqEntry.Bad_record = 'fetching data...';

            setDQResults([...updatedDQResults]);

            const runDQQuery = `RUN DQ ${dqEntry.Name} TABLE ${dqEntry.Table}`;

            try {
                const runDQResult = await fetchApi(runDQQuery);

                if (runDQResult?.error) {
                    dqEntry.Total_record = '';
                    dqEntry.Good_record = '';
                    dqEntry.Bad_record = '';
                    dqEntry.Status = 'error';
                } else {
                    const resultData = runDQResult?.[0] && JSON.parse(runDQResult[0]);
                    dqEntry.Total_record = resultData?.total_record || '0';
                    dqEntry.Good_record = resultData?.good_record || '0';
                    dqEntry.Bad_record = resultData?.bad_record || '0';

                    dqEntry.Status = dqEntry.Bad_record !== '0' ? 'warning' : 'success';
                }
            } catch (error) {
                dqEntry.Total_record = '';
                dqEntry.Good_record = '';
                dqEntry.Bad_record = '';
                dqEntry.Status = 'error';
            }

            setDQResults([...updatedDQResults]);
            setCondition('dqResult');
        }
    };

    const handleTableDoubleClick = () => {
        // console.log("handleTableDoubleClick");
    }

    const handleDataQualityButtonClick = (table) => {
        setActivateTargetTable(table);
        setShowDQPopup(true);
    };

    const handleEditorChange = (newContent) => {
        setEditorContent(newContent);
    };

    const handleDeActivateTableClick = async (table) => {
        // const fileName = table.name.split('.').pop();
        // const result = await deleteActivateTableApi(fileName);
        // if (result) {
        //     setPopupMessage(result);
        //     updateActivatedTables(false)
        // }

        setPopupMessage("Feature under development.");
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

        // Find the indexes of 'ACTIVATE USL TABLE' and 'AS' in the query
        const startIndex = query.expression.indexOf(activateKeyword) + activateKeyword.length;
        const endIndex = query.expression.indexOf(asKeyword);

        // Extract the table name between 'ACTIVATE USL TABLE' and 'AS'
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
            setPopupMessage(`The ${result.name} table has been activated.`);
            setActivateTables((prevTables) => {
                const updatedTables = [...prevTables, table];
                updateActivatedTables(true, updatedTables);
                return updatedTables;
            });
        } else {
            setPopupMessage(result.message);
            updateActivatedTables(false);
        }
        fetchActivateTableData();
        setShowActivePopup(false);
    };

    const handleTableInfoClick = (table) => {
        // setPopupMessage("Feature under development.");
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

    // Effect to handle semanticLayerInfo updates
    useEffect(() => {
        const fetchAndParseDDL = async () => {
            if (semanticLayerInfo && semanticLayerInfo.length > 0) {
                console.log("Received new Semantic Layer info: ", semanticLayerInfo);
                try {
                    // const semanticLayerInfoJson = JSON.parse(semanticLayerInfo[0])
                    const name = semanticLayerInfo[0].name;
                    const ddl = semanticLayerInfo[0].ddl;
                    const parsedDDLResult = await compileUSL(name, ddl, true);
                    if (parsedDDLResult) {
                        console.log(parsedDDLResult)
                        const { savedTables, savedConnections, rulesData } = getSettingDataFromJson(parsedDDLResult);
                        restoreFromTablesAndConnections(savedTables, savedConnections);
                        window.location.reload();
                    }
                } catch (error) {
                    console.error("Error while parsing DDL:", error);
                }
            }
        };

        fetchAndParseDDL();
    }, [semanticLayerInfo]);

    useEffect(() => {
        const fetchUSLContent = async () => {

            const dbname = uslNamebyClick.split('.').pop();
            const path = uslNamebyClick.split('.').slice(0, -1).join('.');

            if (!uslNamebyClick) return;

            let query = `LOAD USL ${dbname} NAMESPACE ${path}`

            try {
                const result = await fetchApi(query);
                if (result) {
                    const { savedTables, savedConnections, rulesData } = getSettingDataFromJson(result);
                    restoreFromTablesAndConnections(savedTables, savedConnections);
                    window.location.reload();
                }
            } catch (error) {
                console.error("Error fetching USL file content:", error);
            }
        };

        if (uslNamebyClick) {
            fetchUSLContent();
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
        fetchActivateTableData();

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
            const tableId = table.id; // Assuming each table container has a unique id
            const savedPosition = localStorage.getItem(tableId);

            if (savedPosition) {
                const { top, left } = JSON.parse(savedPosition);
                table.style.top = `${top}px`;
                table.style.left = `${left}px`;
            }
        });

        // Check if zoom or offset values are missing, then optimize the view
        if (!savedZoomLevel || !savedOffsetX || !savedOffsetY) {
            handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset);
        }

    }, []);

    useEffect(() => {
        updateActivatedTables(true, activateTables);
    }, [activateTables]);

    useEffect(() => {
        const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];

        // If selectedTable exists and is not already saved, add it
        if (selectedTable && jsPlumbInstanceRef.current) {
            const isAlreadySaved = savedTables.some(
                (table) => table.name === selectedTable.name
            );

            if (!isAlreadySaved) {
                const uuid = uuidv4(); // Generate a unique ID
                const tableWithUuid = { ...selectedTable, uuid }; // Add UUID to the table object

                requestAnimationFrame(() => {
                    setupTableForSelectedTable(jsPlumbRef.current, tableWithUuid, jsPlumbInstanceRef.current, uuid, false, handleRowClick, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleDeActivateTableClick, handleDataQualityButtonClick, handleTableDoubleClick);
                    jsPlumbInstanceRef.current.recalculateOffsets(jsPlumbRef.current);
                    jsPlumbInstanceRef.current.repaint();

                    // Save the new table with UUID to localStorage
                    savedTables.push(tableWithUuid);
                    localStorage.setItem('savedTables', JSON.stringify(savedTables));

                    // Update DDL after adding table
                    const updatedDDL = generateDDLJsonFromSettingDatas(savedTables, JSON.parse(localStorage.getItem('connections')) || []);
                });
            }
        }

    }, [selectedTable]);

    const fetchActivateTableData = async () => {
        // Step 1: Get savedTables from localStorage
        const savedTables = JSON.parse(localStorage.getItem("savedTables")) || [];

        const activatedTables = [];

        // Step 2: For each table, execute a query to check if the table is active
        for (const table of savedTables) {
            const tableName = table.name;
            try {
                const query = `SELECT 1 FROM ${tableName}`;
                const result = await fetchApi(query); // Assume fetchApi is a function that executes SQL queries

                // Step 3: If query is successful, add table name to activatedTables array
                if (!result.error) {
                    activatedTables.push(tableName);
                }
            } catch (error) {
                // console.error(`Failed to activate table ${tableName}:`, error.message);
            }
        }

        // Step 4: Update state with the list of activated tables
        setActivateTables(activatedTables);
    };

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

        // Initialize jsPlumb instance
        if (!jsPlumbInstanceRef.current && jsPlumbRef.current) {
            jsPlumbInstanceRef.current = initializeJsPlumb(jsPlumbRef.current, [], openModal, handleRowClick, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleDeActivateTableClick, handleDataQualityButtonClick, handleTableDoubleClick, handleDirectConnection);
        }

        // Render saved tables from localStorage if available
        if (savedTables.length > 0 && jsPlumbInstanceRef.current) {
            savedTables.forEach((table) => {
                setupTableForSelectedTable(jsPlumbRef.current, table, jsPlumbInstanceRef.current, table.uuid, false, handleRowClick, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleDeActivateTableClick, handleDataQualityButtonClick, handleTableDoubleClick);
            });

            // Ensure tables are fully rendered before restoring connections
            requestAnimationFrame(() => {
                if (savedConnections.length > 0) {
                    savedConnections.forEach(({ sourceId, targetId, relationship, relationship_type }, index) => {
                        connectEndpoints(jsPlumbInstanceRef.current, sourceId, targetId, relationship, relationship_type, false);
                        const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);

                        // Operation to change svg image in target column
                        const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
                        const targetColumnClass = targetColumn.children[0].classList[0];
                        // const targetColumnClass = targetColumn.children[0].id;
                        const constraints = getColumnConstraint(targetColumnClass);

                        // Collect type, references, and source column info
                        let tooltipData;
                        if (constraints) {
                            tooltipData = constraints.map((constraint) => {
                                const reference = constraint.references ? `(${constraint.references})` : '';
                                return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
                            }).join(', ');
                        }
                        // Add source column info to the tooltip
                        const combinedTooltipData = !tooltipData ? `foreignKey: (${sourceColumnName})` : `${tooltipData}, foreignKey: (${sourceColumnName})`;

                        if (relationship === 'fk') {
                            addForeignKeyIconToColumn(targetColumn, combinedTooltipData, tooltipData);
                        }

                        // setTimeout(() => {
                        //     handleRowClick(sourceColumnIndex, sourceColumn);
                        //     setTimeout(() => {
                        //         handleRowClick(targetColumnIndex, targetColumn);
                        //     }, 100 * (index + 1));
                        // }, 100 * index);
                    });
                }

                jsPlumbInstanceRef.current.recalculateOffsets(jsPlumbRef.current);
            });

            jsPlumbInstanceRef.current.repaintEverything();
        }
    }

    const restoreFromTablesAndConnections = (savedTables, savedConnections) => {
        if (!savedTables || !savedConnections || !Array.isArray(savedTables) || !Array.isArray(savedConnections)) {
            console.error("Invalid saved tables or connections input");
            return;
        }

        // jsPlumb instance handling for existing tables and connections
        if (jsPlumbInstanceRef.current) {
            // Restore each table from savedTables
            savedTables.forEach((table) => {
                setupTableForSelectedTable(
                    jsPlumbRef.current,
                    table,
                    jsPlumbInstanceRef.current,
                    table.uuid,
                    false,
                    handleRowClick,
                    handlePreViewButtonClick,
                    handleTableInfoClick,
                    handleActivateTableClick,
                    handleDeActivateTableClick,
                    handleDataQualityButtonClick,
                    handleTableDoubleClick,
                );
            });

            // Restore connections from savedConnections
            requestAnimationFrame(() => {
                savedConnections.forEach(({ sourceId, targetId, relationship, relationship_type }) => {
                    connectEndpoints(jsPlumbInstanceRef.current, sourceId, targetId, relationship, relationship_type, false);

                    const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);
                    const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
                    const targetColumnClass = targetColumn.children[0].classList[0];
                    const constraints = getColumnConstraint(targetColumnClass);

                    // Collect type, references, and source column info
                    let tooltipData;
                    if (constraints) {
                        tooltipData = constraints.map((constraint) => {
                            const reference = constraint.references ? `(${constraint.references})` : '';
                            return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
                        }).join(', ');
                    }

                    // Add source column info to the tooltip
                    const combinedTooltipData = `${tooltipData}, foreignKey: (${sourceColumnName})`;

                    if (relationship === 'fk') {
                        addForeignKeyIconToColumn(targetColumn, combinedTooltipData, tooltipData);
                    }

                    // Trigger row click and connections handling
                    // if (!sourceColumn.classList.contains('checked-column') && !targetColumn.classList.contains('checked-column')) {
                    //     requestAnimationFrame(() => {
                    //         handleRowClick(sourceColumnIndex, sourceColumn);
                    //         requestAnimationFrame(() => {
                    //             handleRowClick(targetColumnIndex, targetColumn);
                    //         });
                    //     });
                    // }
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
            namespace: ["lightning", "catalog", name],  // Use the appropriate namespace
            tables: tables.map((table) => {
                if (!table.desc) {
                    console.warn(`Table ${table.name} does not have a 'desc' property.`);
                    return {
                        fqn: table.name.split('.'),  // Split the table name into an array
                        columnSpecs: [],
                    };
                }

                const tableElement = document.getElementById(`table-${table.uuid}`);
                const left = tableElement ? parseFloat(tableElement.style.left) : 0;
                const top = tableElement ? parseFloat(tableElement.style.top) : 0;

                const columnSpecs = table.desc.map((col) => {
                    // Initialize object for each column
                    const columnObj = {
                        name: col.col_name,
                        dataType: col.data_type,
                    };

                    // Handling primaryKey at the column level
                    if (col.primaryKey && col.primaryKey.columns.length === 0) {
                        columnObj.primaryKey = { columns: [] };
                    }

                    // Handling notNull constraint
                    if (col.notNull && col.notNull.columns.length === 0) {
                        columnObj.notNull = { columns: [] };
                    }

                    // Handling unique constraint
                    if (table.uniqueConstraints && table.uniqueConstraints.some(u => u.column === col.col_name)) {
                        columnObj.unique = { columns: [] };
                    }

                    return columnObj;
                });

                // Collect foreignKeys at the table level
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
                    fqn: table.name.split('.'),  // FQN split into an array
                    columnSpecs: columnSpecs,  // Column specs including primaryKey, notNull, and unique constraints
                    ifNotExit: table.ifNotExit || false,  // Handle ifNotExist flag
                    namespace: table.namespace || [],  // Add namespace
                    unique: table.uniqueConstraints || [],  // Ensure unique constraints are included
                    foreignKeys: foreignKeys,  // Table-level foreign keys
                    dqAnnotations: [],  // Add empty dqAnnotations
                    acAnnotations: [],  // Add empty acAnnotations
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
    
        const existingTables = JSON.parse(localStorage.getItem('savedTables')) || [];
    
        const newTables = ddlJson.tables
            .filter((table) => {
                const tableName = `lightning.${ddlJson.namespace.join('.')}.${ddlJson.name}.${table.name}`;
                return !existingTables.some(existingTable => existingTable.name === tableName);
            })
            .map((table) => {
                const foreignKeyConstraints = [];
    
                const columns = table.columnSpecs.map((col) => {
                    const colObj = {
                        col_name: col.name,
                        data_type: col.dataType,
                        ...(col.primaryKey ? { primaryKey: { columns: [] } } : {}),
                        ...(col.notNull ? { notNull: { columns: [] } } : {}),
                        ...(col.unique ? { unique: { columns: [] } } : {}),
                        ...(col.foreignKey ? {
                            foreignKey: {
                                columns: col.foreignKey.columns || [],
                                refTable: col.foreignKey.refTable || [],
                                refColumns: col.foreignKey.refColumns || []
                            }
                        } : {})
                    };
    
                    if (col.foreignKey) {
                        foreignKeyConstraints.push({
                            column: col.name,
                            references: `${col.foreignKey.refTable.join('.')}.${col.foreignKey.refColumns.join(',')}`
                        });
                    }
                    return colObj;
                });
    
                const position = table.position || { left: 100 + Math.random() * 100, top: 100 + Math.random() * 100 };
                return {
                    name: `lightning.${ddlJson.namespace.join('.')}.${ddlJson.name}.${table.name}`,
                    desc: columns,
                    foreignKeyConstraints: foreignKeyConstraints.length ? foreignKeyConstraints : [],
                    uuid: `${uuidv4()}`,
                    position: position
                };
            });
    
        const allTables = [...existingTables, ...newTables];
    
        const savedConnections = [];
        ddlJson.tables.forEach((table) => {
            table.columnSpecs.forEach((col) => {
                if (col.foreignKey) {
                    const sourceTable = `lightning.${ddlJson.namespace.join('.')}.${ddlJson.name}.${table.name}`;
                    const sourceTableUuid = allTables.find(t => t.name === sourceTable)?.uuid;
    
                    // Corrected targetTableName formation
                    const targetTableName = col.foreignKey.refTable.join('.');
                    const targetTableObj = allTables.find(t => t.name === targetTableName);
    
                    if (sourceTableUuid && targetTableObj) {
                        const sourceId = `table-${sourceTableUuid}-col-${table.columnSpecs.findIndex(c => c.name === col.name) + 1}-left`;
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
    
        localStorage.setItem('savedTables', JSON.stringify(allTables));
        localStorage.setItem('connections', JSON.stringify(savedConnections));
    
        return { savedTables: allTables, savedConnections, rulesData: {} };
    };    

    const handlePreViewButtonClick = (tableName) => {
        // console.log(`Preview button clicked for table Name: ${tableName}`);
        runQuery(`SELECT * FROM ` + tableName).then(() => {
            setCondition('preview');
        });
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
            console.log(sourceId, targetId)
            const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);
            // Operation to change svg image in target column
            const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
            const targetColumnClass = targetColumn.children[0].classList[0];
            const constraints = getColumnConstraint(targetColumnClass);

            // Collect type, references, and source column info
            let tooltipData;
            if (constraints) {
                tooltipData = constraints.map((constraint) => {
                    const reference = constraint.references ? `(${constraint.references})` : '';
                    return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
                }).join(', ');
            }

            // Add source column info to the tooltip
            const combinedTooltipData = `${tooltipData}, foreignKey: (${sourceColumnName})`;

            if (relationship === 'fk') {
                addForeignKeyIconToColumn(targetColumn, combinedTooltipData, tooltipData);
            }

            // if (!sourceColumn.classList.contains('checked-column') && !targetColumn.classList.contains('checked-column')) {
            //     requestAnimationFrame(() => {
            //         handleRowClick(sourceColumnIndex, sourceColumn);
            //         requestAnimationFrame(() => {
            //             handleRowClick(targetColumnIndex, targetColumn);
            //         });
            //     });
            // }

            // Update DDL after adding new connection
            // const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];
            // const savedConnections = JSON.parse(localStorage.getItem('connections')) || [];
            // const updatedDDL = generateDDLJsonFromSettingDatas(savedTables, savedConnections);
        }

        setIsModalOpen(false);
    };

    const handleDirectConnection = (info) => {
        // console.log("direct call?")
    }

    const runQuery = async (sqlQuery) => {
        if (!sqlQuery || sqlQuery.trim() === "") {
            setQueryResult({ error: "No table connections. Please check." });
            return;
        }

        try {
            setLoading(true); // Start loading
            const result = await fetchApi(sqlQuery); // Call Api method in common.js
            setLoading(false); // Stop loading after query completes

            if (result) {
                if (result.error) {
                    setQueryResult({ error: result.message });
                } else {
                    const parsedResult = result.map((item) => JSON.parse(item));
                    if (Array.isArray(parsedResult) && parsedResult.length > 0) {
                        // console.log(parsedResult)
                        setQueryResult(<RenderTableForApi data={parsedResult} />); // Display the result in a table
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
        }));

        return (
            <MaterialReactTable
                columns={columns}
                data={data}
                enableSorting={true}
                enableColumnFilters={true}
                initialState={{
                    density: 'compact',
                }}
            />
        );
    };

    const RenderDQTable = ({ data }) => {
        const [dqData, setDQData] = useState(data);
        const [selectedRows, setSelectedRows] = useState({});

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
                Cell: ({ cell }) => renderStatus(cell.getValue())
            },
            { accessorKey: 'Name', header: 'Name' },
            { accessorKey: 'Table', header: 'Table' },
            { accessorKey: 'Type', header: 'Type' },
            { accessorKey: 'Expression', header: 'Expression' },
            {
                accessorKey: 'Total_record',
                header: 'Total Records',
                Cell: ({ cell }) => renderFetchingStatus(cell.getValue())
            },
            {
                accessorKey: 'Good_record',
                header: 'Good Records',
                Cell: ({ cell }) => renderFetchingStatus(cell.getValue())
            },
            {
                accessorKey: 'Bad_record',
                header: 'Bad Records',
                Cell: ({ cell }) => renderFetchingStatus(cell.getValue())
            },
        ];

        const renderFetchingStatus = (value) => {
            if (value === 'fetching data...') {
                return <span>fetching data...</span>;
            }
            return value || '';
        };

        const renderStatus = (status) => {
            if (status === 'loading') {
                return <Spinner className="spinner" style={{ width: '20px', height: '20px', fill: '#27A7D2' }} />;
            } else if (status === 'success') {
                return <CircleCheck style={{ width: '20px', height: '20px', fill: 'green' }} />;
            } else if (status === 'error') {
                return <CircleCheck style={{ width: '20px', height: '20px', fill: 'green' }} />;
            } else if (status === 'warning') {
                return <CircleCheck style={{ width: '20px', height: '20px', fill: 'green' }} />;
            }
            return null;
        };

        return (
            <MaterialReactTable
                columns={dqColumns}
                data={dqData}
                enableSorting
                enableColumnFilters
                initialState={{ density: 'compact' }}
            />
        );
    };

    const createDynamicColumns = () => {
        // Create a fixed title for the first column
        const dynamicColumns = [{
            accessorKey: 'title',
            header: 'original Column Name', // Set the title for the first column
            Cell: ({ cell }) => <strong>{cell.getValue()}</strong> // Display the title in bold
        }];

        // Generate the remaining dynamic columns based on rulesData.columns
        rulesData.columns.forEach((colData, index) => {
            colData[0] = colData[0].split('.').pop();
            const fullHeader = rulesData.columnHeaders?.[index] || `Column ${index + 1}`;
            const headerName = fullHeader.split('.').pop();
            const headerPrefix = fullHeader.split('.').slice(0, 2).join('.');
            dynamicColumns.push({
                accessorKey: `column_${index}`,
                header: `${headerName} (${headerPrefix})` || `Column ${index + 1}`, // Use the clicked row's name as the header
                Cell: ({ row }) => (
                    row.index === 1 ? ( // Check if it's the second row (index 1)
                        <select
                            value={colData[row.index] || '0'} // Default value as 'No Sorting'
                            onChange={(e) => handleInputChange(index, row.index, e.target.value)}
                            style={{ width: '50%', minWidth: '150px' }} // Apply consistent width
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
                            style={{ width: '50%', minWidth: '150px' }} // Apply consistent width
                            onKeyDown={(e) => {
                                // Prevent table navigation when using arrow keys inside the input
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

        // Map the table data to have titles as rows and the rest as columns
        const tableData = rulesData.title.map((title, index) => {
            const rowData = { title }; // Set the title as the first column
            rulesData.columns.forEach((colData, colIndex) => {
                rowData[`column_${colIndex}`] = colData[index] || ''; // Add data to the remaining columns
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

    const handleRowClick = (rowIndex, rowData) => {
        // console.log(`Row ${rowIndex + 1} clicked`, rowData);

        // const columnName = rowData.querySelectorAll('td')[0]?.innerText || ''; // Get the name of the column
        // Find the `td` with the class name that starts with `lightning` (full path)
        const fullPathClass = [...rowData.querySelectorAll('td')[0].classList].find(className => className.startsWith('lightning'));
        const columnName = fullPathClass.split('.').slice(-3).join('.') || '';

        if (!fullPathClass) {
            console.error('Full path class not found');
            return;
        }

        // Split the full path by '.' to separate the path components
        const pathComponents = fullPathClass.split('.');

        // Remove the last component (column name) to get the table path
        const tableId = pathComponents.slice(0, -1).join('.');

        setRulesData((prevData) => {
            if (condition !== 'rules') setCondition('rules');
            let newColumns = [...prevData.columns];
            let newColumnHeaders = [...(prevData.columnHeaders || [])];
            let newColumnTableIds = [...(prevData.columnTableIds || [])];

            const colIndexToRemove = newColumnHeaders.indexOf(columnName);

            if (rowData.classList.contains('checked-column')) {
                // Remove the column if it's already highlighted (lightblue)
                rowData.style.backgroundColor = ''; // Reset the color
                rowData.classList.remove('checked-column'); // Remove the checked-column class

                if (colIndexToRemove !== -1) {
                    // Remove the corresponding column header and data
                    newColumnHeaders.splice(colIndexToRemove, 1);
                    newColumns.splice(colIndexToRemove, 1);
                    newColumnTableIds.splice(colIndexToRemove, 1);
                }
            } else {
                // Add a new column if it's not highlighted
                rowData.style.backgroundColor = 'lightblue'; // Set the new color
                rowData.classList.add('checked-column'); // Add the checked-column class

                // Add the column name as a new header and create a new column array only if it's not already present
                if (colIndexToRemove === -1) {
                    newColumnHeaders.push(columnName);
                    newColumns.push([]); // Add a new column array
                    newColumnTableIds.push(tableId); // Add the associated table ID

                    // Add the clicked row's name as the first value of the new column
                    newColumns[newColumns.length - 1][0] = columnName;
                }
            }

            // Ensure the data remains unique
            newColumnHeaders = Array.from(new Set(newColumnHeaders));
            newColumns = newColumns.filter((_, index) => index < newColumnHeaders.length);
            // newColumnTableIds = Array.from(new Set(newColumnTableIds)).filter((_, index) => index < newColumnHeaders.length);

            return { ...prevData, columns: newColumns, columnHeaders: newColumnHeaders, columnTableIds: newColumnTableIds };
        });
        // Increment currentColumnIndex to move to the next column
        setCurrentColumnIndex(currentColumnIndex + 1);
    };

    const handleInputChange = (colIndex, rowIndex, value) => {
        const updatedColumns = [...rulesData.columns];

        // Update the value in the specified column and row
        updatedColumns[colIndex][rowIndex] = value;

        setRulesData({ ...rulesData, columns: updatedColumns });
    };

    const renderResults = (position) => {
        if (loading) {
            return <div>Fetching data...</div>; // Show loading indicator while fetching data
        }

        if (queryResult?.error) {
            return <div>{queryResult.error}</div>;
        }

        if (condition === 'preview') {
            return <div>{queryResult ? queryResult : "Generating SQL..."}</div>;
        } else if (condition === 'build') {
            return <div>{queryResult ? queryResult : "Generating SQL..."}</div>;
        } else if (condition === 'rules') {
            return renderRulesTable();
        } else if(condition === 'dqResult'){
            return dqResults.length > 0 ? <RenderDQTable data={dqResults} /> : null;
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
                console.log(response.message)
                setPopupMessage(`Failed to compile USL: ${response.message}`);
            } else {
                setPopupMessage('USL compiled successfully');
                return response;
            }
        } catch (error) {
            setPopupMessage(`Error in compileUSL:, ${error}`);
        }
    };

    const onSaveChanges = (updatedTableInfo) => {
        console.log(updatedTableInfo)
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

        // window.location.reload();

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
                    <div
                        style={{
                            position: 'relative',
                            height: `${position - reSizingOffset}px`,
                            overflow: 'hidden',
                        }}
                    >
                        <div
                            ref={jsPlumbRef}
                            style={{
                                width: '5000px',
                                height: '5000px',
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
                            {/* Table and connection points will be added here */}
                        </div>
                    </div>

                    <div className="btn-line">
                        <div className="button-group">
                            {/* <button className="btn-primary" onClick={() => setCondition('preview')}>Preview</button> */}
                            {/* <button className="btn-primary" onClick={(handleBuildClick)}>Build</button> */}
                            {/* <button className="btn-primary" onClick={() => setCondition('rules')}>Rules</button> */}
                            {/* <button className="btn-primary" onClick={(saveSemanticLayer)}>Save</button> */}
                            {/* <button className="btn-primary" onClick={(getQueryMapping)}>Query Mapping</button> */}
                            {/* <button className="btn-primary" onClick={(getLineage)}>Lineage</button> */}
                            {/* <button className="btn-primary" onClick={(deleteAllTables)}>Delete USL</button> */}
                            <button className="btn-primary" onClick={(loadDQ)}>Load Data Qaulity</button>
                            <button className="btn-primary" onClick={(runDQ)}>Run Data Qaulity</button>
                            <div className="zoom-controls">
                                {/* <button className="btn-primary" >Optimize View</button> */}
                                <LocationIcon onClick={() => handleOptimizeView(jsPlumbRef.current, zoomLevel, setZoomLevel, setOffset)} style={{ width: '30px', height: '30px', cursor: 'pointer' }} />
                                <MinusIcon onClick={() => handleZoomOut(jsPlumbRef.current, setZoomLevel, setOffset, jsPlumbInstanceRef.current)} style={{ width: '30px', height: '30px', cursor: 'pointer' }} />
                                <div className="zoom-level">{Math.round(zoomLevel * 100)}%</div>
                                <PlusIcon onClick={() => handleZoomIn(jsPlumbRef.current, setZoomLevel, setOffset, jsPlumbInstanceRef.current)} style={{ width: '30px', height: '30px', cursor: 'pointer' }} />
                            </div>
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
                    <div className='result-box'
                        style={{ '--position-offset': `${position - reSizingOffset}px` }}
                    >
                        {renderResults(position)}
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