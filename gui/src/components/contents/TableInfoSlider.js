import React, { useState, useEffect } from 'react';
import { Drawer, List, ListItem, ListItemText, Divider, Collapse, Select, MenuItem, IconButton } from '@mui/material';
import { ExpandLess, ExpandMore, Add, Delete } from '@mui/icons-material';
import './TableInfoSlider.css';
import './../../styleguides/styleguides.css';

export const TableInfoSlider = ({ tableInfo, isOpen, onClose, onSaveChanges }) => {
    const [newDesc, setNewDesc] = useState(tableInfo?.desc || []);
    const [openColumns, setOpenColumns] = useState({});
    const [changedFields, setChangedFields] = useState({});
    const [newConstraint, setNewConstraint] = useState({});

    useEffect(() => {
        if (tableInfo?.desc) {
            const updatedDesc = tableInfo.desc.map(column => {
                return {
                    ...column,
                    data_type: removeQuotes(column.data_type)
                };
            });
            setNewDesc(updatedDesc);
        }
    }, [tableInfo]);

    // Handle data type change
    const handleDataTypeChange = (index, value) => {
        const updatedFields = { ...changedFields };
        if (!updatedFields[index]) {
            updatedFields[index] = {};
        }
        updatedFields[index].data_type = value;
        setChangedFields(updatedFields);
    };

    // Handle new constraint selection and update specific constraint fields (primaryKey, notNull, etc.)
    const handleNewConstraintChange = (index, value) => {
        console.log(value)
        const updatedDesc = [...newDesc];
        const column = updatedDesc[index];

        if (value === 'primaryKey') {
            column.primaryKey = { columns: [] };
        } else if (value === 'notNull') {
            column.notNull = { columns: [] };
        } else if (value === 'unique') {
            column.unique = { columns: [] };
        }

        setNewDesc(updatedDesc);
    };

    // Remove specific constraint field
    const removeConstraint = (columnIndex, constraintType) => {
        const updatedDesc = [...newDesc];
        const column = updatedDesc[columnIndex];

        if (constraintType === 'primaryKey') {
            delete column.primaryKey;
        } else if (constraintType === 'notNull') {
            delete column.notNull;
        } else if (constraintType === 'unique') {
            delete column.unique;
        }

        setNewDesc(updatedDesc);
    };

    // Save the changes and pass them to the parent component
    const handleSave = () => {
        const updatedTableInfo = { ...tableInfo };
        updatedTableInfo.desc = newDesc.map((column, index) => {
            if (changedFields[index]) {
                return { ...column, ...changedFields[index] };
            }
            return column;
        });

        requestAnimationFrame(() => {
            onSaveChanges(updatedTableInfo);
        })
        onClose();
    };

    const removeQuotes = (str) => {
        return str.replace(/['"]+/g, '');
    };

    if (!tableInfo) return null;

    return (
        <Drawer anchor="right" open={isOpen} onClose={onClose}>
            <div className="slider-container">
                <h2 className="slider-title">Table Info</h2>
                <h3 className="slider-subtitle">{tableInfo.name}</h3>

                <Divider />
                <h3 className="section-title">Columns</h3>
                <List>
                    {newDesc.map((column, index) => (
                        <div key={index}>
                            <ListItem onClick={() => setOpenColumns({ ...openColumns, [index]: !openColumns[index] })} className="list-item">
                                <ListItemText primary={`${column.col_name}`} primaryTypographyProps={{ className: 'column-name' }} />
                                {openColumns[index] ? <ExpandLess /> : <ExpandMore />}
                            </ListItem>

                            <Collapse in={openColumns[index]} timeout="auto" unmountOnExit>
                                <List component="div" disablePadding className="nested-list">
                                    <ListItem>
                                        <ListItemText primary="Data Type:" style={{ flex: '0 0 auto', marginRight: '16px' }} />
                                        <Select
                                            value={changedFields[index]?.data_type || column.data_type}
                                            onChange={(e) => handleDataTypeChange(index, e.target.value)}
                                            className="select-box"
                                            style={{ flex: '1' }}
                                        >
                                            <MenuItem value="varchar(100)">varchar(100)</MenuItem>
                                            <MenuItem value="integer">integer</MenuItem>
                                            <MenuItem value="boolean">boolean</MenuItem>
                                        </Select>
                                    </ListItem>

                                    {/* Display existing constraints */}
                                    {column.primaryKey && (
                                        <ListItem>
                                            <ListItemText primary="Primary Key" primaryTypographyProps={{ className: "constraint-list" }} />
                                            <IconButton onClick={() => removeConstraint(index, 'primaryKey')}>
                                                <Delete />
                                            </IconButton>
                                        </ListItem>
                                    )}
                                    {column.notNull && (
                                        <ListItem>
                                            <ListItemText primary="Not Null" primaryTypographyProps={{ className: "constraint-list" }} />
                                            <IconButton onClick={() => removeConstraint(index, 'notNull')}>
                                                <Delete />
                                            </IconButton>
                                        </ListItem>
                                    )}
                                    {column.unique && (
                                        <ListItem>
                                            <ListItemText primary="Unique" primaryTypographyProps={{ className: "constraint-list" }} />
                                            <IconButton onClick={() => removeConstraint(index, 'unique')}>
                                                <Delete />
                                            </IconButton>
                                        </ListItem>
                                    )}

                                    {/* Add new constraints */}
                                    <ListItem>
                                        <Select
                                            value={newConstraint[index] || ''}
                                            onChange={(e) => setNewConstraint({ ...newConstraint, [index]: e.target.value })}
                                            displayEmpty
                                            className="select-box"
                                        >
                                            <MenuItem value="" disabled>Select Constraint</MenuItem>
                                            <MenuItem value="primaryKey">Primary Key</MenuItem>
                                            <MenuItem value="notNull">Not Null</MenuItem>
                                            <MenuItem value="unique">Unique</MenuItem>
                                        </Select>
                                        <IconButton onClick={() => handleNewConstraintChange(index, newConstraint[index])}>
                                            <Add />
                                        </IconButton>
                                    </ListItem>
                                </List>
                            </Collapse>
                        </div>
                    ))}
                </List>

                {tableInfo.foreignKeyConstraints && tableInfo.foreignKeyConstraints.length > 0 && (
                    <>
                        <Divider />
                        <h3 className="section-title">Foreign Key Constraints</h3>
                        <List>
                            {tableInfo.foreignKeyConstraints.map((fk, index) => (
                                <ListItem key={index}>
                                    <ListItemText
                                        primary={`${fk.column}`}
                                        secondary={`References: ${fk.references}`}
                                        primaryTypographyProps={{ className: 'foreign-key-primary' }}
                                        secondaryTypographyProps={{ className: 'foreign-key-secondary' }}
                                    />
                                </ListItem>
                            ))}
                        </List>
                    </>
                )}

                {/* Save Changes Button */}
                <div className="save-btn-container">
                    <button onClick={handleSave} className="btn-primary">Save Changes</button>
                </div>
            </div>
        </Drawer>
    );
};
