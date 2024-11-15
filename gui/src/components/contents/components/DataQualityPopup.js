import React, { useState } from 'react';
import Editor from './Editor'; // Assuming this is the path for your custom editor component
import './Popup.css';
import { fetchApi } from '../../../utils/common';

const DataQualityPopup = ({ onClose, onSubmit, table, setPopupMessage }) => {
    const [ruleName, setRuleName] = useState('');  // State for rule name
    const [ruleExpression, setRuleExpression] = useState('');  // State for rule expression
    const [rules, setRules] = useState([]);
    const [isAddingNewRule, setIsAddingNewRule] = useState(false);

    const handleSubmit = () => {
        // Create full query with rule name and expression
        const fullExpression = `REGISTER DQ ${ruleName} TABLE ${table.name} AS ${ruleExpression};`;
        setRules([...rules, { name: ruleName, expression: fullExpression, isOpen: false }]);
        setRuleName('');  // Clear inputs after submission
        setRuleExpression('');
        setIsAddingNewRule(false);  // Close the rule addition form
    };

    const toggleRuleDetail = (index) => {
        setRules(rules.map((rule, i) => (
            i === index ? { ...rule, isOpen: !rule.isOpen } : rule
        )));
    };

    const handleAddNewRule = () => {
        setIsAddingNewRule(!isAddingNewRule);
    };

    const handleCancelAddNew = () => {
        setIsAddingNewRule(false);
        setRuleName('');  // Reset rule name and expression if addition is canceled
        setRuleExpression('');
    };

    const handleDeleteRule = (index) => {
        const updatedRules = [...rules];
        updatedRules.splice(index, 1);
        setRules(updatedRules);
    };

    // Handle register DQ request to the server
    const handleRegisterDQ = async () => {
        try {
            // Loop through the rules and send each one to the server using fetchApi
            for (const rule of rules) {
                const query = `${rule.expression}`;
                const response = await fetchApi(query);

                console.log(response)

                if (response.error) {
                    // Handle success response if needed
                    setPopupMessage(response.message);
                } else {
                    setPopupMessage('Data Quality rule registered successfully');
                }
            }
        } catch (error) {
            setPopupMessage(error);
        }
    };

    return (
        <div className="popup-overlay">
            <div className="popup">
                <div className="popup-content">
                    <div className="popup-title">Data Quality Check</div>
                    <table className="rules-table">
                        <thead>
                            <tr>
                                <th>Rule Name</th>
                            </tr>
                        </thead>
                        <tbody>
                            {rules.map((rule, index) => (
                                <React.Fragment key={index}>
                                    <tr>
                                        <td>{rule.name}</td>
                                        <td className="action-button">
                                            <button className="action-button" onClick={() => toggleRuleDetail(index)}>
                                                {rule.isOpen ? '▼' : '▶'}
                                            </button>
                                        </td>
                                    </tr>
                                    {rule.isOpen && (
                                        <tr>
                                            <td colSpan="2">
                                                <div className="rule-details">
                                                    {rule.expression}
                                                </div>
                                                <div className="popup-buttons">
                                                    <button className="btn-primary" onClick={() => handleDeleteRule(index)}>
                                                        Delete
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            ))}

                            {/* Add New Rule Button Row */}
                            <tr>
                                <td>
                                    <button className="btn-primary" onClick={handleAddNewRule}>
                                        {isAddingNewRule ? '− Cancel Add New Rule' : '+ Add New Rule'}
                                    </button>
                                </td>
                            </tr>

                            {/* Add New Rule Form */}
                            {isAddingNewRule && (
                                <tr>
                                    <td colSpan="2">
                                        <div>
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
                                                <div style={{ height: '100px', width: '100%' }}>
                                                    <Editor
                                                        id="ruleExpressionEditor"
                                                        content={ruleExpression}
                                                        onChange={setRuleExpression}
                                                    />
                                                </div>
                                            </div>
                                            <div className="popup-buttons">
                                                <button className="btn-primary" onClick={handleSubmit}>
                                                    Add
                                                </button>
                                                <button className="btn-secondary" onClick={handleCancelAddNew}>
                                                    Cancel
                                                </button>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>

                    <div className="popup-buttons">
                        <button className="btn-secondary" onClick={onClose}>
                            Close
                        </button>
                        <button className="btn-primary" onClick={handleRegisterDQ}>
                            Register DQ
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DataQualityPopup;