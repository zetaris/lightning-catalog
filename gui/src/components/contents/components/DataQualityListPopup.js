import React, { useEffect, useState } from 'react';
import Editor from './Editor';
import './Popup.css';
import { fetchApi } from '../../../utils/common';
import { ReactComponent as XmarkIcon } from '../../../assets/images/xmark-solid.svg';

const DataQualityListPopup = ({ onClose, table, setPopupMessage, updateUSLInfo }) => {
    const [filteredResults, setFilteredResults] = useState([]);
    const [showAddPopup, setShowAddPopup] = useState(false);
    const [ruleName, setRuleName] = useState('');
    const [ruleExpression, setRuleExpression] = useState('');

    useEffect(() => {
        const fetchData = async () => {
            const listDQQuery = `LIST DQ USL ${table.name.split('.').slice(0, -1).join('.')}`;
            const listDQResult = await fetchApi(listDQQuery);

            if (!listDQResult) {
                setPopupMessage("Failed to fetch Data Quality annotations.");
                return;
            }

            const parsedResults = listDQResult.map((item) => JSON.parse(item));
            const filtered = parsedResults.filter((dq) => dq.table === table.name.split('.').pop());

            filtered.sort((a, b) => (b.type === "Custom Data Quality") - (a.type === "Custom Data Quality"));
            setFilteredResults(filtered);
        };

        fetchData();
    }, [table.name, setPopupMessage]);

    const handleDelete = async (result) => {
        const runDQQuery = `REMOVE DQ ${result.name} TABLE ${table.name}`;

        try {
            const removeDQResult = await fetchApi(runDQQuery);
            const parsedResult = JSON.parse(removeDQResult);

            if (parsedResult.remove === true) {
                setFilteredResults((prevResults) => prevResults.filter((preResult) => preResult.name !== result.name));
            } else {
                setPopupMessage(parsedResult.message || "Unknown error");
            }
        } catch (error) {
            setPopupMessage(error.message || "Unknown error");
        }
    };

    const handleAddDQ = async () => {
        if (!ruleName.trim()) {
            setPopupMessage("Rule Name cannot be empty. Please enter a valid name.");
            return;
        }

        const query = `REGISTER DQ ${ruleName} TABLE ${table.name} AS ${ruleExpression};`;

        try {
            const response = await fetchApi(query);

            if (response.error) {
                setPopupMessage(response.message);
            } else {
                updateUSLInfo(response);
                setFilteredResults((prevResults) => [
                    { name: ruleName, type: "Custom Data Quality", expression: ruleExpression },
                    ...prevResults,
                ]);
                setShowAddPopup(false);
            }
        } catch (error) {
            setPopupMessage(error.message || "An error occurred while registering the rule");
        }
    };

    return (
        <div className="popup-overlay">
            <div className="popup">
                <h3 className="popup-title">Data Quality List</h3>
                <div className="popup-content">
                    {filteredResults.length > 0 ? (
                        <table className="rules-table">
                            <thead>
                                <tr>
                                    <th style={{ textAlign: 'center' }}>Name</th>
                                    <th style={{ textAlign: 'center' }}>Type</th>
                                    <th style={{ textAlign: 'center' }}>Expression</th>
                                    <th style={{ textAlign: 'center' }}>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredResults.map((result, index) => (
                                    <tr key={index}>
                                        <td>{result.name}</td>
                                        <td>{result.type}</td>
                                        <td>{result.expression || "N/A"}</td>
                                        <td style={{textAlign: 'center'}}>
                                            {result.type === "Custom Data Quality" && (
                                                <XmarkIcon
                                                    className="action-x-button"
                                                    onClick={() => handleDelete(result)}
                                                />
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                <tr>
                                    <td colSpan="5" style={{ textAlign: "center" }}>
                                        <button
                                            className="btn-primary"
                                            onClick={() => setShowAddPopup(true)}
                                        >
                                            Add DQ
                                        </button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    ) : (
                        <p>Loading data quality annotations...</p>
                    )}
                </div>
                <div className="popup-buttons">
                    <button className="btn-secondary" onClick={onClose}>
                        Close
                    </button>
                </div>
            </div>

            {showAddPopup && (
                <div className="popup-overlay">
                    <div className="popup">
                        <div className="popup-content">
                            <h3 className="popup-title">Add Custom Data Quality</h3>

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
                                <div style={{ height: "400px", width: "100%" }}>
                                    <Editor
                                        id="ruleExpressionEditor"
                                        content={ruleExpression}
                                        onChange={setRuleExpression}
                                    />
                                </div>
                            </div>

                            <div className="popup-buttons">
                                <button className="btn-secondary" onClick={() => setShowAddPopup(false)}>
                                    Cancel
                                </button>
                                <button className="btn-primary" onClick={handleAddDQ}>
                                    Add
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DataQualityListPopup;
