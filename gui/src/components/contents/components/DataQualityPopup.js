import React, { useState, useEffect } from 'react';
import Editor from './Editor'; // Assuming this is the path for your custom editor component
import './Popup.css';
import { fetchApi } from '../../../utils/common';

const DataQualityPopup = ({ onClose, table, setPopupMessage, updateUSLInfo }) => {
    const [ruleName, setRuleName] = useState('');  // State for rule name
    const [ruleExpression, setRuleExpression] = useState('');  // State for rule expression
    const [selectedTable, setSelectedTable] = useState('');
    const [savedTables, setSavedTables] = useState([]);

    useEffect(() => {
        const tables = JSON.parse(localStorage.getItem('savedTables')) || [];
        const activeTables = tables.filter(table => table.activateQuery);
        setSavedTables(activeTables);
        if (activeTables.length > 0) {
            setSelectedTable(activeTables[0].name);
        }
    }, []);

    const handleSubmit = async () => {
        if (!ruleName.trim()) {
            setPopupMessage('Rule Name cannot be empty. Please enter a valid name.');
            return;
        }

        if (!selectedTable) {
            setPopupMessage('Please select a table.');
            return;
        }

        const query = `REGISTER DQ ${ruleName} TABLE ${selectedTable} AS ${ruleExpression};`;

        try {
            const response = await fetchApi(query);
            console.log(response)

            if (response.error) {
                setPopupMessage(response.message);
            } else {
                updateUSLInfo();
                onClose();
            }
            setRuleName('');
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
                        <label htmlFor="ruleName">Select Table:</label>
                        <select id="tableSelect" value={selectedTable} onChange={(e) => setSelectedTable(e.target.value)}
                            style={{
                                width: '100%',
                                padding: '8px',
                                marginTop: '5px',
                                marginBottom: '15px',
                                borderRadius: '4px',
                                border: '1px solid #ccc',
                            }}
                        >
                            {savedTables.length > 0 ? (
                                savedTables.map((table, index) => (
                                    <option key={index} value={table.name}>
                                        {table.name.split('.').pop()}
                                    </option>
                                ))
                            ) : (
                                <option value="" disabled>
                                    No tables available
                                </option>
                            )}
                        </select>
                        
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
                        <div style={{ height: '300px', width: '100%' }}>
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
