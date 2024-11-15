import React, { useState } from 'react';
import Editor from './Editor';
import './Popup.css';

const ActivatePopup = ({ onClose, onSubmit, table }) => {
    const [dataSource, setDataSource] = useState("--SELECT \n--CAST(id AS INT) AS [Column Name 1], \n--CAST(name AS STRING) AS [Column Name 2], \n--CAST(age AS STRING) AS [Column Name 3] \n--FROM [Table Name for the data source to connect];"); 

    const handleSubmit = () => {
        const expression = `ACTIVATE USL TABLE ${table.name} AS ${dataSource};`;
        console.log(expression)
        
        onSubmit({ expression });
        onClose();
    };

    return (
        <div className="popup-overlay">
            <div className="popup">
                <div className="popup-content">
                    <div className="popup-title">Activate Table</div>
                    <div className="popup-field">
                        {/* Use Editor for the Data Source Table */}
                        <label htmlFor="dataSourceEditor">Data Source Table:</label>
                        <div style={{ height: '200px', width: '100%' }}>
                            <Editor
                                id="dataSourceEditor"
                                content={dataSource}
                                onChange={setDataSource}
                                placeholder="Enter Data Source Table"
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
