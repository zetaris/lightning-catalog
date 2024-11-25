import React from 'react';
import './PreviewPopup.css';
import { MaterialReactTable } from 'material-react-table';

const PreviewPopup = ({ open, onClose, columns, data, errorMessage }) => {
    if (!open) return null;

    const formattedColumns = columns.map(col => ({
        ...col,
        Cell: ({ cell }) => {
            const value = cell.getValue();
            if (typeof value === 'number') {
                return (
                    <div style={{ textAlign: 'right' }}>
                        {value.toLocaleString()} 
                    </div>
                );
            }
            return (
                <div style={{ textAlign: 'left' }}>
                    {value}
                </div>
            );
        }
    }));

    return (
        <div className="popup-overlay">
            <div className="popup-content preview-content">
                <div className="popup-header">
                    <h2>{errorMessage ? 'Error' : 'Table Preview'}</h2>
                    <button onClick={onClose} className="close-button">✕</button>
                </div>
                <div className="popup-body">
                    {errorMessage ? (
                        <p>{errorMessage}</p>
                    ) : (
                        <MaterialReactTable
                            columns={formattedColumns}
                            data={data}
                            enableSorting={true}
                            enableColumnFilters={true}
                            initialState={{ density: 'compact' }}
                        />
                    )}
                </div>
            </div>
        </div>
    );
};

export default PreviewPopup;
