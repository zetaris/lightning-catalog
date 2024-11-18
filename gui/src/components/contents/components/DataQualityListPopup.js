import React from 'react';
import './Popup.css';

const DataQualityListPopup = ({ onClose, table, setPopupMessage }) => {
    if (!table.dqAnnotations || table.dqAnnotations.length === 0) {
        setPopupMessage("No dqAnnotations available for this table.");
        return null;
    }

    return (
        <div className="popup-overlay">
            <div className="popup">
                <h3>Data Quality for {table.name}</h3>
                <ul>
                    {table.dqAnnotations.map((annotation, index) => (
                        <li key={index} className="dq-annotation-item">
                            <div className="dq-annotation-name">
                                <span className="dq-annotation-label">Name:</span>
                                <span className="dq-annotation-value">{annotation.name}</span>
                            </div>
                            <div className="dq-annotation-expression">
                                <span className="dq-annotation-label">Expression:</span>
                                <span className="dq-annotation-value">{annotation.expression}</span>
                            </div>
                        </li>
                    ))}
                </ul>
                <div className="popup-buttons">
                    <button className="btn-secondary" onClick={onClose}>
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DataQualityListPopup;