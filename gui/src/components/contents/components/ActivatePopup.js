import React, { useState } from 'react';
import Editor from './Editor'; // Assuming this is the path for your custom editor component
import './Popup.css';

const ActivatePopup = ({ onClose, onSubmit, table }) => {
    const defaultExpression = `ACTIVATE USL TABLE ${table.name} QUERY SELECT * FROM ;`;
    const [expression, setExpression] = useState('');

    const handleSubmit = () => {
        onSubmit({ expression: expression || defaultExpression });
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
                                content={expression || defaultExpression}
                                onChange={setExpression} // Updating the expression when the editor changes
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
