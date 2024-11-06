import { jsPlumb } from 'jsplumb';
import { useEffect } from 'react';
import './JsPlumbTables.css';
import { createRoot } from 'react-dom/client';
import React from 'react';
import { ReactComponent as XmarkIcon } from '../../assets/images/xmark-solid.svg';
import { ReactComponent as PlayIcon } from '../../assets/images/play-solid.svg';
import { ReactComponent as EllipsisIcon } from '../../assets/images/ellipsis-vertical-solid.svg';
import { ReactComponent as PkIcon } from '../../assets/images/key-outline.svg';
import { ReactComponent as UniqueIcon } from '../../assets/images/fingerprint-solid.svg';
import { ReactComponent as IndexIcon } from '../../assets/images/book-solid.svg';
import { ReactComponent as CustomERDIcon } from '../../assets/images/customERD.svg';
import { ReactComponent as ZeroMoreLeftIcon } from '../../assets/images/zero_more_left.svg';
import { ReactComponent as ZeroMoreRightIcon } from '../../assets/images/zero_more_right.svg';
import { ReactComponent as ZeroOneLeftIcon } from '../../assets/images/zero_one_left.svg';
import { ReactComponent as ZeroOneRightIcon } from '../../assets/images/zero_one_right.svg';
import { ReactComponent as OneMoreLeftIcon } from '../../assets/images/one_more_left.svg';
import { ReactComponent as OneMoreRightIcon } from '../../assets/images/one_more_right.svg';
import { ReactComponent as OneOnlyOneLeftIcon } from '../../assets/images/one_only_one_left.svg';
import { ReactComponent as OneOnlyOneRightIcon } from '../../assets/images/zero_one_right.svg';
import { ReactComponent as OneLeftIcon } from '../../assets/images/one_left.svg';
import { ReactComponent as OneRightIcon } from '../../assets/images/one_right.svg';
import { ReactComponent as ManyLeftIcon } from '../../assets/images/many_left.svg';
import { ReactComponent as ManyRightIcon } from '../../assets/images/many_right.svg';
import { ReactComponent as ExpandingIcon } from '../../assets/images/up-right-and-down-left-from-center-solid.svg';
import { ReactComponent as CollapsingIcon } from '../../assets/images/down-left-and-up-right-to-center-solid.svg';
import { ReactComponent as LinkIcon } from '../../assets/images/link-solid.svg';


export const initializeJsPlumb = (container, tables = [], openModal, handleRowClickCallback, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleDeActivateTableClick, handleDataQualityButtonClick, handleTableDoubleClick, handleDirectConnection) => {
  const jsPlumbInstance = jsPlumb.getInstance({
    Container: container,
  });

  jsPlumbInstance.ready(() => {
    // Ensure tables is an array
    if (Array.isArray(tables)) {
      tables.forEach((table) => {
        setupTableForSelectedTable(container, table, jsPlumbInstance, table.id, false, handleRowClickCallback, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleDeActivateTableClick, handleDataQualityButtonClick, handleTableDoubleClick); // false to indicate it's an existing table
      });
    }

    jsPlumbInstance.bind('click', (connection, originalEvent) => {
      jsPlumbInstance.deleteConnection(connection);
    });

    // jsPlumbInstance.bind('beforeDrop', (info) => {
    //   openModal(info);
    //   return false;
    // });

    jsPlumbInstance.bind('beforeDrop', (info) => {
      connectEndpoints(
          jsPlumbInstance,
          info.sourceId,
          info.targetId,
          'fk',
          'many_to_many',
          true
      );
      addForeignKeyToConnection(jsPlumbInstance, info.sourceId, info.targetId, 'fk'); // Add foreign key icon
      return false;
    });

    jsPlumbInstance.bind('connectionDetached', (info) => {
      const { sourceId, targetId } = info.connection;
      console.log(sourceId, targetId)
      const targetColumnParent = document.getElementById(targetId).parentElement;
      removeForeignKeyIconFromColumn(targetColumnParent);
      removeConnectionFromLocalStorage(sourceId, targetId);

      console.log('Connection detached:', info);
    });

  });

  requestAnimationFrame(() => {
    const zoomLevel = parseFloat(localStorage.getItem('zoomLevel')) || 1;
    jsPlumbInstance.setZoom(zoomLevel);
  });
  jsPlumbInstance.repaintEverything();

  return jsPlumbInstance;
};

const saveConnectionToLocalStorage = (sourceId, targetId, relationship, relationship_type) => {
  const connections = JSON.parse(localStorage.getItem('connections')) || [];
  connections.push({ sourceId, targetId, relationship, relationship_type });
  localStorage.setItem('connections', JSON.stringify(connections));
};

