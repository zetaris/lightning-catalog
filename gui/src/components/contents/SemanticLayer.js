import React, { useRef, useState, useEffect } from 'react';
import Resizable from 'react-resizable-layout';
import { initializeJsPlumb, setupTableForSelectedTable, connectEndpoints, handleOptimizeView, handleZoomIn, handleZoomOut, getColumnConstraint, getRowInfo, addForeignKeyIconToColumn } from '../configs/JsPlumbConfig';
import { v4 as uuidv4 } from 'uuid';
import { fetchApi, fetchApiForSave, fetchCompileUSLApi, fetchActivateTableApi, fetchApiForListSemanticLayers, deleteActivateTableApi, fetchUSLFileContent } from '../../utils/common';
import './Contents.css';
import './SemanticLayer.css'
import RelationshipModal from './RelationshipModal';
import { MaterialReactTable } from 'material-react-table';
import { ReactComponent as MinusIcon } from '../../assets/images/square-minus-regular.svg';
import { ReactComponent as PlusIcon } from '../../assets/images/square-plus-regular.svg';
import { ReactComponent as KeyIcon } from '../../assets/images/key-outline.svg';
import { ReactComponent as LinkIcon } from '../../assets/images/link-solid.svg';
import { ReactComponent as LocationIcon } from '../../assets/images/location-crosshairs-solid.svg';
import { createRoot } from 'react-dom/client';
import { TableInfoSlider } from './TableInfoSlider';
import Editor from './components/Editor.js';
import DataQualityPopup from './components/DataQualityPopup.js';
import ActivePopup from './components/ActivatePopup.js';
import { colors } from '@mui/material';

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
        alert("developing");
    }

    const getQueryMapping = () => {
        alert("developing");
    }

    const handleTableDoubleClick = () => {
        console.log("handleTableDoubleClick");
    }

    const handleDataQualityButtonClick = () => {
        setShowDQPopup(true);
    };

    const handleEditorChange = (newContent) => {
        setEditorContent(newContent);
    };

    const handleDeActivateTableClick = async (table) => {
        const fileName = table.name.split('.').pop();
        const result = await deleteActivateTableApi(fileName);
        if (result) {
            setPopupMessage(result);
            updateActivatedTables(false)
        }
    }

    const handleActivateTableClick = (table) => {
        setActivateTargetTable(table);
        setactivateTable(true);

        if (!activateTable) {
            setShowActivePopup(true);
        }
    };

    const handleSubmitActivateQuery = async (table, query) => {
        if (!table) {
            console.error("No table selected");
            return;
        }

        const queryIndex = query.expression.indexOf('SELECT');
        if (queryIndex === -1) {
            console.error("Invalid query expression: SELECT statement not found.");
            return;
        }

        const selectQuery = query.expression.substring(queryIndex);
        const requestData = {
            table: table.name,
            query: selectQuery
        };

        let result = await fetchActivateTableApi(requestData);
        if (!result.error) {
            setPopupMessage(`The ${result.name} table has been activated.`);
            setActivateTables((prevTables) => {
                const updatedTables = [...prevTables, table.name];
                updateActivatedTables(true, updatedTables);
                return updatedTables;
            });
        } else {
            setPopupMessage(result.error);
            updateActivatedTables(false);
        }
        fetchActivateTableData();
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
                        const { savedTables, savedConnections, rulesData } = getSettingDataFromJson(JSON.stringify(parsedDDLResult));
                        // parseJSONAndRestore(parsedDDLResult);
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
            const fileName = uslNamebyClick ? `${uslNamebyClick}_usl.json` : null;

            if (!fileName) return;

            try {
                const result = await fetchUSLFileContent(fileName);
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
        const result = await fetchApiForListSemanticLayers();
        if (!result.error) {
            const filteredTableNames = result
                .filter(fileName => fileName.endsWith('_table_query.json'))
                .map(fileName => fileName.replace('_table_query.json', ''));
            setActivateTables(filteredTableNames);
        } else {
            console.error("Failed to fetch semantic layers: ", result.message);
        }
    };

    function updateActivatedTables(isActivate, activeTables = []) {
        const captions = document.querySelectorAll('.caption-text');
        const validActiveTables = Array.isArray(activeTables) ? activeTables : [];

        captions.forEach((caption) => {
            const tableElement = caption.closest('.table-container');
            if (tableElement) {
                if (isActivate && validActiveTables.includes(caption.innerText)) {
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
                        const combinedTooltipData = !tooltipData ? `foreign key: (${sourceColumnName})` : `${tooltipData}, foreign key: (${sourceColumnName})`;

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
        console.log(savedTables)
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
                    const combinedTooltipData = `${tooltipData}, foreign key: (${sourceColumnName})`;

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
            ddlJson = JSON.parse(ddlText); // parsing ddlText to JSON
            console.log(ddlJson)
        } catch (e) {
            // console.error("Invalid DDL JSON format: ", e);
            setPopupMessage(`Invalid DDL JSON format: , ${e}`);
            return null;
        }

        // Table Information Export from DDL Json
        const savedTables = ddlJson.tables.map((table) => {
            const foreignKeyConstraints = [];

            // Map each column to extract column details (including foreignKey, notNull, primaryKey, unique)
            const columns = table.columnSpecs.map((col) => {
                const colObj = {
                    col_name: col.name,
                    data_type: col.dataType,
                    // Add the constraint if it exists, otherwise exclude it
                    ...(col.primaryKey ? { primaryKey: { columns: [] } } : {}), // Handle PK
                    ...(col.notNull ? { notNull: { columns: [] } } : {}), // Handle NotNull
                    ...(col.unique ? { unique: { columns: [] } } : {}), // Handle Unique
                    ...(col.foreignKey ? {
                        foreignKey: {
                            columns: col.foreignKey.columns || [],
                            refTable: col.foreignKey.refTable || [],
                            refColumns: col.foreignKey.refColumns || []
                        }
                    } : {})
                };

                // If the column has a foreignKey, store the foreign key relation
                if (col.foreignKey) {
                    foreignKeyConstraints.push({
                        column: col.name,
                        references: `${col.foreignKey.refTable.join('.')}.${col.foreignKey.refColumns.join(',')}`
                    });
                }

                return colObj;
            });

            // Set default position if not available
            const defaultPosition = { left: 100 + Math.random() * 100, top: 100 + Math.random() * 100 };
            const position = table.position || defaultPosition;

            return {
                // Prepend 'lightning' and append USL name (ddlJson.name) to the table name
                name: `lightning.${ddlJson.namespace.join('.')}.${ddlJson.name}.${table.name}`,
                desc: columns,
                foreignKeyConstraints: foreignKeyConstraints.length > 0 ? foreignKeyConstraints : [],
                uuid: `${uuidv4()}`,  // Generate unique ID for each table
                position: position
            };
        });

        // Connections Information Export from DDL Json
        const savedConnections = [];
        ddlJson.tables.forEach((table) => {
            // Process each column for foreignKey relationships
            table.columnSpecs.forEach((col) => {
                if (col.foreignKey) {
                    const sourceColumn = col.name;
                    // Include USL name (ddlJson.name) in the table names
                    const sourceTableUuid = savedTables.find(t => t.name === `lightning.${ddlJson.namespace.join('.')}.${ddlJson.name}.${table.name}`).uuid;
                    const targetTable = col.foreignKey.refTable.join('.');
                    const targetTableObj = savedTables.find(t => t.name === `lightning.${ddlJson.namespace.join('.')}.${ddlJson.name}.${targetTable}`);

                    if (targetTableObj) {
                        const targetColumn = col.foreignKey.refColumns[0];  // Assume only one target column for simplicity
                        const sourceId = `table-${sourceTableUuid}-col-${table.columnSpecs.findIndex(c => c.name === sourceColumn) + 1}-left`;
                        const targetId = `table-${targetTableObj.uuid}-col-${targetTableObj.desc.findIndex(c => c.col_name === targetColumn) + 1}-right`;

                        savedConnections.push({
                            sourceId,
                            targetId,
                            relationship: 'fk',
                        });
                    } else {
                        console.warn(`Target table ${targetTable} not found for foreign key.`);
                    }
                }
            });
        });

        // Store the tables and connections in localStorage
        localStorage.setItem('connections', JSON.stringify(savedConnections));
        localStorage.setItem('savedTables', JSON.stringify(savedTables));

        return { savedTables, savedConnections, rulesData: {} };
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
            const combinedTooltipData = `${tooltipData}, foreign key: (${sourceColumnName})`;

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

    const addAliasesToQuery = (query) => {
        let aliasCounter = 1;

        // List to store table and alias pairs
        const tableAliases = [];

        // Function to generate a new alias
        const getNewAlias = () => `t${aliasCounter++}`;

        // Function to get the latest alias for a given table path
        const getLatestAlias = (tablePath) => {
            // Filter table aliases to find all matches, then get the latest
            const matches = tableAliases.filter(entry => entry.table === tablePath);
            return matches.length > 0 ? matches[matches.length - 1].alias : null;
        };

        // Split the query into parts based on FROM and JOIN clauses
        const fromJoinPattern = /(FROM|JOIN)\s+([a-zA-Z0-9._]+)/gi;
        let splitQuery = query.split(fromJoinPattern);

        let finalQuery = '';
        for (let i = 0; i < splitQuery.length; i++) {
            let part = splitQuery[i].trim();
            if (part.match(/^(FROM|JOIN)$/)) {
                // If it is 'FROM' or 'JOIN', add it back and continue
                finalQuery += part + ' ';
                continue;
            }

            // If it is a table path (following FROM or JOIN), add a unique alias
            if (i > 0 && (splitQuery[i - 1].trim() === 'FROM' || splitQuery[i - 1].trim() === 'JOIN')) {
                const tablePath = part;
                const cleanTablePath = tablePath; // Remove column if present
                const alias = getNewAlias();
                tableAliases.push({ table: cleanTablePath, alias: alias });
                finalQuery += `${tablePath} AS ${alias} `;
            } else {
                // Replace ON conditions with latest assigned aliases
                const onPattern = /ON\s+([a-zA-Z0-9._]+)\.([a-zA-Z0-9_]+)\s*=\s*([a-zA-Z0-9._]+)\.([a-zA-Z0-9_]+)/gi;
                part = part.replace(onPattern, (match, leftTablePath, leftColumn, rightTablePath, rightColumn) => {
                    // Get the latest aliases for both left and right table paths
                    const leftAlias = getLatestAlias(leftTablePath);
                    const rightAlias = getLatestAlias(rightTablePath);

                    // Check if either alias is null and handle it
                    if (!leftAlias || !rightAlias) {
                        throw new Error(`Alias for table not found: ${leftTablePath} or ${rightTablePath}`);
                    }

                    return `ON ${leftAlias}.${leftColumn} = ${rightAlias}.${rightColumn}`;
                });

                finalQuery += part + ' ';
            }
        }

        // Replace SELECT clause with aliases
        const selectPattern = /SELECT\s+([\s\S]+?)\s+FROM/i;
        finalQuery = finalQuery.replace(selectPattern, (match, columns) => {
            const columnList = columns.split(',').map(column => {
                const trimmedColumn = column.trim();

                // Split the column to extract table path and column name
                const lastDotIndex = trimmedColumn.lastIndexOf('.');
                if (lastDotIndex === -1) {
                    return trimmedColumn; // Return as is if no table name exists
                }

                const tablePath = trimmedColumn.slice(0, lastDotIndex);
                const columnName = trimmedColumn.slice(lastDotIndex + 1);

                // Get the alias for the table path
                const alias = getLatestAlias(tablePath);
                if (alias) {
                    return `${alias}.${columnName}`;
                }
                return trimmedColumn;
            });

            return `SELECT ${columnList.join(', ')} FROM `;
        });

        // Replace WHERE clause with aliases
        const wherePattern = /WHERE\s+([\s\S]+?)(\s+ORDER\s+BY|\s*$)/i;
        finalQuery = finalQuery.replace(wherePattern, (match, conditions, rest) => {
            const conditionList = conditions.split(/AND|OR/).map(condition => {
                const trimmedCondition = condition.trim();

                // Extract table path and column name if they exist
                const lastDotIndex = trimmedCondition.lastIndexOf('.');
                if (lastDotIndex === -1) {
                    return trimmedCondition; // Return as is if no table name exists
                }

                const tablePath = trimmedCondition.slice(0, lastDotIndex);
                const alias = getLatestAlias(tablePath);
                if (alias) {
                    return trimmedCondition.replace(tablePath, alias);
                }
                return trimmedCondition;
            });

            return `WHERE ${conditionList.join(' ')}${rest}`;
        });

        // Replace ORDER BY clause with aliases
        const orderByPattern = /ORDER\s+BY\s+([\s\S]+)$/i;
        finalQuery = finalQuery.replace(orderByPattern, (match, columns) => {
            const columnList = columns.split(',').map(column => {
                const trimmedColumn = column.trim();

                // Extract table path and column name if they exist
                const lastDotIndex = trimmedColumn.lastIndexOf('.');
                if (lastDotIndex === -1) {
                    return trimmedColumn; // Return as is if no table name exists
                }

                const tablePath = trimmedColumn.slice(0, lastDotIndex);
                const alias = getLatestAlias(tablePath);
                if (alias) {
                    return trimmedColumn.replace(tablePath, alias);
                }
                return trimmedColumn;
            });

            return `ORDER BY ${columnList.join(', ')}`;
        });

        return finalQuery.trim();
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
        }
    };

    const saveSemanticLayer = async () => {
        const name = prompt("Enter the name of the Semantic Layer:", "Default Name");
        const description = prompt("Enter the description of the Semantic Layer:", "Default Description");

        // 1. Get tables information
        const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];

        // 2. Get connections information
        const savedConnections = JSON.parse(localStorage.getItem('connections')) || [];

        // 3. Get selected columns and rules data
        const savedRulesData = { ...rulesData };

        // await updateTableColumns(savedTables);

        // 4. Combine everything into a single object
        const semanticLayerData = {
            tables: savedTables,
            connections: savedConnections,
            rulesData: savedRulesData,
        };

        const ddl = generateDDLJsonFromSettingDatas(savedTables, savedConnections, name, description);

        // console.log(JSON.stringify(ddl))

        // 5. Save to localStorage or send to a server
        localStorage.setItem('semanticLayer', JSON.stringify(semanticLayerData));

        let fileName = prompt("Please enter your file name:", "");

        try {
            const result = await fetchApiForSave(ddl, fileName);

            if (result.error) {
                console.error("Error saving semantic layer:", result.message);
            } else {
                console.log("SemanticLayer saved successfully on server:", result);
            }
        } catch (error) {
            console.error("Failed to save semantic layer to the server:", error);
        }
    }

    const compileUSL = async (name = "noname", ddlQuery, isDeploy = true) => {
        try {
            // First, run the query to create the namespace if it doesn't exist
            const createNamespaceQuery = "CREATE NAMESPACE IF NOT EXISTS lightning.metastore.usl;";
            const namespaceResponse = await fetchApi(createNamespaceQuery);

            if (namespaceResponse.error) {
                console.error('Failed to create namespace:', namespaceResponse.message);
                return; // If namespace creation fails, stop further execution
            }

            // Create the USL query
            let uslQuery = `COMPILE USL IF NOT EXISTS crmdb NAMESPACE lightning.metastore.usl DDL ${ddlQuery}`;

            // Prepare USL data
            const uslData = {
                name: name,
                ddl: uslQuery,
                deploy: isDeploy,
                ifNotExists: true,
                namespace: 'lightning.metastore.usl'
            };

            // Call the API to compile the USL
            const compileResponse = await fetchCompileUSLApi(uslData);

            if (compileResponse.error) {
                console.log(compileResponse.message)
                setPopupMessage(`Failed to compile USL: ${compileResponse.message}`);
            } else {
                setPopupMessage('USL compiled successfully');
                return compileResponse;
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

    return (
        <Resizable
            axis="y"
            initial={700}
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
                            <button className="btn-primary" onClick={(deleteAllTables)}>Delete USL</button>
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
                            height: '3px',
                            backgroundColor: '#ccc',
                            cursor: 'row-resize',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            position: 'relative',
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
                        />
                    )}

                    {activateTable && showActivePopup && (
                        <ActivePopup
                            onClose={handleCloseActivePopup}
                            onSubmit={(query) => handleSubmitActivateQuery(activateTargetTable, query)}
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