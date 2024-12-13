import { jsPlumb } from 'jsplumb';
import './JsPlumbTables.css';
import { createRoot } from 'react-dom/client';
import React from 'react';
import { ReactComponent as EllipsisIcon } from '../../assets/images/ellipsis-vertical-solid.svg';
import { ReactComponent as PkIcon } from '../../assets/images/key-outline.svg';
import { ReactComponent as UniqueIcon } from '../../assets/images/fingerprint-solid.svg';
import { ReactComponent as IndexIcon } from '../../assets/images/book-solid.svg';
import { ReactComponent as NNIcon } from '../../assets/images/notnull-icon.svg';
import { ReactComponent as OneLeftIcon } from '../../assets/images/one_left.svg';
import { ReactComponent as OneRightIcon } from '../../assets/images/one_right.svg';
import { ReactComponent as ManyLeftIcon } from '../../assets/images/many_left.svg';
import { ReactComponent as ManyRightIcon } from '../../assets/images/many_right.svg';
import { ReactComponent as ExpandingIcon } from '../../assets/images/up-right-and-down-left-from-center-solid.svg';
import { ReactComponent as CollapsingIcon } from '../../assets/images/down-left-and-up-right-to-center-solid.svg';
import { ReactComponent as LinkIcon } from '../../assets/images/link-solid.svg';