const removeConnectionFromLocalStorage = (sourceId, targetId) => {
  let connections = JSON.parse(localStorage.getItem('connections')) || [];
  connections = connections.filter(conn => !(conn.sourceId === sourceId && conn.targetId === targetId));
  localStorage.setItem('connections', JSON.stringify(connections));
};

export function getRowInfo(sourceId, targetId) {
  const sourceTableId = sourceId.split('-col-')[0];
  const targetTableId = targetId.split('-col-')[0];
  const sourceColumnIndex = sourceId.match(/-col-(\d+)-/)[1];
  const targetColumnIndex = targetId.match(/-col-(\d+)-/)[1];

  const sourceTable = document.getElementById(sourceTableId);
  const targetTable = document.getElementById(targetTableId);

  const sourceColumn = sourceTable.querySelectorAll('tr')[sourceColumnIndex];
  const targetColumn = targetTable.querySelectorAll('tr')[targetColumnIndex];

  // Return as an object
  return { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn };
}

export function getColumnConstraint(fullPath) {
  // Find the table in the ddlText by the full path (e.g., lightning.datasource.rdbms.my_postgres.nytaxis.customer)
  const tablePath = fullPath.split('.').slice(0, -1).join('.');
  const columnName = fullPath.split('.').pop();
  const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];

  const table = savedTables.find(table => table.name === tablePath);

  if (!table) {
      console.error(`Table not found for path: ${tablePath}`);
      return null;
  }

  // Find the column within the table (use columnSpecs instead of columns)
  const column = table.desc.find(col => col.col_name === columnName);

  if (!column) {
      console.error(`Column not found for name: ${columnName}`);
      return null;
  }

  // Check for constraints inside the column itself (like primaryKey, notNull, etc.)
  const constraints = [];

  // Primary Key constraint
  if (column.primaryKey) {
      constraints.push({
          type: 'primaryKey',
          details: column.primaryKey
      });
  }

  // Not Null constraint
  if (column.notNull) {
      constraints.push({
          type: 'notNull',
          details: column.notNull
      });
  }

  // Foreign Key constraint (from table's foreignKeys array)
  if (table.foreignKeys && table.foreignKeys.length > 0) {
      const foreignKey = table.foreignKeys.find(fk => fk.columns.includes(columnName));
      if (foreignKey) {
          constraints.push({
              type: 'foreignKey',
              details: foreignKey
          });
      }
  }

  return constraints.length > 0 ? constraints : null; // Return all constraints related to the column
};

const removeForeignKeyIconFromColumn = (columnElement) => {
  console.log(columnElement);
  if (columnElement) {
    const fkIconContainer = columnElement.querySelector('.icon-container');
    const fkOnly = columnElement.querySelector('.fk-only');

    if (fkOnly) {
      if (fkIconContainer && fkIconContainer._root) {
        // Unmount the React root to remove the icon properly
        fkIconContainer._root.unmount();
        delete fkIconContainer._root; // Remove the reference to the root so it can be recreated later
      }
    }

    // Now, check for other constraint icons and generate tooltip text accordingly
    let tooltipText = '';

    // Check for primary key
    if (fkIconContainer.classList.contains('pk-icon')) {
      tooltipText += 'primary key';
    }

    // Check for unique constraint
    if (fkIconContainer.classList.contains('unique-icon')) {
      if (tooltipText) tooltipText += ', '; // Add a comma if there is already text
      tooltipText += 'unique';
    }

    // Check for index constraint
    if (fkIconContainer.classList.contains('index-icon')) {
      if (tooltipText) tooltipText += ', '; // Add a comma if there is already text
      tooltipText += 'index';
    }

    // If tooltipText is not empty, update the tooltip
    if (tooltipText) {
      const tooltipContainer = columnElement.querySelector('.tooltip-container');
      const tooltipTextElement = tooltipContainer?.querySelector('.tooltip-text');
      if (tooltipTextElement) {
        tooltipTextElement.innerText = tooltipText; // Update tooltip with the new text
      } else if (tooltipContainer) {
        // If tooltip element does not exist, create it
        const newTooltipText = document.createElement('span');
        newTooltipText.className = 'tooltip-text';
        newTooltipText.innerText = tooltipText;
        tooltipContainer.appendChild(newTooltipText);
      }
    }
  }
};

const addForeignKeyToConnection = (jsPlumbInstance, sourceId, targetId, relationship) => {
  const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);

  // Operation to change svg image in target column
  const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
  const targetColumnClass = targetColumn.children[0].classList[0];
  const constraints = getColumnConstraint(targetColumnClass);

  // Collect type, references, and source column info
  let tooltipData;
  if (constraints) {
    tooltipData = constraints.map((constraint) => {
      const reference = constraint.references ? `(${constraint.references})` : '';
      return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
    }).join(', ');
  }

  // Add source column info to the tooltip
  const combinedTooltipData = !tooltipData ? `foreign key: (${sourceColumnName})` : `${tooltipData}, foreign key: (${sourceColumnName})`;

  // Add the foreign key icon to the target column if the relationship is 'fk'
  if (relationship === 'fk') {
    addForeignKeyIconToColumn(targetColumn, combinedTooltipData, tooltipData);
  }
};

