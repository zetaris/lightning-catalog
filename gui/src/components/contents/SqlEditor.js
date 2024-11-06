import React, { useState, useEffect, useRef } from 'react';
import Resizable from 'react-resizable-layout';
import { fetchApi, fetchApiForSaveFiles } from '../../utils/common';
import { MaterialReactTable } from 'material-react-table';
import { ReactComponent as XmarkIcon } from '../../assets/images/xmark-solid.svg';
import './Contents.css';
import Editor from './components/Editor';
import { queryBookContents, queryBookColumns } from '../configs/editorConfig';

function SqlEditor({ toggleRefreshNav }) {
    const [editors, setEditors] = useState([]);
    const [activeEditor, setActiveEditor] = useState(null);
    const [queryResult, setQueryResult] = useState('');
    const [showHistory, setShowHistory] = useState(false);
    const [queryHistory, setQueryHistory] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showQueryBook, setShowQueryBook] = useState(false);
    const [popupMessage, setPopupMessage] = useState(null);
    const offset = 115;
    const resizingRef = useRef(false);

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

    const runQuery = async () => {
        const activeEditorContent = editors.find((editor) => editor.id === activeEditor)?.content;
        if (!activeEditorContent) {
            setQueryResult({ error: 'No SQL query to run.' });
            return;
        }
        const cleanQuery = removeComments(activeEditorContent);
        if (!cleanQuery || cleanQuery.trim() === "") {
            setQueryResult({ error: "Query is empty. Please provide a valid SQL query." });
            return;
        }
        setLoading(true);
        setQueryHistory([...queryHistory, { query: cleanQuery, timestamp: new Date().toLocaleString() }]);
        const result = await fetchApi(cleanQuery);
        setLoading(false);
        if (result?.error) {
            setQueryResult({ error: result.message });
        } else {
            const parsedResult = result.map((item) => JSON.parse(item));
            setQueryResult(parsedResult.length ? <RenderTableForApi data={parsedResult} /> : "No data");
        }
    };

    const RenderTableForApi = ({ data }) => {
        const normalizedData = data.map((row) => ({
            ...row,
            allKeys: [...new Set(Object.keys(row))]
        }));
        const columns = Object.keys(normalizedData[0]).map((key) => ({
            accessorKey: key,
            header: key.charAt(0).toUpperCase() + key.slice(1),
        }));
        return <MaterialReactTable columns={columns} data={data} enableSorting enableColumnFilters initialState={{ density: 'compact' }} />;
    };

    const renderTable = () => {
        if (loading) return <div>Fetching data...</div>;

        if (showHistory) {
            return renderHistory();
        }

        if (!queryResult) return <div>No result available</div>;
        if (queryResult.error) return <div>Error: {queryResult.error}</div>;

        return queryResult;
    };

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

    const renderHistory = () => {
        if (queryHistory.length === 0) {
            return <div>No query history available.</div>;
        }

        const columns = [
            { accessorKey: 'timestamp', header: 'Timestamp' },
            { accessorKey: 'query', header: 'Query' },
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
    };

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
            console.log("SQL saved successfully:", response.message);
            setPopupMessage("The file has been successfully saved in the env/ligt-model folder.");
        }
    };

    const closePopup = () => setPopupMessage(null);


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
                                <button className="btn-primary" onClick={openQueryBook}>Query Book</button>
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
                                        />
                                    )
                            )}
                        </div>
                        <div className="btn-line">
                            <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <button className="btn-primary" onClick={saveSQL}>Save SQL</button>
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
                        <div className='result-box' style={{ '--position-offset': `${position - offset}px` }}>
                            {loading ? <div>Fetching data...</div> : renderTable()}
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
                    </div>
                )}
            </Resizable>
        </div>
    );
}

export default SqlEditor;
