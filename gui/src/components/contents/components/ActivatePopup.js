import React, { useState, useEffect } from 'react';
import Editor from './Editor';
import './Popup.css';

const ActivatePopup = ({ onClose, onSubmit, table }) => {
    // Initialize dataSource with activateQuery if available, otherwise use a default template
    const [dataSource, setDataSource] = useState(() => {
        try {
            return table.activateQuery ? JSON.parse(table.activateQuery).query || "" : "";
        } catch (error) {
            console.error("Invalid JSON format:", error);
            return "";
        }
    });

    const handleSubmit = () => {
        const expression = `ACTIVATE USL TABLE ${table.name} AS ${dataSource};`;

        onSubmit({ expression });
        onClose();
    };

    return (
        <div className="popup-overlay">
            <div className="popup">
                <div className="popup-content">
                    <div className="popup-title">Activate Query</div>
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