export function addForeignKeyIconToColumn(columnElement, combinedTooltipData, tooltipData){
  if (columnElement) {
      const iconContainer = columnElement.querySelector('.icon-container');

      if (iconContainer) {
          requestAnimationFrame(() => {
              // Create or reuse the root for rendering
              if (!iconContainer._root) {
                  iconContainer._root = createRoot(iconContainer);
              }

              if (!tooltipData) {
                  // Initial render of the icon and tooltip (only done once)
                  iconContainer._root.render(
                      <div className="tooltip-container">
                          <LinkIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />
                          <span className="tooltip-text">{combinedTooltipData}</span>
                          <div className="fk-only"></div>
                      </div>
                  );
              } else if (tooltipData === 'notNull') {
                  iconContainer._root.render(
                      <div className="tooltip-container">
                          <LinkIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />
                          <span className="tooltip-text">{combinedTooltipData}</span>
                          <div className="fk-only"></div>
                      </div>
                  );
              } else {
                  // Update only the tooltip text if the icon already exists
                  const tooltipElement = iconContainer.querySelector('.tooltip-text');
                  if (tooltipElement) {
                      tooltipElement.innerText = combinedTooltipData;
                  }
              }
          });
      }
  }
};


export const connectEndpoints = (jsPlumbInstance, sourceId, targetId, relationship, relationship_type = 'many_to_many', isSaveConnection = true) => {
  const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);

  const sourceColumnClass = sourceColumn.children[0].classList[0];
  const targetColumnClass = targetColumn.children[0].classList[0];
  const sourceConstraints = getColumnConstraint(sourceColumnClass);
  const targetConstraints = getColumnConstraint(targetColumnClass);

  // Check if source or target contains primaryKey constraint
  if (sourceConstraints && sourceConstraints.some(constraint => constraint.type === 'primaryKey')) {
    relationship_type = 'one_to_many';
  } else if (targetConstraints && targetConstraints.some(constraint => constraint.type === 'primaryKey')) {
    relationship_type = 'many_to_one';
  }

  const existingConnections = jsPlumbInstance.getAllConnections().filter(
    (conn) => conn.sourceId === sourceId && conn.targetId === targetId
  );

  if (existingConnections.length > 0) {
    return;
  }

  const sourceEndpoint = jsPlumbInstance.getEndpoints(sourceId)?.[0];
  const targetEndpoint = jsPlumbInstance.getEndpoints(targetId)?.[0];

  if (sourceEndpoint && targetEndpoint) {
    let sourceIconComponent, targetIconComponent;
    const leftIconStyle = { width: '100%', height: '100%', position: 'absolute', left: '-15px', top: '0px' };
    const rightIconStyle = { width: '100%', height: '100%', position: 'absolute', right: '-15px', top: '0px' };

    if (relationship_type === 'one_to_one') {
      sourceIconComponent = <OneRightIcon style={rightIconStyle} />;
      targetIconComponent = <OneLeftIcon style={leftIconStyle} />;
    } else if (relationship_type === 'one_to_many') {
      sourceIconComponent = <OneRightIcon style={rightIconStyle} />;
      targetIconComponent = <ManyLeftIcon style={leftIconStyle} />;
    } else if (relationship_type === 'many_to_one') {
      sourceIconComponent = <ManyRightIcon style={rightIconStyle} />;
      targetIconComponent = <OneLeftIcon style={leftIconStyle} />;
    } else if (relationship_type === 'many_to_many') {
      sourceIconComponent = <ManyRightIcon style={rightIconStyle} />;
      targetIconComponent = <ManyLeftIcon style={leftIconStyle} />;
    }

    const isSourceLeft = sourceId.endsWith('-left');
    const isTargetLeft = targetId.endsWith('-left');
    const sourceIcon = isSourceLeft ? sourceIconComponent : targetIconComponent;
    const targetIcon = isTargetLeft ? sourceIconComponent : targetIconComponent;

    const connection = jsPlumbInstance.connect({
      source: sourceEndpoint,
      target: targetEndpoint,
      anchors: ["Right", "Left"],
      overlays: [
        ["Label", { label: '', location: 0.5, cssClass: "connection-label" }],
        ["Custom", {
          create: () => {
            const erdConnector = document.createElement('div');
            erdConnector.style.width = '30px';
            erdConnector.style.height = '30px';
            erdConnector.style.position = 'relative';

            const root = createRoot(erdConnector);
            root.render(sourceIcon);
            return erdConnector;
          },
          location: 0,
          cssClass: "erd-connector"
        }],
        ["Custom", {
          create: () => {
            const erdConnector = document.createElement('div');
            erdConnector.style.width = '30px';
            erdConnector.style.height = '30px';
            erdConnector.style.position = 'relative';

            const root = createRoot(erdConnector);
            root.render(targetIcon);
            return erdConnector;
          },
          location: 1,
          cssClass: "erd-connector"
        }]
      ],
      connector: ["Flowchart", { stub: [30, 50], gap: 30, cornerRadius: 5 }],
      paintStyle: { stroke: '#5c96bc', strokeWidth: 2 },
    });

    // Use connection's main canvas for hover effects
    const canvas = connection.connector.canvas || connection.connector.element;

    if (canvas) {
      canvas.addEventListener("mouseenter", () => {
        connection.setPaintStyle({ stroke: 'red', strokeWidth: 2 });
        canvas.style.cursor = 'pointer';
        canvas.style.zIndex = '9';
      });

      canvas.addEventListener("mouseleave", () => {
        connection.setPaintStyle({ stroke: '#5c96bc', strokeWidth: 2 });
        canvas.style.cursor = 'default';
      });
    }

    if (isSaveConnection) {
      saveConnectionToLocalStorage(sourceId, targetId, relationship, relationship_type);
    }
  } else {
    console.error("Source or target endpoint not found.");
  }

  jsPlumbInstance.repaintEverything();
};

