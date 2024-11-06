import React, { useState } from 'react';
import './Modal.css'; // Modal styling

const RelationshipModal = ({ isOpen, onClose, onSubmit }) => {
    const [relationshipType, setRelationshipType] = useState('one_to_one'); // Default is one_to_one

    const handleSubmit = (event) => {
        event.preventDefault();

        // Set relationship to fk and use relationshipType for the type
        const relationship = 'fk'; // Always fk for foreign key relationships
        let type;
        switch (relationshipType) {
            case 'one_to_one':
                type = 'one_to_one';
                break;
            case 'one_to_many':
                type = 'one_to_many';
                break;
            case 'many_to_one':
                type = 'many_to_one';
                break;
            default:
                type = 'one_to_one';
        }

        onSubmit(relationship, type); // Pass both the fixed relationship and selected type
        onClose(); // Close the modal
    };

    if (!isOpen) return null; // Do not display if the modal is not open

    return (
        <div className="modal-overlay">
            <div className="popup">
                <h2 className="popup-title">Set Relationship Type</h2>
                <form onSubmit={handleSubmit}>
                    <div className="popup-content">
                        <div className="form-group">
                            <label className="relationship-label">
                                <input
                                    type="radio"
                                    name="relationshipType"
                                    value="one_to_one"
                                    checked={relationshipType === 'one_to_one'}
                                    onChange={(e) => setRelationshipType(e.target.value)}
                                />
                            </label>
                            <div>
                                <span className="relationship-label-text">1:1 (One-to-One)</span>
                                <div className="relationship-description">
                                    One-to-One relationship where one record from the source table is related to only one record in the target table.
                                </div>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="relationship-label">
                                <input
                                    type="radio"
                                    name="relationshipType"
                                    value="one_to_many"
                                    checked={relationshipType === 'one_to_many'}
                                    onChange={(e) => setRelationshipType(e.target.value)}
                                />
                            </label>
                            <div>
                                <span className="relationship-label-text">1:N (One-to-Many)</span>
                                <div className="relationship-description">
                                    One-to-Many relationship where one record in the source table can be related to multiple records in the target table.
                                </div>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="relationship-label">
                                <input
                                    type="radio"
                                    name="relationshipType"
                                    value="many_to_one"
                                    checked={relationshipType === 'many_to_one'}
                                    onChange={(e) => setRelationshipType(e.target.value)}
                                />
                            </label>
                            <div>
                                <span className="relationship-label-text">N:1 (many_to_one)</span>
                                <div className="relationship-description">
                                Many-to-One relationship where multiple records in the source table can be related to a single record in the target table.
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="popup-buttons">
                        <button className="btn-close" type="button" onClick={onClose}>Close</button>
                        <button className="btn-create" type="submit">Set Relationship</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default RelationshipModal;
