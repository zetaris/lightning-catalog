import React, { useState } from 'react';
import Editor from './Editor'; // Assuming this is the path for your custom editor component
import './Popup.css';
import { fetchApi } from '../../../utils/common';

const DataQualityPopup = ({ onClose, table, setPopupMessage }) => {
    const [ruleName, setRuleName] = useState('');  // State for rule name
    const [ruleExpression, setRuleExpression] = useState('');  // State for rule expression

    const handleSubmit = async () => {
        const query = `REGISTER DQ ${ruleName} TABLE ${table.name} AS ${ruleExpression};`;

        try {
            const response = await fetchApi(query);

            if (response.error) {
                setPopupMessage(response.message);
            }else{
                onClose();
            }
            setRuleName('');  // Clear inputs after submission
            setRuleExpression('');
        } catch (error) {
            setPopupMessage(error.message || 'An error occurred while registering the rule');
        }
    };

    return (
        <div className="popup-overlay">
            <div className="popup">
                <div className="popup-content">
                    <div className="popup-title">Data Quality Check</div>

                    <div className="popup-field">
                        <label htmlFor="ruleName">Rule Name:</label>
                        <input
                            id="ruleName"
                            type="text"
                            value={ruleName}
                            onChange={(e) => setRuleName(e.target.value)}
                            placeholder="Enter rule name"
                        />
                    </div>

                    <div className="popup-field">
                        <label htmlFor="ruleExpression">Rule Expression:</label>
                        <div style={{ height: '400px', width: '100%' }}>
                            <Editor
                                id="ruleExpressionEditor"
                                content={ruleExpression}
                                onChange={setRuleExpression}
                            />
                        </div>
                    </div>

                    <div className="popup-buttons">
                        <button className="btn-secondary" onClick={onClose}>
                            Close
                        </button>
                        <button className="btn-primary" onClick={handleSubmit}>
                            Add
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DataQualityPopup;
