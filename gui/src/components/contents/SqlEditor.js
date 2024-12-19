import React, { useState, useEffect, useRef, useMemo } from 'react';
import Resizable from 'react-resizable-layout';
import { fetchApi, fetchApiForSaveFiles } from '../../utils/common';
import { MaterialReactTable } from 'material-react-table';
import { ReactComponent as XmarkIcon } from '../../assets/images/xmark-solid.svg';
import './Contents.css';
import Editor from './components/Editor';
import { queryBookContents, queryBookColumns } from '../configs/editorConfig';

import { useDispatch, useSelector } from 'react-redux';
import { setQueryResult, addQueryToHistory } from '../../store/querySlice';

function SqlEditor({ toggleRefreshNav, previewTableName, isMouseLoading, navErrorMsg, setNavErrorMsg }) {
    const dispatch = useDispatch();
    const queryResult = useSelector((state) => state.query.queryResult);
    const queryHistory = useSelector((state) => state.query.queryHistory);

    const [editors, setEditors] = useState([]);
    const [activeEditor, setActiveEditor] = useState(null);
    const [showHistory, setShowHistory] = useState(false);
    const [loading, setLoading] = useState(false);
    const [showQueryBook, setShowQueryBook] = useState(false);
    const [popupMessage, setPopupMessage] = useState(null);
    const [popupMessages, setPopupMessages] = useState([]);

    const offset = 80;
    const resizingRef = useRef(false);
    const [editorInstances, setEditorInstances] = useState({});

    const addPopupMessage = (message) => {
        setPopupMessages((prevMessages) => [...prevMessages, message]);
    };

    useEffect(() => {
        const runPreviewQuery = async () => {
            if (!previewTableName || !previewTableName.startsWith('lightning.datasource')) return;

            setLoading(true);
            const query = `SELECT * FROM ${previewTableName} LIMIT 100`;
            const result = await fetchApi(query);

            setLoading(false);

            if (result?.error) {
                dispatch(setQueryResult({ error: result.message }));
            } else {
                const parsedResult = result.map((item) => JSON.parse(item));
                dispatch(setQueryResult(parsedResult));
            }
        };

        runPreviewQuery();
    }, [previewTableName, dispatch]);

    useEffect(() => {
        isMouseLoading ? document.body.style.cursor = "wait" : document.body.style.cursor = "default";
    }, [isMouseLoading])

    useEffect(() => {
        if(sessionStorage.getItem('selectedTab')==='sqlEditor'){
            dispatch(setQueryResult({ error: navErrorMsg }));
        }
    }, [navErrorMsg])

    useEffect(() => {
        const storedEditors = localStorage.getItem('editors');
        if (storedEditors) {
            const parsedEditors = JSON.parse(storedEditors);
            setEditors(parsedEditors);
            if (parsedEditors.length > 0) {
                setActiveEditor(parsedEditors[0].id);
            }
        } else {
            setEditors([{ id: 1, name: 'Editor 1', content: '' }]);
            setActiveEditor(1);
        }

        setLoading(false);
    }, []);

    useEffect(() => {
        if (editors.length > 0) {
            localStorage.setItem('editors', JSON.stringify(editors));
        }
    }, [editors]);

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

    const openQueryBook = () => setShowQueryBook(true);
    const closeQueryBook = () => setShowQueryBook(false);

    const removeComments = (query) => {
        const lines = query.split('\n');
        const cleanLines = lines.filter(line => !line.trim().startsWith('--'));
        return cleanLines.join('\n').trim();
    };

    const handleEditorChange = (newValue, id) => {
        setEditors((prevEditors) =>
            prevEditors.map((editor) =>
                editor.id === id ? { ...editor, content: newValue } : editor
            )
        );
    };

    const addEditor = () => {
        const newId = editors.length + 1;
        setEditors([...editors, { id: newId, name: `Editor ${newId}`, content: '' }]);
        setActiveEditor(newId);
    };

    const switchEditor = (id) => setActiveEditor(id);

    const deleteEditor = (id) => {
        const updatedEditors = editors
            .filter((editor) => editor.id !== id)
            .map((editor, index) => ({
                ...editor,
                id: index + 1,
                name: `Editor ${index + 1}`,
            }));
        setEditors(updatedEditors);
        if (updatedEditors.length > 0) {
            setActiveEditor(updatedEditors[0].id);
        } else {
            setActiveEditor(null);
        }
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

    const runQuery = async () => {
        const activeEditorInstance = editorInstances[activeEditor];
        const selectedText = activeEditorInstance?.getSelectedText();

        const activeEditorContent = editors.find((editor) => editor.id === activeEditor)?.content;
        const cleanQuery = selectedText && selectedText.trim() !== ""
            ? selectedText
            : removeComments(activeEditorContent);

        if (!cleanQuery || cleanQuery.trim() === "") {
            dispatch(setQueryResult({ error: "Query is empty. Please provide a valid SQL query." }));
            return;
        }

        const queries = cleanQuery.split(";").map((query) => query.trim()).filter((query) => query);

        if (queries.length === 0) {
            dispatch(setQueryResult({ error: "No valid queries found. Please check your SQL input." }));
            return;
        }

        setLoading(true);

        dispatch(addQueryToHistory({ query: cleanQuery, timestamp: new Date().toLocaleString() }));

        for (const query of queries) {
            try {
                const result = await fetchApi(query);

                if (result?.error) {
                    // setPopupMessage(`Error executing query: "${query}". Error: ${result.message}`);
                    dispatch(setQueryResult({ error: result.message }));
                    // addPopupMessage(`Error executing query: "${query}". Error: ${result.message}`);
                } else {
                    const parsedResult = expandDuplicateKeys(result);
                    dispatch(setQueryResult(parsedResult));
                }
            } catch (error) {
                // setPopupMessage(`Unexpected error executing query: "${query}". Error: ${error.message}`);
                // addPopupMessage(`Unexpected error executing query: "${query}". Error: ${error.message}`);
                dispatch(setQueryResult({ error: error.message }));
            }
        }

        setLoading(false);
    };

    useEffect(() => {
        const handleKeyPress = (event) => {
            if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
                event.preventDefault();
                runQuery();
            }
        };

        document.addEventListener('keydown', handleKeyPress);

        return () => {
            document.removeEventListener('keydown', handleKeyPress);
        };
    }, [runQuery]);

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

    const RenderTableForApi = ({ data }) => {
        if (!Array.isArray(data)) {
            // console.log(data)
            // console.error("Expected an array, but received:", data);
            return [];
        }

        if (!data || data.length === 0) {
            return <div>No data available</div>;
        }

        const normalizedData = normalizeData(data);

        const columns = Object.keys(normalizedData[0]).map((key) => ({
            accessorKey: key,
            header: key.charAt(0).toUpperCase() + key.slice(1),
            muiTableHeadCellProps: { align: 'center' },
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
            <MaterialReactTable
                columns={columns}
                data={normalizedData}
                enableSorting={true}
                enableColumnFilters={true}
                // onPaginationChange={setPagination}
                // state={{ pagination }}
                initialState={{
                    density: 'compact',
                    pagination: { pageSize: 30, pageIndex: 0 },
                }}
            />
        );
    };

    const renderTable = () => {
        if (loading) return <div>Fetching data...</div>;

        if (showHistory) {
            return renderHistory();
        }

        if (!queryResult) return <div>No result available</div>;
        if (queryResult.error) return <div>Error: {queryResult.error}</div>;

        return <RenderTableForApi data={queryResult} />;
    };

    const memoizedRenderTable = useMemo(() => {
        if (loading) return <div>Fetching data...</div>;

        if (showHistory) {
            return renderHistory();
        }

        if (!queryResult) return <div>No result available</div>;
        if (queryResult.error) return <div>Error: {queryResult.error}</div>;

        return <RenderTableForApi data={queryResult} />;
    }, [loading, showHistory, queryResult]);


    const renderQueryBook = () => {
        const queryBookData = queryBookContents;
        const columns = queryBookColumns;

        const handleRowClick = (query) => {
            setEditors((prevEditors) =>
                prevEditors.map((editor) =>
                    editor.id === activeEditor
                        ? { ...editor, content: `${editor.content}\n${query}` }
                        : editor
                )
            );
            closeQueryBook();
        };

        return (
            <div className="popup">
                <div className="popup-title">Query Book</div>
                <div className="popup-content">
                    <MaterialReactTable
                        columns={columns}
                        data={queryBookData}
                        enableSorting={true}
                        enableColumnFilters={true}
                        initialState={{ density: 'compact' }}
                        muiTableBodyRowProps={({ row }) => ({
                            onClick: () => handleRowClick(row.original.query),
                            style: { cursor: 'pointer' },
                        })}
                    />
                </div>
                <div className="popup-buttons">
                    <button className="btn-primary bold-text" onClick={closeQueryBook}>
                        Close
                    </button>
                </div>
            </div>
        );
    };

    function renderHistory() {
        if (queryHistory.length === 0) {
            return <div>No query history available.</div>;
        }

        const columns = [
            { accessorKey: 'timestamp', header: 'Timestamp', muiTableHeadCellProps: { align: 'center' } },
            { accessorKey: 'query', header: 'Query', muiTableHeadCellProps: { align: 'center' } },
        ];

        const handleRowClick = (query) => {
            setEditors((prevEditors) =>
                prevEditors.map((editor) =>
                    editor.id === activeEditor
                        ? { ...editor, content: `${editor.content}\n${query}` }
                        : editor
                )
            );
        };

        return (
            <MaterialReactTable
                columns={columns}
                data={queryHistory}
                enableSorting={true}
                enableColumnFilters={true}
                initialState={{ density: 'compact' }}
                muiTableBodyRowProps={({ row }) => ({
                    onClick: () => handleRowClick(row.original.query),
                    style: { cursor: 'pointer' },
                })}
            />
        );
    }

    const saveSQL = async () => {
        const activeEditorContent = editors.find(editor => editor.id === activeEditor)?.content;
        if (!activeEditorContent) {
            console.error("No content to save");
            return;
        }

        const fileName = `editor_${activeEditor}_content`;
        const extension = "sql";
        const response = await fetchApiForSaveFiles(activeEditorContent, fileName, extension);

        if (response.error) {
            console.error("Failed to save SQL:", response.message);
        } else {
            // console.log("SQL saved successfully:", response.message);
            // setPopupMessage("The file has been successfully saved in the env/ligt-model folder.");
        }
    };

    const closePopup = () => setPopupMessage(null);

    const closePopups = (messageToRemove) => {
        setPopupMessages((prevMessages) =>
            prevMessages.filter((message) => message !== messageToRemove)
        );
    };


    return (
        <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
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
                        <div className="btn-line">
                            <div style={{ display: 'flex', alignItems: 'center', overflowX: 'auto', whiteSpace: 'nowrap', flexGrow: 1, gap: '10px' }}>
                                {editors.map((editor) => (
                                    <div key={editor.id} style={{ display: 'inline-block' }}>
                                        <button
                                            className={`btn-editor ${activeEditor === editor.id ? 'selected' : ''}`}
                                            onClick={() => switchEditor(editor.id)}
                                        >
                                            <span className="btn-editor-label">
                                                {editor.name}
                                                <div className="btn-editor-close" onClick={(e) => { e.stopPropagation(); deleteEditor(editor.id); }}>
                                                    <XmarkIcon
                                                        className="editor-xmark"
                                                        style={{ fill: activeEditor === editor.id ? 'white' : '#27A7D2' }}
                                                    />
                                                </div>
                                            </span>
                                        </button>
                                    </div>
                                ))}
                                <button className="btn-primary" onClick={addEditor}>Add</button>
                            </div>
                            <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <button className="btn-primary" onClick={runQuery}>Run</button>
                                <button className="btn-primary" onClick={() => setShowHistory(!showHistory)}>
                                    {showHistory ? 'Hide History' : 'Show History'}
                                </button>
                                {/* <button className="btn-primary" onClick={openQueryBook}>Query Book</button> */}
                            </div>
                        </div>
                        <div className="seperator-content"></div>
                        <div style={{ height: '10px', backgroundColor: '#fff' }}></div>
                        <div style={{ height: position - offset }}>
                            {editors.map(
                                (editor) =>
                                    activeEditor === editor.id && (
                                        <Editor
                                            key={editor.id}
                                            id={editor.id}
                                            content={editor.content}
                                            onChange={(newValue) => handleEditorChange(newValue, editor.id)}
                                            setEditorInstances={setEditorInstances}
                                            isMouseLoading={isMouseLoading}
                                        />
                                    )
                            )}
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
                        <div className='result-box' style={{ '--position-offset': `${position - offset}px`, display: 'block' }}>
                            {/* {loading ? <div>Fetching data...</div> : renderTable()} */}
                            {memoizedRenderTable}
                        </div>

                        {showQueryBook && renderQueryBook()}
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

                        {popupMessages.length > 0 && (
                            <div className="popup-overlay">
                                {popupMessages.map((msg, index) => (
                                    <div
                                        key={index}
                                        className="popup-message"
                                        onClick={(e) => e.stopPropagation()}
                                        style={{
                                            position: 'absolute',
                                            top: `30%`,
                                            left: '50%',
                                            transform: 'translate(-50%, -50%)',
                                            zIndex: 1000 + index,
                                        }}
                                    >
                                        <p>{msg}</p>
                                        <div className="popup-buttons">
                                            <button
                                                className="btn-primary"
                                                onClick={() => closePopups(msg)}
                                            >
                                                Close
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </Resizable>
        </div>
    );
}

export default SqlEditor;