const addConstraintIconToColumn = (iconContainer, type, reference = '') => {
  if (iconContainer) {
    let IconComponent;
    let titleText;
    let iconClass;

    // Set the appropriate SVG icon, tooltip text, and class name based on the type
    switch (type) {
      case 'pk':
        IconComponent = <PkIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />;
        titleText = 'primary key';
        iconClass = 'pk-icon'; // Class name for primary key icon
        break;
      case 'unique':
        IconComponent = <UniqueIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />;
        titleText = 'unique';
        iconClass = 'unique-icon'; // Class name for unique icon
        break;
      case 'index':
        IconComponent = <IndexIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />;
        titleText = 'index';
        iconClass = 'index-icon'; // Class name for index icon
        break;
      default:
        return;
    }

    // Check if there's already a tooltip with `tooltip-text` class
    const existingTooltip = iconContainer.querySelector('.tooltip-text');
    let newTooltipText = titleText;

    if (existingTooltip) {
      // If tooltip exists, append the new title to the existing text
      newTooltipText = `${existingTooltip.innerText}, ${titleText}`;
    }

    // Create or update the tooltip container
    const tooltipContainer = document.createElement('div');
    tooltipContainer.className = 'tooltip-container';

    // Add the corresponding class for the icon type
    iconContainer.classList.add(iconClass); // Add the class to iconContainer

    // If the tooltip already exists, update its content, otherwise create a new one
    if (existingTooltip) {
      existingTooltip.innerText = newTooltipText;
    } else {
      const tooltipText = document.createElement('span');
      tooltipText.className = 'tooltip-text';
      tooltipText.innerText = newTooltipText;
      tooltipContainer.appendChild(tooltipText);

      // Create an icon element and append it to the tooltip container
      const iconElement = document.createElement('span');
      tooltipContainer.appendChild(iconElement);

      // Render the IconComponent inside the icon element
      const root = createRoot(iconElement);
      root.render(IconComponent);

      // Append the tooltip container to the icon container
      iconContainer.appendChild(tooltipContainer);
    }
  }
};

function calculateTablePositions(container) {
  const savedTables = localStorage.getItem('savedTables');
  let tableCount;

  // Calculate the number of tables
  if (savedTables) {
    const parsedTables = JSON.parse(savedTables);
    tableCount = parsedTables.length + 1; // Including the new table to be added
  } else {
    const tableContainers = container.querySelectorAll('.table-container');
    tableCount = tableContainers.length + 1; // Including the new table to be added
  }

  const positions = [];
  const gridSize = Math.ceil(Math.sqrt(tableCount));
  let currentY = 1500; // Initialize the Y position for the first row

  // Iterate through the tables and position them based on right and bottom offsets
  for (let i = 0; i < tableCount; i++) {
    const row = Math.floor(i / gridSize);
    const col = i % gridSize;

    let x = 1500;
    let y = currentY;

    // If there are previous tables in the row, calculate `x` based on the previous table's right position
    if (col > 0) {
      const previousTablePosition = positions[i - 1];
      x = previousTablePosition.x + 500; // Fixed horizontal offset for spacing
    }

    // If there are previous rows, calculate `y` based on the table directly above
    if (row > 0) {
      const aboveTablePosition = positions[i - gridSize];
      y = aboveTablePosition.y + 600; // Fixed vertical offset for spacing
    }

    positions.push({ x, y });
  }

  return positions;
}

