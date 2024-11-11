import React, { useState } from 'react';
import Editor from './Editor'; // Assuming this is the path for your custom editor component
import './Popup.css';

const ActivatePopup = ({ onClose, onSubmit, table }) => {
    const [expression, setExpression] = useState(`ACTIVATE USL TABLE ${table.name} AS SELECT * FROM [Data Source Table];`);

    const handleSubmit = () => {
        onSubmit({ expression: expression });
        onClose();
    };

    return (
        <div className="popup-overlay">
            <div className="popup">
                <div className="popup-content">
                    <div className="popup-title">Activate Table</div>
                    <div className="popup-field">
                        {/* <label htmlFor="expressionEditor">Expression:</label> */}
                        <div style={{ height: '200px', width: '100%' }}>
                            <Editor
                                id="expressionEditor"
                                content={expression}
                                onChange={setExpression}
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