export const initializeJsPlumb = (container, tables = [], openModal, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleActivateQueryClick, handleDataQualityButtonClick, handleTableDoubleClick, handleListDQClick) => {
  const jsPlumbInstance = jsPlumb.getInstance({
    Container: container,
  });

  jsPlumbInstance.ready(() => {
    // Ensure tables is an array
    if (Array.isArray(tables)) {
      tables.forEach((table) => {
        setupTableForSelectedTable(container, table, jsPlumbInstance, table.id, false, handlePreViewButtonClick, handleTableInfoClick, handleActivateTableClick, handleActivateQueryClick, handleDataQualityButtonClick, handleTableDoubleClick, handleListDQClick); // false to indicate it's an existing table
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
      const sourceId = typeof info.sourceId === 'string' ? info.sourceId : info.sourceId.toString();
      const targetId = typeof info.targetId === 'string' ? info.targetId : info.targetId.toString();
      const optimalEndpoints = getOptimalEndpointPosition(sourceId, targetId);

      connectEndpoints(
        jsPlumbInstance,
        // info.sourceId,
        // info.targetId,
        optimalEndpoints.sourceId,
        optimalEndpoints.targetId,
        'fk',
        'many_to_many',
        true
      );
      // addForeignKeyToConnection(jsPlumbInstance, info.sourceId, info.targetId, 'fk');
      addForeignKeyToConnection(jsPlumbInstance, optimalEndpoints.sourceId, optimalEndpoints.targetId, 'fk');
      return false;
    });

    jsPlumbInstance.bind('connectionDetached', (info) => {
      const { sourceId, targetId } = info.connection;
      const sourceColumnParent = document.getElementById(sourceId).parentElement;
      removeForeignKeyIconFromColumn(sourceColumnParent, sourceId, targetId);
      removeConnectionFromLocalStorage(sourceId, targetId);

      // console.log('Connection detached:', info);
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

  const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];
  const sourceTableId = sourceId.split('-col-')[0].replace(/^table-/, '');
  const targetTableId = targetId.split('-col-')[0].replace(/^table-/, '');

  const sourceTable = savedTables.find((table) => table.uuid === sourceTableId);
  const targetTable = savedTables.find((table) => table.uuid === targetTableId);

  if (sourceTable && targetTable) {
    const sourceColumnIndex = sourceId.split('-col-')[1].split('-')[0];
    const targetColumnIndex = targetId.split('-col-')[1].split('-')[0];
    const sourceColumnName = sourceTable.desc[sourceColumnIndex - 1].col_name;
    const targetColumnName = targetTable.desc[targetColumnIndex - 1].col_name;;

    const sourceColumn = sourceTable.desc.find((col) => col.col_name === sourceColumnName);
    if (sourceColumn) {
      if (!sourceColumn.foreignKey) {
        sourceColumn.foreignKey = { columns: [], refTable: [], refColumns: [] };
      }

      sourceColumn.foreignKey.refTable.push(targetTable.name);
      sourceColumn.foreignKey.refColumns.push(targetColumnName);
    }

    const targetColumn = targetTable.desc.find((col) => col.col_name === targetColumnName);
    if (targetColumn) {
      if (!targetColumn.references) {
        targetColumn.references = [];
      }

      targetColumn.references.push({
        fromTable: sourceTable.name,
        fromColumn: sourceColumnName,
      });
    }

    localStorage.setItem('savedTables', JSON.stringify(savedTables));
  }
};

// const removeConnectionFromLocalStorage = (sourceId, targetId) => {
//   let connections = JSON.parse(localStorage.getItem('connections')) || [];
//   connections = connections.filter(conn => !(conn.sourceId === sourceId && conn.targetId === targetId));
//   localStorage.setItem('connections', JSON.stringify(connections));
// };

const removeConnectionFromLocalStorage = (sourceId, targetId) => {
  let connections = JSON.parse(localStorage.getItem('connections')) || [];
  connections = connections.filter((conn) => !(conn.sourceId === sourceId && conn.targetId === targetId));
  localStorage.setItem('connections', JSON.stringify(connections));

  const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];
  const sourceTableId = sourceId.split('-col-')[0].replace(/^table-/, '');
  const targetTableId = targetId.split('-col-')[0].replace(/^table-/, '');

  const sourceTable = savedTables.find((table) => table.uuid === sourceTableId);
  const targetTable = savedTables.find((table) => table.uuid === targetTableId);

  if (sourceTable && targetTable) {
    const sourceColumnIndex = sourceId.split('-col-')[1].split('-')[0];
    const targetColumnIndex = targetId.split('-col-')[1].split('-')[0];
    const sourceColumnName = sourceTable.desc[sourceColumnIndex - 1].col_name;
    const targetColumnName = targetTable.desc[targetColumnIndex - 1].col_name;

    const sourceColumn = sourceTable.desc.find((col) => col.col_name === sourceColumnName);
    if (sourceColumn && sourceColumn.foreignKey) {
      sourceColumn.foreignKey.refTable = sourceColumn.foreignKey.refTable.filter(
        (table) => table !== targetTable.name
      );
      sourceColumn.foreignKey.refColumns = sourceColumn.foreignKey.refColumns.filter(
        (column) => column !== targetColumnName
      );

      if (
        sourceColumn.foreignKey.refTable.length === 0 &&
        sourceColumn.foreignKey.refColumns.length === 0
      ) {
        delete sourceColumn.foreignKey;
      }
    }

    const targetColumn = targetTable.desc.find((col) => col.col_name === targetColumnName);
    if (targetColumn && targetColumn.references) {
      targetColumn.references = targetColumn.references.filter(
        (ref) => !(ref.fromTable === sourceTable.name && ref.fromColumn === sourceColumnName)
      );

      if (targetColumn.references.length === 0) {
        delete targetColumn.references;
      }
    }

    const sourceColumnElement = document.getElementById(`${sourceId}`);
    if (sourceColumnElement) {
      const iconContainer = sourceColumnElement.querySelector('.icon-container');
      if (iconContainer) {
        const tooltipElement = iconContainer.querySelector('.tooltip-text');
        if (tooltipElement) {
          const currentTooltipData = tooltipElement.textContent.split(', ').filter(Boolean);

          const updatedTooltipData = currentTooltipData.filter(
            (tooltip) => !tooltip.includes(`foreignKey(${targetColumnName})`)
          );

          if (updatedTooltipData.length > 0) {
            tooltipElement.innerText = updatedTooltipData.join(', ');
          } else {
            tooltipElement.remove();
            iconContainer.remove();
          }
        }
      }
    }

    localStorage.setItem('savedTables', JSON.stringify(savedTables));
  }
};

// export function getRowInfo(sourceId, targetId) {
//   const sourceTableId = sourceId.split('-col-')[0];
//   const targetTableId = targetId.split('-col-')[0];
//   const sourceColumnIndex = sourceId.match(/-col-(\d+)-/)[1];
//   const targetColumnIndex = targetId.match(/-col-(\d+)-/)[1];

//   const sourceTable = document.getElementById(sourceTableId);
//   const targetTable = document.getElementById(targetTableId);

//   const sourceColumn = sourceTable.querySelectorAll('tr')[sourceColumnIndex];
//   const targetColumn = targetTable.querySelectorAll('tr')[targetColumnIndex];

//   if (!sourceColumn || !targetColumn) {
//     console.error('Source or target column not found:', sourceId, targetId);
//     return {};
//   }

//   // Return as an object
//   return { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn };
// }

export function getRowInfo(sourceId, targetId) {
  try {
    const sourceTableId = sourceId.split('-col-')[0];
    const targetTableId = targetId.split('-col-')[0];

    const sourceColumnMatch = sourceId.match(/-col-(\d+)-/);
    const targetColumnMatch = targetId.match(/-col-(\d+)-/);

    if (!sourceColumnMatch || !targetColumnMatch) {
      // console.error('Invalid sourceId or targetId format:', sourceId, targetId);
      return {};
    }

    const sourceColumnIndex = parseInt(sourceColumnMatch[1], 10);
    const targetColumnIndex = parseInt(targetColumnMatch[1], 10);

    const sourceTable = document.getElementById(sourceTableId);
    const targetTable = document.getElementById(targetTableId);

    if (!sourceTable || !targetTable) {
      // console.error('Source or target table not found:', sourceTableId, targetTableId);
      return {};
    }

    const sourceColumns = sourceTable.querySelectorAll('tr');
    const targetColumns = targetTable.querySelectorAll('tr');

    if (
      sourceColumnIndex >= sourceColumns.length ||
      targetColumnIndex >= targetColumns.length
    ) {
      // console.error(
      //   'Invalid column index:',
      //   { sourceColumnIndex, targetColumnIndex },
      //   'Max indices:',
      //   { source: sourceColumns.length - 1, target: targetColumns.length - 1 }
      // );
      return {};
    }

    const sourceColumn = sourceColumns[sourceColumnIndex];
    const targetColumn = targetColumns[targetColumnIndex];

    if (!sourceColumn || !targetColumn) {
      // console.error('Source or target column not found:', sourceId, targetId);
      return {};
    }

    // Return as an object
    return { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn };
  } catch (error) {
    // console.error('Error in getRowInfo:', error);
    return {};
  }
}


export function getColumnConstraint(fullPath) {
  const tablePath = fullPath.split('.').slice(0, -1).join('.');
  const columnName = fullPath.split('.').pop();
  const savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];

  const table = savedTables.find((table) => table.name === tablePath);

  if (!table) {
    console.error(`Table not found for path: ${tablePath}`);
    return null;
  }

  const column = table.desc.find((col) => col.col_name === columnName);

  if (!column) {
    console.error(`Column not found for name: ${columnName}`);
    return null;
  }

  const constraints = [];

  // Check for primaryKey constraint at column level
  if (column.primaryKey) {
    constraints.push({
      type: 'primaryKey',
      details: column.primaryKey,
    });
  }

  // Check for notNull constraint at column level
  if (column.notNull) {
    constraints.push({
      type: 'not null',
      details: column.notNull,
    });
  }

  // Check for foreignKey at table level (legacy handling)
  if (table.foreignKeys && table.foreignKeys.length > 0) {
    const foreignKey = table.foreignKeys.find((fk) => fk.columns.includes(columnName));
    if (foreignKey) {
      constraints.push({
        type: 'foreignKey',
        details: foreignKey,
      });
    }
  }

  // Check for foreignKey at column level
  if (column.foreignKey) {
    const { refTable, refColumns } = column.foreignKey;

    refColumns.forEach((targetColumnName, index) => {
      const targetTable = refTable[index] || refTable[0]; // Handle multiple references
      constraints.push({
        type: `foreignKey(${targetColumnName})`, // Add fk(targetColumnName)
        details: {
          refTable: targetTable,
          refColumns: targetColumnName,
        },
      });
    });
  }

  return constraints.length > 0 ? constraints : null; // Return all applicable constraints
}

const removeForeignKeyIconFromColumn = (columnElement, sourceId, targetId) => {
  if (columnElement) {
    const fkIconContainer = columnElement.querySelector('.icon-container');
    const tooltipContainer = columnElement.querySelector('.tooltip-container');
    const tooltipTextElement = tooltipContainer?.querySelector('.tooltip-text');

    const targetName = document.getElementById(targetId).innerText.trim();

    const fkToRemove = `foreignKey(${targetName})`.trim().replace(/\s+/g, ' ');

    let updatedTooltipText = '';

    if (tooltipTextElement) {
      const currentTooltipText = tooltipTextElement.textContent.trim().replace(/\s+/g, ' ');
      updatedTooltipText = currentTooltipText
        .split(', ')
        .filter((fk) =>
          fk !== fkToRemove)
        .join(', ');

      if (updatedTooltipText) {
        tooltipTextElement.innerText = updatedTooltipText;
      } else {
        tooltipTextElement.innerText = '';
      }
    }

    columnElement.setAttribute('data-tooltip', updatedTooltipText);

    const fkOnly = columnElement.querySelector('.fk-only');
    if (fkOnly) {
      const remainingTooltipText = tooltipTextElement?.textContent.trim() || '';
      const hasOtherForeignKeys = remainingTooltipText.toLowerCase().includes('foreignkey');

      if (!tooltipTextElement || (!remainingTooltipText && !hasOtherForeignKeys)) {
        if (fkIconContainer && fkIconContainer._root) {
          fkIconContainer._root.unmount();
          delete fkIconContainer._root;
        }
      }
    }

    let tooltipText = '';

    // Check for primary key
    if (fkIconContainer && fkIconContainer.classList.contains('pk-icon')) {
      tooltipText += 'primary key';
    }

    // Check for unique constraint
    if (fkIconContainer && fkIconContainer.classList.contains('unique-icon')) {
      if (tooltipText) tooltipText += ', '; // Add a comma if there is already text
      tooltipText += 'unique';
    }

    // Check for index constraint
    if (fkIconContainer && fkIconContainer.classList.contains('index-icon')) {
      if (tooltipText) tooltipText += ', '; // Add a comma if there is already text
      tooltipText += 'index';
    }

    // If tooltipText is not empty, update the tooltip
    if (tooltipText) {
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

  const sourceColumnName = sourceColumn.querySelector('td')?.innerText || '';
  const targetColumnName = targetColumn.querySelector('td')?.innerText || '';
  const sourceColumnClass = sourceColumn.children[0].classList[0];
  const constraints = getColumnConstraint(sourceColumnClass);

  let tooltipData;
  if (constraints) {
    tooltipData = constraints.map((constraint) => {
      const reference = constraint.references ? `(${constraint.references})` : '';
      return reference ? `${constraint.type}: ${reference}` : `${constraint.type}`;
    }).join(', ');
  }

  const targetForeignKeyText = `foreignKey(${targetColumnName})`;

  const newTooltipData = tooltipData
    ? `${tooltipData}, ${targetForeignKeyText}`
    : targetForeignKeyText;

  if (relationship === 'fk') {
    const existingTooltipData = sourceColumn.getAttribute('data-tooltip') || '';

    // Split existing tooltip data into an array and remove duplicates
    const combinedTooltipArray = [
      ...new Set([...existingTooltipData.split(', '), ...newTooltipData.split(', ')].filter(Boolean)),
    ];

    // Rejoin the array into a string
    const combinedTooltipData = combinedTooltipArray.join(', ');

    sourceColumn.setAttribute('data-tooltip', combinedTooltipData);

    addForeignKeyIconToColumn(sourceColumn, combinedTooltipData, tooltipData);
  }
};

export function addForeignKeyIconToColumn(columnElement, combinedTooltipData, tooltipData) {
  if (columnElement) {
    let iconContainer = columnElement.querySelector('.icon-container');
    const td = columnElement.querySelector('td');

    if (!iconContainer) {
      iconContainer = document.createElement('span');
      iconContainer.className = 'icon-container fk-icon';
      iconContainer.style.display = 'inline-block';
      iconContainer.style.position = 'absolute';
      iconContainer.style.left = '20px';
      td.appendChild(iconContainer);
    }

    if (iconContainer) {
      requestAnimationFrame(() => {
        // Create or reuse the root for rendering
        if (!iconContainer._root) {
          iconContainer._root = createRoot(iconContainer);
        }

        if (!combinedTooltipData) {
          // Initial render of the icon and tooltip (only done once)
          iconContainer._root.render(
            <div className="tooltip-container">
              <LinkIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />
              <span className="tooltip-text">{combinedTooltipData}</span>
              <div className="fk-only"></div>
            </div>
          );
        } else if (combinedTooltipData.includes('not null')) {
          iconContainer._root.render(
            <div className="tooltip-container">
              <NNIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />
              <span className="tooltip-text">{combinedTooltipData}</span>
              <div className="fk-only"></div>
            </div>
          );
        } else {
          const tooltipElement = iconContainer.querySelector('.tooltip-text');
          if (tooltipElement) {
            // Update the existing tooltip text
            tooltipElement.innerText = combinedTooltipData;
          } else {
            // Tooltip text doesn't exist, create and append it to the existing container
            const tooltipText = document.createElement('span');
            tooltipText.className = 'tooltip-text';
            tooltipText.innerText = combinedTooltipData;

            // Append the new tooltip text to the existing iconContainer
            const tooltipContainer = iconContainer.querySelector('.tooltip-container');
            if (tooltipContainer) {
              tooltipContainer.appendChild(tooltipText);
            } else {
              // Fallback: Render the full structure if tooltip-container is missing
              iconContainer._root.render(
                <div className="tooltip-container">
                  <LinkIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />
                  <span className="tooltip-text">{combinedTooltipData}</span>
                  <div className="fk-only"></div>
                </div>
              );
            }
          }
        }
      });
    }
  }
};

export const connectEndpoints = (jsPlumbInstance, sourceId, targetId, relationship, relationship_type = 'many_to_many', isSaveConnection = true) => {
  const { sourceColumnIndex, targetColumnIndex, sourceColumn, targetColumn } = getRowInfo(sourceId, targetId);

  if (!sourceColumn || !sourceColumn.children || sourceColumn.children.length === 0) {
    // console.error("Source column children not found or invalid:", sourceColumn);
    return;
  }

  if (!targetColumn || !targetColumn.children || targetColumn.children.length === 0) {
    // console.error("Target column children not found or invalid:", targetColumn);
    return;
  }

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

    const sourceIsRight = sourceEndpoint.elementId.endsWith('-right');
    const targetIsLeft = targetEndpoint.elementId.endsWith('-left');

    if (relationship_type === 'one_to_one') {
      sourceIconComponent = !sourceIsRight
        ? <OneRightIcon style={rightIconStyle} />
        : <OneLeftIcon style={leftIconStyle} />;
      targetIconComponent = !targetIsLeft
        ? <OneLeftIcon style={leftIconStyle} />
        : <OneRightIcon style={rightIconStyle} />;
    } else if (relationship_type === 'one_to_many') {
      sourceIconComponent = !sourceIsRight
        ? <OneRightIcon style={rightIconStyle} />
        : <OneLeftIcon style={leftIconStyle} />;
      targetIconComponent = !targetIsLeft
        ? <ManyLeftIcon style={leftIconStyle} />
        : <ManyRightIcon style={rightIconStyle} />;
    } else if (relationship_type === 'many_to_one') {
      sourceIconComponent = !sourceIsRight
        ? <ManyRightIcon style={rightIconStyle} />
        : <ManyLeftIcon style={leftIconStyle} />;
      targetIconComponent = !targetIsLeft
        ? <OneLeftIcon style={leftIconStyle} />
        : <OneRightIcon style={rightIconStyle} />;
    } else if (relationship_type === 'many_to_many') {
      sourceIconComponent = !sourceIsRight
        ? <ManyRightIcon style={rightIconStyle} />
        : <ManyLeftIcon style={leftIconStyle} />;
      targetIconComponent = !targetIsLeft
        ? <ManyLeftIcon style={leftIconStyle} />
        : <ManyRightIcon style={rightIconStyle} />;
    }

    const sourceIcon = sourceIconComponent;
    const targetIcon = targetIconComponent;

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
      paintStyle: { stroke: '#ccc', strokeWidth: 1 },
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
        connection.setPaintStyle({ stroke: '#ccc', strokeWidth: 1 });
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
        iconClass = 'pk-icon';
        break;
      case 'not-null':
        IconComponent = <NNIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />;
        titleText = 'not null';
        iconClass = 'notnull-icon';
        break;
      case 'unique':
        IconComponent = <UniqueIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />;
        titleText = 'unique';
        iconClass = 'unique-icon';
        break;
      case 'index':
        IconComponent = <IndexIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />;
        titleText = 'index';
        iconClass = 'index-icon';
        break;
      case 'fk': // New case for foreign keys
        IconComponent = <LinkIcon style={{ height: '15px', width: '15px', fill: 'gray', cursor: 'pointer' }} />;
        titleText = ``;
        iconClass = 'fk-icon';
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

    // Calculate x position based on the previous table in the row, if available
    if (col > 0 && positions[i - 1]) {
      const previousTablePosition = positions[i - 1];
      x = previousTablePosition.x + 500; // Fixed horizontal offset for spacing
    }

    // Calculate y position based on the table directly above, if available
    if (row > 0 && positions[i - gridSize]) {
      const aboveTablePosition = positions[i - gridSize];
      const aboveTableContainer = container.querySelectorAll('.table-container')[i - gridSize];
      const dynamicOffsetY = aboveTableContainer ? aboveTableContainer.offsetHeight + 100 : 600;
      y = aboveTablePosition.y + dynamicOffsetY; // Dynamic vertical offset based on actual table height
    }

    positions.push({ x, y });
  }

  return positions;
}

export const setupTableForSelectedTable = (container, selectedTable, jsPlumbInstance, uuid, isNewTable, handlePreViewButtonClick,
  handleTableInfoClick, handleActivateTableClick, handleActivateQueryClick, handleDataQualityButtonClick, handleTableDoubleClick, handleListDQClick) => {

  const uniqueTableId = selectedTable.id || `table-${uuid}`;
  const tableName = selectedTable.name;

  // Check if the table is already rendered to avoid duplication
  if (!isNewTable && document.getElementById(uniqueTableId)) {
    return; // If the table is already added, do nothing
  }

  const tableContainer = document.createElement('div');
  tableContainer.className = 'table-container ' + tableName;
  tableContainer.style.position = 'absolute';
  tableContainer.id = uniqueTableId;

  const positions = calculateTablePositions(container);
  const existingTableContainers = Array.from(container.querySelectorAll('.table-container'));

  const uniqueTableNames = new Set(
    existingTableContainers.map((table) => table.className.split(' ')[1])
  );

  const uniqueCount = uniqueTableNames.size;

  const { x, y } = positions[uniqueCount] || { x: 0, y: 0 };
  // const { x, y } = positions[container.querySelectorAll('.table-container').length];

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
      if (index !== 0) {
        const leftEndpointId = row.querySelector('td:first-child')?.id;
        const rightEndpointId = row.querySelector('td:last-child')?.id;

        const leftConnections = leftEndpointId
          ? jsPlumbInstance.getConnections({ source: leftEndpointId }).concat(jsPlumbInstance.getConnections({ target: leftEndpointId }))
          : [];
        const rightConnections = rightEndpointId
          ? jsPlumbInstance.getConnections({ source: rightEndpointId }).concat(jsPlumbInstance.getConnections({ target: rightEndpointId }))
          : [];
        const hasConnection = leftConnections.length > 0 || rightConnections.length > 0;


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
      const activateQueryOption = document.createElement('div');
      activateQueryOption.className = 'popup-menu-option';
      activateQueryOption.innerText = 'Activate Table';
      activateQueryOption.onclick = (e) => {
        e.stopPropagation();
        handleActivateQueryClick(selectedTable);
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

      const listDataQualityOption = document.createElement('div');
      listDataQualityOption.className = 'popup-menu-option';
      listDataQualityOption.innerText = 'Data Quality List';
      listDataQualityOption.onclick = (e) => {
        e.stopPropagation();
        handleListDQClick(selectedTable);
        popupMenu.classList.add('hidden');
      };

      // const dataQualityOption = document.createElement('div');
      // dataQualityOption.className = 'popup-menu-option';
      // dataQualityOption.innerText = 'Register Data Quality';
      // dataQualityOption.onclick = (e) => {
      //   e.stopPropagation();
      //   handleDataQualityButtonClick(selectedTable);
      //   popupMenu.classList.add('hidden');
      // };

      // const tableInfoOption = document.createElement('div');
      // tableInfoOption.className = 'popup-menu-option';
      // tableInfoOption.innerText = 'Table Info';
      // tableInfoOption.onclick = (e) => {
      //   e.stopPropagation();
      //   handleTableInfoClick(selectedTable);
      //   popupMenu.classList.add('hidden');
      // };

      const deleteOption = document.createElement('div');
      deleteOption.className = 'popup-menu-option';
      deleteOption.innerText = 'Delete Table';
      deleteOption.onclick = (e) => {
        e.stopPropagation();
        removeTable(uniqueTableId, jsPlumbInstance);
        popupMenu.classList.add('hidden');
      };

      popupMenu.appendChild(activateQueryOption);
      popupMenu.appendChild(previewOption);
      popupMenu.appendChild(listDataQualityOption);
      // popupMenu.appendChild(dataQualityOption);
      // popupMenu.appendChild(tableInfoOption);
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
    if (column.notNull) {
      addConstraintIconToColumn(iconContainer, 'not-null'); // Add Not Null icon
    }
    if (column.unique) {
      addConstraintIconToColumn(iconContainer, 'unique'); // Add Unique Key icon
    }
    if (column.index) {
      addConstraintIconToColumn(iconContainer, 'index'); // Add Index Key icon
    }
    if (column.foreignKey) {
      addConstraintIconToColumn(iconContainer, 'fk'); // Add Index Key icon
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
// const removeTable = (tableId, jsPlumbInstance) => {
//   // Remove the connections and endpoints related to this table
//   if (jsPlumbInstance) {
//     // Remove all connections related to the endpoints of this table
//     const endpoints = jsPlumbInstance.getEndpoints(tableId);
//     if (endpoints) {
//       endpoints.forEach(endpoint => {
//         jsPlumbInstance.deleteEndpoint(endpoint);
//       });
//     }

//     // Remove the element from jsPlumb
//     jsPlumbInstance.remove(tableId);
//   }

//   // Remove the element from the DOM
//   const tableElement = document.getElementById(tableId);
//   if (tableElement) {
//     tableElement.remove();
//   }

//   // Assuming you want to remove the table and its related connections from localStorage
//   let savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];
//   const actualUuid = tableId.replace(/^table-/, '');
//   savedTables = savedTables.filter(table => {
//     return table.uuid !== actualUuid;
//   });
//   localStorage.setItem('savedTables', JSON.stringify(savedTables));
//   localStorage.removeItem(tableId)

//   let connections = JSON.parse(localStorage.getItem('connections')) || [];
//   connections = connections.filter(conn => conn.sourceId !== tableId && conn.targetId !== tableId);
//   localStorage.setItem('connections', JSON.stringify(connections));
// };

const removeTable = async (tableId, jsPlumbInstance) => {
  const confirmed = await showConfirmationPopup("Are you sure you want to delete this table?");

  if (!confirmed) {
    return; // Exit if user canceled
  }

  // Remove the connections and endpoints related to this table
  if (jsPlumbInstance) {
    const endpoints = jsPlumbInstance.getEndpoints(tableId);
    if (endpoints) {
      endpoints.forEach(endpoint => {
        jsPlumbInstance.deleteEndpoint(endpoint);
      });
    }
    jsPlumbInstance.remove(tableId);
  }

  // Remove the element from the DOM
  const tableElement = document.getElementById(tableId);
  if (tableElement) {
    tableElement.remove();
  }

  // Remove table and its connections from localStorage
  let savedTables = JSON.parse(localStorage.getItem('savedTables')) || [];
  const actualUuid = tableId.replace(/^table-/, '');
  savedTables = savedTables.filter(table => table.uuid !== actualUuid);
  localStorage.setItem('savedTables', JSON.stringify(savedTables));
  localStorage.removeItem(tableId);

  let connections = JSON.parse(localStorage.getItem('connections')) || [];
  connections = connections.filter(conn => conn.sourceId !== tableId && conn.targetId !== tableId);
  localStorage.setItem('connections', JSON.stringify(connections));
};

const getDistance = (point1, point2) => {
  return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
};

export const getOptimalEndpointPosition = (sourceId, targetId) => {
  if (typeof sourceId !== 'string' || typeof targetId !== 'string') {
    console.error("Invalid sourceId or targetId. Expected strings but got:", { sourceId, targetId });
    return { sourceId, targetId };
  }

  const sourceLeftId = sourceId.includes('-left') ? sourceId : sourceId.replace(/-right$/, '-left');
  const sourceRightId = sourceId.includes('-right') ? sourceId : sourceId.replace(/-left$/, '-right');
  const targetLeftId = targetId.includes('-left') ? targetId : targetId.replace(/-right$/, '-left');
  const targetRightId = targetId.includes('-right') ? targetId : targetId.replace(/-left$/, '-right');

  const sourceLeftElement = document.getElementById(sourceLeftId);
  const sourceRightElement = document.getElementById(sourceRightId);
  const targetLeftElement = document.getElementById(targetLeftId);
  const targetRightElement = document.getElementById(targetRightId);

  const sourceLeft = sourceLeftElement?.getBoundingClientRect();
  const sourceRight = sourceRightElement?.getBoundingClientRect();
  const targetLeft = targetLeftElement?.getBoundingClientRect();
  const targetRight = targetRightElement?.getBoundingClientRect();

  if (sourceLeft && sourceRight && targetLeft && targetRight) {
    const distances = [
      {
        sourceId: sourceLeftId,
        targetId: targetLeftId,
        distance: getDistance(
          { x: sourceLeft.left, y: sourceLeft.top + sourceLeft.height / 2 },
          { x: targetLeft.left, y: targetLeft.top + targetLeft.height / 2 }
        )
      },
      {
        sourceId: sourceLeftId,
        targetId: targetRightId,
        distance: getDistance(
          { x: sourceLeft.left, y: sourceLeft.top + sourceLeft.height / 2 },
          { x: targetRight.right, y: targetRight.top + targetRight.height / 2 }
        )
      },
      {
        sourceId: sourceRightId,
        targetId: targetLeftId,
        distance: getDistance(
          { x: sourceRight.right, y: sourceRight.top + sourceRight.height / 2 },
          { x: targetLeft.left, y: targetLeft.top + targetLeft.height / 2 }
        )
      },
      {
        sourceId: sourceRightId,
        targetId: targetRightId,
        distance: getDistance(
          { x: sourceRight.right, y: sourceRight.top + sourceRight.height / 2 },
          { x: targetRight.right, y: targetRight.top + targetRight.height / 2 }
        )
      }
    ];

    const optimalEndpoints = distances.reduce((min, current) => (current.distance < min.distance ? current : min));

    return {
      sourceId: optimalEndpoints.sourceId,
      targetId: optimalEndpoints.targetId
    };
  } else {
    // console.error("Unable to retrieve bounding box for one or more endpoint elements.");
    return { sourceId, targetId };
  }
};

const makeTableDraggable = (tableElement, jsPlumbInstance, container) => {
  try {
    if (tableElement) {
      jsPlumbInstance.draggable(tableElement.id, {
        containment: container,
        stop: (params) => {
          const newPosition = {
            top: params.pos[1],
            left: params.pos[0]
          };
          localStorage.setItem(tableElement.id, JSON.stringify(newPosition));

          const connections = JSON.parse(localStorage.getItem("connections")) || [];
          const relevantConnections = connections.filter(
            (connection) =>
              connection.sourceId.includes(tableElement.id) ||
              connection.targetId.includes(tableElement.id)
          );

          relevantConnections.forEach((connection) => {
            const sourceId = typeof connection.sourceId === 'string' ? connection.sourceId : connection.sourceId.toString();
            const targetId = typeof connection.targetId === 'string' ? connection.targetId : connection.targetId.toString();
            const optimalEndpoints = getOptimalEndpointPosition(sourceId, targetId);

            const existingConnection = jsPlumbInstance.getConnections({
              source: connection.sourceId,
              target: connection.targetId
            })[0];
            if (existingConnection) {
              jsPlumbInstance.deleteConnection(existingConnection);
            }

            connectEndpoints(
              jsPlumbInstance,
              optimalEndpoints.sourceId,
              optimalEndpoints.targetId,
              connection.relationship,
              connection.relationship_type || 'many_to_many',
              true
            );
            addForeignKeyToConnection(jsPlumbInstance, optimalEndpoints.sourceId, optimalEndpoints.targetId, 'fk');
          });

          jsPlumbInstance.repaintEverything();
        }
      });
    } else {
      console.error("Table container not found for draggable operation.");
    }
  } catch (error) {
    console.error(`Failed to make table container draggable: ${error}`);
  }
};

// const addEndpoint = (jsPlumbInstance, elementId, anchorPosition, config = {}) => {
//   const endpoint = jsPlumbInstance.addEndpoint(elementId, {
//     anchor: anchorPosition,
//     isSource: true,
//     isTarget: true,
//     maxConnections: -1,
//     endpoint: ['Dot', { radius: 6 }],
//     paintStyle: { fill: 'gray', radius: 6 },
//     hoverPaintStyle: { fill: 'red', radius: 8 },
//     ...config,
//   });

//   const endpointCanvas = endpoint.canvas || endpoint.endpoint.canvas;
//   if (endpointCanvas) {
//     endpointCanvas.style.cursor = 'default';

//     endpointCanvas.addEventListener('mouseenter', () => {
//       endpoint.setPaintStyle({ fill: 'red', radius: 8 });
//       endpointCanvas.style.cursor = 'pointer';
//     });

//     endpointCanvas.addEventListener('mouseleave', () => {
//       endpoint.setPaintStyle({ fill: 'gray', radius: 6 });
//       endpointCanvas.style.cursor = 'default';
//     });
//   }
// };

const addEndpoint = (jsPlumbInstance, elementId, anchorPosition, config = {}) => {
  try {
    const element = document.getElementById(elementId);
    if (!element) {
      // console.error('Element not found for addEndpoint:', elementId);
      return null;
    }

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

    const endpointCanvas = endpoint.canvas || endpoint.endpoint?.canvas;
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
    } else {
      // console.warn('Endpoint canvas not found for element:', elementId);
    }

    return endpoint;
  } catch (error) {
    // console.error('Error in addEndpoint:', error);
    return null;
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
    const finalZoomLevel = Math.max(0.8, Math.min(optimalZoomLevel, 2)); // Limit zoom between 0.8 and 2
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

  const jsPlumbInstance = jsPlumb.getInstance({
    Container: container,
  });

  jsPlumbInstance.repaintEverything();
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

// const adjustOffsetForZoom = (container, scaleFactor, setOffset) => {
//   if (container) {
//     const containerRect = container.getBoundingClientRect();

//     // Calculate center of screen
//     const centerX = (containerRect.left + containerRect.right) / 2 - 500;
//     const centerY = (containerRect.top + containerRect.bottom) / 2;

//     // Calculate offset
//     setOffset((prevOffset) => {
//       const newOffsetX = centerX - (centerX - prevOffset.x) * scaleFactor;
//       const newOffsetY = centerY - (centerY - prevOffset.y) * scaleFactor;
//       localStorage.setItem('offsetX', newOffsetX);
//       localStorage.setItem('offsetY', newOffsetY);

//       return { x: newOffsetX, y: newOffsetY };
//     });
//   }
// };    

const adjustOffsetForZoom = (container, scaleFactor, setOffset) => {
  if (container) {
    const containerRect = container.getBoundingClientRect();

    // const centerX = (containerRect.left + containerRect.right) / 2 - container.offsetLeft;
    // const centerY = (containerRect.top + containerRect.bottom) / 2 - container.offsetTop;

    const centerX = 0;
    const centerY = 0;

    setOffset((prevOffset) => {
      const newOffsetX = centerX - (centerX - prevOffset.x) * scaleFactor;
      const newOffsetY = centerY - (centerY - prevOffset.y) * scaleFactor;
      return { x: newOffsetX, y: newOffsetY };
    });
  }
};

const showConfirmationPopup = (message) => {
  return new Promise((resolve) => {
    const popup = document.createElement('div');
    popup.className = 'popup-overlay';
    popup.innerHTML = `
          <div class="popup">
              <p class="semibold-text">${message}</p>
              <div class="popup-buttons">
                  <button id="confirm-btn" class="btn-primary">Confirm</button>
                  <button id="cancel-btn" class="btn-secondary">Cancel</button>
              </div>
          </div>
      `;

    document.body.appendChild(popup);

    // Style for buttons container
    const popupButtons = popup.querySelector('.popup-buttons');
    popupButtons.style.display = 'flex';
    popupButtons.style.justifyContent = 'flex-end';
    popupButtons.style.gap = '10px'; // Adds spacing between buttons

    popup.querySelector('#confirm-btn').onclick = () => {
      resolve(true); // User confirmed
      document.body.removeChild(popup);
    };

    popup.querySelector('#cancel-btn').onclick = () => {
      resolve(false); // User canceled
      document.body.removeChild(popup);
    };
  });
};