export const setupTableForSelectedTable = (container, selectedTable, jsPlumbInstance, uuid, isNewTable, handleRowClickCallback, handlePreViewButtonClick, 
  handleTableInfoClick, handleActivateTableClick, handleDeActivateTableClick, handleDataQualityButtonClick, handleTableDoubleClick) => {

  const uniqueTableId = selectedTable.id || `table-${uuid}`;
  const tableName = selectedTable.name;

  // Check if the table is already rendered to avoid duplication
  if (!isNewTable && document.getElementById(uniqueTableId)) {
    return; // If the table is already added, do nothing
  }

  const tableContainer = document.createElement('div');
  tableContainer.className = 'table-container';
  tableContainer.style.position = 'absolute';
  tableContainer.id = uniqueTableId;

  const positions = calculateTablePositions(container);
  const { x, y } = positions[container.querySelectorAll('.table-container').length];

  tableContainer.style.left = `${x}px`;
  tableContainer.style.top = `${y}px`;

  const tableElement = document.createElement('table');
  tableElement.className = 'table-body';

  // Create table header (caption)
  const caption = document.createElement('div');
  caption.className = 'table-header'; 
  caption.ondblclick = (e) => {
    e.stopPropagation();
    handleTableDoubleClick();
  };

  const captionText = document.createElement('div');
  captionText.className = 'caption-text';
  captionText.innerText = selectedTable.name.split('.').pop();

  // Create a toggle button for expanding/collapsing table rows and endpoints
  const toggleButtonContainer = document.createElement('div');
  toggleButtonContainer.className = 'toggle-button-container';

  let isExpanded = true;

  // Create the React root once
  const iconRoot = createRoot(toggleButtonContainer);

  // Function to render the appropriate icon based on `isExpanded` state
  const renderToggleIcon = () => {
    const iconElement = isExpanded
      ? React.createElement(CollapsingIcon, { className: 'ellipsis-btn' })
      : React.createElement(ExpandingIcon, { className: 'ellipsis-btn' });
    
    iconRoot.render(iconElement);
  };

  // Initial render of the toggle icon
  renderToggleIcon();

  // Toggle table rows and related endpoints
  const toggleColumnList = () => {
    isExpanded = !isExpanded;
    const rows = tableElement.querySelectorAll('tr');

    rows.forEach((row, index) => {
      if (index !== 0) { // Skip the header row
        const leftEndpointId = row.querySelector('td:first-child')?.id;
        const rightEndpointId = row.querySelector('td:last-child')?.id;

        // Check if the row has connections on either endpoint
        const hasConnection = 
          (leftEndpointId && jsPlumbInstance.getConnections({ source: leftEndpointId }).length > 0) ||
          (rightEndpointId && jsPlumbInstance.getConnections({ target: rightEndpointId }).length > 0);

        // Only hide the row if it has no connections
        if (!hasConnection) {
          row.style.display = isExpanded ? '' : 'none';

          if (leftEndpointId) {
            const leftEndpoint = jsPlumbInstance.getEndpoints(leftEndpointId)?.[0];
            if (leftEndpoint) leftEndpoint.setVisible(isExpanded);
          }

          if (rightEndpointId) {
            const rightEndpoint = jsPlumbInstance.getEndpoints(rightEndpointId)?.[0];
            if (rightEndpoint) rightEndpoint.setVisible(isExpanded);
          }
        }
      }
    });

    jsPlumbInstance.repaintEverything();

    // Update the icon based on the expanded state
    renderToggleIcon();
  };

  // Attach the click event to toggleButtonContainer
  toggleButtonContainer.onclick = (e) => {
    e.stopPropagation();
    toggleColumnList();
  };

  // Append the toggle button container to the caption or desired parent element
  caption.appendChild(toggleButtonContainer);
  caption.appendChild(captionText);

  // Toggle popup menu visibility
  const togglePopupMenu = (event) => {
    const tableElement = document.getElementById(`table-${selectedTable.uuid}`);
    const isActive = tableElement && tableElement.classList.contains('activated-table');

    popupMenu.innerHTML = '';

    if (isActive) {
      const deActivateTableOption = document.createElement('div');
      deActivateTableOption.className = 'popup-menu-option';
      deActivateTableOption.innerText = 'Deactivate Table';
      deActivateTableOption.onclick = (e) => {
        e.stopPropagation();
        handleDeActivateTableClick(selectedTable);
        popupMenu.classList.add('hidden');
      };

      const previewOption = document.createElement('div');
      previewOption.className = 'popup-menu-option';
      previewOption.innerText = 'Preview Table';
      previewOption.onclick = (e) => {
        e.stopPropagation();
        handlePreViewButtonClick(tableName);
        popupMenu.classList.add('hidden');
      };

      const dataQualityOption = document.createElement('div');
      dataQualityOption.className = 'popup-menu-option';
      dataQualityOption.innerText = 'Data Quality Check';
      dataQualityOption.onclick = (e) => {
        e.stopPropagation();
        handleDataQualityButtonClick(tableName);
        popupMenu.classList.add('hidden');
      };

      const tableInfoOption = document.createElement('div');
      tableInfoOption.className = 'popup-menu-option';
      tableInfoOption.innerText = 'Table Info';
      tableInfoOption.onclick = (e) => {
        e.stopPropagation();
        handleTableInfoClick(selectedTable);
        popupMenu.classList.add('hidden');
      };

      const deleteOption = document.createElement('div');
      deleteOption.className = 'popup-menu-option';
      deleteOption.innerText = 'Delete Table';
      deleteOption.onclick = (e) => {
        e.stopPropagation();
        removeTable(uniqueTableId, jsPlumbInstance);
        popupMenu.classList.add('hidden');
      };

      popupMenu.appendChild(deActivateTableOption);
      popupMenu.appendChild(previewOption);
      popupMenu.appendChild(dataQualityOption);
      popupMenu.appendChild(tableInfoOption);
      popupMenu.appendChild(deleteOption);
    } else {
      const activateTableOption = document.createElement('div');
      activateTableOption.className = 'popup-menu-option';
      activateTableOption.innerText = 'Activate Table';
      activateTableOption.onclick = (e) => {
        e.stopPropagation();
        handleActivateTableClick(selectedTable);
        popupMenu.classList.add('hidden');
      };

      const deleteOption = document.createElement('div');
      deleteOption.className = 'popup-menu-option';
      deleteOption.innerText = 'Delete Table';
      deleteOption.onclick = (e) => {
        e.stopPropagation();
        removeTable(uniqueTableId, jsPlumbInstance);
        popupMenu.classList.add('hidden');
      };

      popupMenu.appendChild(activateTableOption);
      popupMenu.appendChild(deleteOption);
    }

    if (popupMenu.classList.contains('hidden')) {
      popupMenu.style.top = `${ellipsisButtonContainer.offsetTop + ellipsisButtonContainer.offsetHeight - 35}px`;
      popupMenu.style.left = `${ellipsisButtonContainer.offsetLeft + 35}px`;
      popupMenu.classList.remove('hidden');
      document.addEventListener('click', handleClickOutside, true);
    } else {
      popupMenu.classList.add('hidden');
      document.removeEventListener('click', handleClickOutside, true);
    }
  };

  // Handle clicks outside of the popup menu to close it
  const handleClickOutside = (event) => {
    if (!popupMenu.contains(event.target) && !ellipsisButtonContainer.contains(event.target)) {
      popupMenu.classList.add('hidden');
      document.removeEventListener('click', handleClickOutside, true);
    }
  };

  // Create ellipsis button container for menu
  const ellipsisButtonContainer = document.createElement('div');
  ellipsisButtonContainer.className = 'ellipsis-button-container';
  ellipsisButtonContainer.onclick = (e) => {
    e.stopPropagation();
    togglePopupMenu(e);
  };

  // Append the SVG Ellipsis icon using React component
  const ellipsisButtonReactElement = React.createElement(EllipsisIcon, {
    className: 'ellipsis-btn',
    style: { cursor: 'pointer' },
  });

  // Render the React element into the container
  const ellipsisButtonRoot = createRoot(ellipsisButtonContainer);
  ellipsisButtonRoot.render(ellipsisButtonReactElement);

  // Create popup menu for Delete Table and Preview Table options
  const popupMenu = document.createElement('div');
  popupMenu.className = 'popup-menu hidden'; 

  // Append ellipsis button container and popup menu to the caption
  caption.appendChild(captionText);
  caption.appendChild(ellipsisButtonContainer);
  caption.appendChild(popupMenu);

  // Append caption to table element
  tableElement.appendChild(caption);

  const tbody = document.createElement('tbody');
  tableElement.appendChild(tbody);

  const headerRow = document.createElement('tr');
  headerRow.className = 'table-row';

  // Create table cells for title
  const columnNameCell = document.createElement('td');
  columnNameCell.className = 'table-cell table-title';
  columnNameCell.innerText = 'column name';
  columnNameCell.style.paddingLeft = '40px';
  columnNameCell.style.textAlign = 'left';

  const dataTypeCell = document.createElement('td');
  dataTypeCell.className = 'table-cell table-title';
  dataTypeCell.innerText = 'data type';
  dataTypeCell.style.textAlign = 'left';

  headerRow.appendChild(columnNameCell);
  headerRow.appendChild(dataTypeCell);
  tbody.appendChild(headerRow);

  // Automatically add rows based on the selectedTable columns
  selectedTable.desc.forEach((column, index) => {
    const row = document.createElement('tr');
    row.className = 'table-row';

    const nameCell = document.createElement('td');
    nameCell.className = selectedTable.name + '.' + column.col_name + ' table-cell';
    nameCell.style.position = 'relative';
    nameCell.style.textAlign = 'left';

    // Add a container for the Foreign Key icon
    const iconContainer = document.createElement('span');
    iconContainer.className = 'icon-container';
    iconContainer.style.display = 'inline-block';
    iconContainer.style.position = 'absolute'; // Absolute positioning to align within the parent td
    iconContainer.style.left = '20px'; // Adjust the left value to position icon within td

    // Conditionally add icons based on constraints
    if (column.primaryKey) {
      addConstraintIconToColumn(iconContainer, 'pk'); // Add Primary Key icon
    }
    // if (column.notNull) {
    //   console.log(column)
    //   addConstraintIconToColumn(iconContainer, 'not-null'); // Add Not Null icon
    // }
    if (column.unique) {
      addConstraintIconToColumn(iconContainer, 'unique'); // Add Unique Key icon
    }
    if (column.index) {
      addConstraintIconToColumn(iconContainer, 'index'); // Add Index Key icon
    }

    // Append the icon container (which may be empty) to the name cell
    nameCell.appendChild(iconContainer);

    const columnNameText = document.createElement('span');
    columnNameText.innerText = column.col_name;
    columnNameText.style.paddingLeft = '30px';
    nameCell.appendChild(columnNameText);

    const typeCell = document.createElement('td');
    typeCell.className = 'table-cell'; 
    typeCell.style.textAlign = 'left';
    typeCell.innerText = column.data_type.replace(/"/g, '');

    row.appendChild(nameCell);
    row.appendChild(typeCell);
    tbody.appendChild(row);

    // Create endpoints for each cell (left for name, right for type)
    const leftEndpointId = `${uniqueTableId}-col-${index + 1}-left`;
    const rightEndpointId = `${uniqueTableId}-col-${index + 1}-right`;

    nameCell.id = leftEndpointId;
    typeCell.id = rightEndpointId;

    // Add endpoints to the left (name) and right (type) cells
    requestAnimationFrame(() => {
      if (!jsPlumbInstance.getEndpoints(leftEndpointId)?.length) {
        addEndpoint(jsPlumbInstance, leftEndpointId, 'LeftMiddle');
      }
      if (!jsPlumbInstance.getEndpoints(rightEndpointId)?.length) {
        addEndpoint(jsPlumbInstance, rightEndpointId, 'RightMiddle');
      }
    });
  });

  tableContainer.appendChild(tableElement);
  container.appendChild(tableContainer);

  // Set the table container as draggable
  requestAnimationFrame(() => {
    if (jsPlumbInstance && tableContainer) {
      makeTableDraggable(tableContainer, jsPlumbInstance, container);
    } else {
      console.error("jsPlumbInstance was not initialized or tableContainer is missing.");
    }
  });
};

// Function to remove a specific table
const removeTable = (tableId, jsPlumbInstance) => {
  // Remove the connections and endpoints related to this table
  if (jsPlumbInstance) {
    // Remove all connections related to the endpoints of this table
    const endpoints = jsPlumbInstance.getEndpoints(tableId);
    if (endpoints) {
      endpoints.forEach(endpoint => {
        jsPlumbInstance.deleteEndpoint(endpoint);
      });
    }

    // Remove the element from jsPlumb
    jsPlumbInstance.remove(tableId);
  }

  // Remove the element from the DOM
  const tableElement = document.getElementById(tableId);
  if (tableElement) {
    tableElement.remove();
  }

  // Assuming you want to remove the table and its related connections from localStorage
  let savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];
  const actualUuid = tableId.replace(/^table-/, '');
  savedTables = savedTables.filter(table => {
    return table.uuid !== actualUuid;
  });
  localStorage.setItem('savedTables', JSON.stringify(savedTables));
  localStorage.removeItem(tableId)

  let connections = JSON.parse(localStorage.getItem('connections')) || [];
  connections = connections.filter(conn => conn.sourceId !== tableId && conn.targetId !== tableId);
  localStorage.setItem('connections', JSON.stringify(connections));
};

