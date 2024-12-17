import React, { useState, useEffect } from 'react';
import Editor from './Editor';
import './Popup.css';

const ActivatePopup = ({ onClose, onSubmit, table }) => {
    const [dataSource, setDataSource] = useState("");
    // const [dataSource, setDataSource] = useState(() => {
    //     if (!table.activateQuery) return "";
    
    //     try {
    //         const parsed = JSON.parse(table.activateQuery);
    //         return parsed.query || "";
    //     } catch (error) {
    //         // console.error("Invalid JSON format:", error);
    //         return table.activateQuery;
    //     }
    // });
    useEffect(() => {
        const savedTableData = JSON.parse(localStorage.getItem("savedTables")) || [];
        const matchedTable = savedTableData.find((t) => t.name === table.name);

        if (matchedTable && matchedTable.activateQuery) {
            const parsed = JSON.parse(matchedTable.activateQuery)
            setDataSource(parsed.query);
        } else if (table.activateQuery) {
            try {
                const parsed = JSON.parse(table.activateQuery);
                setDataSource(parsed.query || table.activateQuery);
            } catch (error) {
                setDataSource(table.activateQuery);
            }
        } else {
            setDataSource("");
        }
    }, []);
    
    

    const handleSubmit = () => {
        const expression = `ACTIVATE USL TABLE ${table.name} AS ${dataSource};`;

        onSubmit({ expression });
        onClose();
    };

    return (
        <div className="popup-overlay">
            <div className="popup">
                <div className="popup-content">
                    <div className="popup-title">Activate Table</div>
                    <div className="popup-field">
                        <div style={{ height: '500px', width: '100%' }}>
                            <Editor
                                id="dataSourceEditor"
                                content={dataSource}
                                onChange={setDataSource}
                            />
                        </div>
                    </div>
                    <div className="popup-buttons">
                        <button className="btn-secondary" onClick={onClose}>
                            Cancel
                        </button>
                        <button className="btn-primary" onClick={handleSubmit}>
                            Activate
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ActivatePopup;
