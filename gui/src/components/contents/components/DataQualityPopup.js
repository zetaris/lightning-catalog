import React, { useState } from 'react';
import Editor from './Editor'; // Assuming this is the path for your custom editor component
import './Popup.css';

const DataQualityPopup = ({ onClose, onSubmit }) => {
    const [name, setName] = useState('');
    const [expression, setExpression] = useState('');
    const [rules, setRules] = useState([
        { name: 'Rule 1', expression: 'SELECT * FROM table1', isOpen: false },
        { name: 'Rule 2', expression: 'SELECT COUNT(*) FROM table2', isOpen: false },
    ]);
    const [isAddingNewRule, setIsAddingNewRule] = useState(false);

    const handleSubmit = () => {
        // Add new rule to the list
        setRules([...rules, { name, expression, isOpen: false }]);
        setName('');
        setExpression('');
        setIsAddingNewRule(false); // Close the rule addition form
    };

    const toggleRuleDetail = (index) => {
        setRules(rules.map((rule, i) => (
            i === index ? { ...rule, isOpen: !rule.isOpen } : rule
        )));
    };

    const handleAddNewRule = () => {
        setIsAddingNewRule(!isAddingNewRule); // Toggle add new rule form
    };

    const handleCancelAddNew = () => {
        setIsAddingNewRule(false); // Close the add new rule form
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
                                                    <strong>Expression:</strong> {rule.expression}
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
                                                <label htmlFor="name">Name:</label>
                                                <input
                                                    id="name"
                                                    type="text"
                                                    value={name}
                                                    onChange={(e) => setName(e.target.value)}
                                                    placeholder="Enter rule name"
                                                />
                                            </div>
                                            <div className="popup-field">
                                                <label htmlFor="expressionEditor">Expression:</label>
                                                <div style={{ height: '100px', width: '100%' }}>
                                                    <Editor
                                                        id="expressionEditor"
                                                        content={expression}
                                                        onChange={setExpression}
                                                    />
                                                </div>
                                            </div>
                                            <div className="popup-buttons">
                                                <button className="btn-primary" onClick={handleSubmit}>
                                                    Add
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
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DataQualityPopup;