// Make a table container draggable
const makeTableDraggable = (tableElement, jsPlumbInstance, container) => {
  try {
    if (tableElement) {
      jsPlumbInstance.draggable(tableElement.id, {
        containment: container, // Ensure that the table is dragged only within the container
        stop: (params) => {
          const newPosition = {
            top: params.pos[1],
            left: params.pos[0]
          };
          localStorage.setItem(tableElement.id, JSON.stringify(newPosition));
        }
      });
    } else {
      console.error("Table container not found for draggable operation.");
    }
  } catch (error) {
    console.error(`Failed to make table container draggable: ${error}`);
  }
};

const addEndpoint = (jsPlumbInstance, elementId, anchorPosition, config = {}) => {
  const endpoint = jsPlumbInstance.addEndpoint(elementId, {
    anchor: anchorPosition,
    isSource: true,
    isTarget: true,
    maxConnections: -1,
    endpoint: ['Dot', { radius: 6 }],
    paintStyle: { fill: 'gray', radius: 6 },
    hoverPaintStyle: { fill: 'red', radius: 8 },
    ...config,
  });

  const endpointCanvas = endpoint.canvas || endpoint.endpoint.canvas;
  if (endpointCanvas) {
    endpointCanvas.style.cursor = 'default';

    endpointCanvas.addEventListener('mouseenter', () => {
      endpoint.setPaintStyle({ fill: 'red', radius: 8 });
      endpointCanvas.style.cursor = 'pointer';
    });

    endpointCanvas.addEventListener('mouseleave', () => {
      endpoint.setPaintStyle({ fill: 'gray', radius: 6 });
      endpointCanvas.style.cursor = 'default';
    });
  }
};

export const handleOptimizeView = (container, zoomLevel, setZoomLevel, setOffset) => {
  if (container) {
      // Get all table-container elements inside the main container
      const tableContainers = Array.from(container.querySelectorAll('.table-container'));

      if (tableContainers.length === 0) {
          return; // No tables to optimize view for
      }

      // Find the bounding box of all table-containers
      let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;

      tableContainers.forEach(container => {
          const left = parseFloat(container.style.left);
          const top = parseFloat(container.style.top);
          const right = left + container.offsetWidth;
          const bottom = top + container.offsetHeight;

          minX = Math.min(minX, left);
          minY = Math.min(minY, top);
          maxX = Math.max(maxX, right);
          maxY = Math.max(maxY, bottom);
      });

      // console.log("minX : ", minX, "minY : ", minY, "maxX : ", maxX, "maxY : ", maxY, "actualContainerWidth : ", actualContainerWidth, "actualContainerHeight : ", actualContainerHeight)

      // Calculate required zoom level to fit all tables within the container
      const widthRatio = (600) / (maxX - minX);
      const heightRatio = (600) / (maxY - minY);
      const optimalZoomLevel = Math.min(widthRatio, heightRatio);

      // Apply the calculated zoom level
      const finalZoomLevel = Math.max(0.5, Math.min(optimalZoomLevel, 2)); // Limit zoom between 0.5 and 2
      setZoomLevel(finalZoomLevel);
      localStorage.setItem('zoomLevel', finalZoomLevel);

      // Calculate the offset to center the tables in the viewport
      const centerOffsetX = -(minX) * finalZoomLevel + 30;
      const centerOffsetY = -(minY) * finalZoomLevel;

      // Set the new zoom level and offset
      setOffset({ x: centerOffsetX, y: centerOffsetY });
      localStorage.setItem('offsetX', centerOffsetX);
      localStorage.setItem('offsetY', centerOffsetY);
  }
};

export const handleZoomIn = (container, setZoomLevel, setOffset, jsPlumbInstance) => {
  setZoomLevel((prevZoom) => {
      const newZoom = Math.min(prevZoom + 0.1, 2);
      jsPlumbInstance.setZoom(newZoom);
      adjustOffsetForZoom(container, newZoom / prevZoom, setOffset);
      localStorage.setItem('zoomLevel', newZoom);
      return newZoom;
  });
};

export const handleZoomOut = (container, setZoomLevel, setOffset, jsPlumbInstance) => {
  setZoomLevel((prevZoom) => {
      const newZoom = Math.max(prevZoom - 0.1, 0.1);
      jsPlumbInstance.setZoom(newZoom);
      adjustOffsetForZoom(container, newZoom / prevZoom, setOffset);
      localStorage.setItem('zoomLevel', newZoom);
      return newZoom;
  });
};

const adjustOffsetForZoom = (container, scaleFactor, setOffset) => {
  if (container) {
      const containerRect = container.getBoundingClientRect();

      // Calculate center of screen
      const centerX = (containerRect.left + containerRect.right) / 2;
      const centerY = (containerRect.top + containerRect.bottom) / 2;

      // Calculate offset
      setOffset((prevOffset) => {
          const newOffsetX = centerX - (centerX - prevOffset.x) * scaleFactor;
          const newOffsetY = centerY - (centerY - prevOffset.y) * scaleFactor;
          localStorage.setItem('offsetX', newOffsetX);
          localStorage.setItem('offsetY', newOffsetY);

          return { x: newOffsetX, y: newOffsetY };
      });
  }
};    