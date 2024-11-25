import React, { useState, useEffect, useRef } from 'react';
import { SimpleTreeView, TreeItem } from '@mui/x-tree-view';
import { fetchApi } from '../utils/common';
import './Navigation.css';
import '../styleguides/styleguides.css';
import { ReactComponent as CirclePlus } from '../assets/images/circle-plus-solid.svg';
import { ReactComponent as TableIcon } from '../assets/images/table-solid.svg';
import { ReactComponent as FolderIcon } from '../assets/images/folder-regular.svg';
import { ReactComponent as PreviewIcon } from '../assets/images/circle-play-regular.svg';
import { ReactComponent as MinusIcon } from '../assets/images/square-minus-regular-black.svg';
import { ReactComponent as PlusIcon } from '../assets/images/square-plus-regular-black.svg';
import SetSemanticLayerModal from './SetSemanticLayerModal';
import Resizable from 'react-resizable-layout';
import PreviewPopup from './PreviewPopup';

const Navigation = ({ refreshNav, onGenerateDDL, setView, setUslNamebyClick }) => {

  const reSizingOffset = 115;
  const resizingRef = useRef(false);
  const [showPopup, setShowPopup] = useState(false);
  const [ddlName, setDdlName] = useState('');
  const [ddlCode, setDdlCode] = useState('');
  const [previewOpen, setPreviewOpen] = useState(false);
  const [columns, setColumns] = useState([]);
  const [previewData, setPreviewData] = useState([]);
  const [errorMessage, setErrorMessage] = useState(null);
  const [expandedNodeIds, setExpandedNodeIds] = useState([]);
  const [activeInputNode, setActiveInputNode] = useState(null);
  const [inputValue, setInputValue] = useState('');

  // State for managing datasource tree structure
  const [dataSources, setDataSources] = useState([
    {
      name: 'lightning.datasource',
      children: null
    }
  ]);

  // State for managing semantic layer files
  const [semanticLayerFiles, setSemanticLayerFiles] = useState([
    {
      name: 'lightning.metastore',
      children: null
    }
  ]);

  // Update handleGenerateClick to pass the DDL to onGenerateDDL
  const handleGenerateClick = () => {
    onGenerateDDL(ddlName, ddlCode);
    setShowPopup(false);
    setView('semanticLayer');
  };

  const handleMouseMove = (e) => {
    if (resizingRef.current) {
      e.preventDefault();
    }
  };

  const handleMouseUp = () => {
    resizingRef.current = false;
  };

  useEffect(() => {
    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    };
  }, []);

  useEffect(() => {
    const fetchInitialData = async () => {
      // Fetch DataSource tree
      const dataSourceChildren = await fetchDatasources('lightning.datasource');
      if (dataSourceChildren) {
        const updatedDataSources = dataSources.map((node) => ({
          ...node,
          children: dataSourceChildren,
        }));
        setDataSources(updatedDataSources);
      }

      // Fetch SemanticLayer tree
      const semanticLayerChildren = await fetchDatasources('lightning.metastore', true);
      if (semanticLayerChildren) {
        const updatedSemanticLayers = semanticLayerFiles.map((node) => ({
          ...node,
          children: semanticLayerChildren,
        }));
        setSemanticLayerFiles(updatedSemanticLayers);
      }
    };
    fetchInitialData();
  }, []);

  // Function to get the depth (level) of the current path
  const getCurrentLevel = (fullPath) => {
    const parts = fullPath.split('.');
    return parts.length;
  };

  const fetchDatasources = async (fullPath, isMetastore = false) => {
    const excludedNamespaces = ["information_schema", "pg_catalog", "public"];
    let query;
    const currentLevel = getCurrentLevel(fullPath);

    // Determine query based on level
    if (fullPath.startsWith("lightning.datasource") || fullPath.startsWith("lightning.metastore")) {
      query = `SHOW NAMESPACES OR TABLES IN ${fullPath};`;
    }

    const result = await fetchApi(query);
    if (!Array.isArray(result) || result.length === 0) return [];

    return result
      .map((item) => JSON.parse(item))
      .filter((parsedItem) => !excludedNamespaces.includes(parsedItem.name))
      .map((parsedItem) => ({
        name: parsedItem.name,
        fullPath: `${fullPath}.${parsedItem.name}`,
        type: parsedItem.type,
        children: null,
      }));
  };

  const getTableDesc = async (fullPath) => {
    let query = `DESC ${fullPath}`;
    const result = await fetchApi(query);
    if (result) {
      const parsedResult = result.map((item) => JSON.parse(item));
      return parsedResult;
    } else {
      return [];
    }
  };

  const handleTreeItemClick = async (node) => {
    if (node.type === 'table') {
      const tableDetails = await getTableDesc(node.fullPath);

      const tableChildren = tableDetails.map((column) => ({
        name: column.col_name,
        fullPath: `${node.fullPath}.${column.col_name}`,
        type: 'column',
        children: null,
        dataTypeElement: (
          <span style={{ fontSize: '0.8em', color: '#888', marginLeft: '10px' }}>
            ({column.data_type})
          </span>
        ),
      }));

      node.children = tableChildren;
      setDataSources((prevData) => updateNodeChildren(prevData, node.name, tableChildren));
    } else {
      await fetchChildNodes(node, node.fullPath?.startsWith("lightning.metastore"));
    }
  };


  const fetchChildNodes = async (node, isMetastore = false) => {
    const childNodes = await fetchDatasources(node.fullPath || node.name, isMetastore);
    if (isMetastore) {
      setSemanticLayerFiles((prevData) => updateNodeChildren(prevData, node.name, childNodes));
    } else {
      setDataSources((prevData) => updateNodeChildren(prevData, node.name, childNodes));
    }
  };

  const updateNodeChildren = (nodes, nodeName, children) => {
    return nodes.map((node) => {
      if (node.name === nodeName) {
        return { ...node, children };
      }
      if (node.children) {
        return { ...node, children: updateNodeChildren(node.children, nodeName, children) };
      }
      return node;
    });
  };

  useEffect(() => {
  }, [refreshNav]);

  const drawUSL = async (node) => {
    setUslNamebyClick(node.fullPath);
    setView('semanticLayer');
    sessionStorage.setItem('selectedTab', 'semanticLayer');
  }

  const handlePreview = async (fullPath) => {
    const query = `SELECT * FROM ${fullPath} LIMIT 100`;
    const result = await fetchApi(query);

    if (Array.isArray(result)) {
      const parsedData = result.map(item => JSON.parse(item));
      setPreviewData(parsedData);

      if (parsedData.length > 0) {
        const dynamicColumns = Object.keys(parsedData[0]).map(key => ({
          accessorKey: key,
          header: key.charAt(0).toUpperCase() + key.slice(1)
        }));
        setColumns(dynamicColumns);
      }
      setPreviewOpen(true);
    } else {
      setErrorMessage(`Error : ${result.message}`);
      setPreviewOpen(true);
    }
  };

  const handleClosePreview = () => {
    setPreviewOpen(false);
    setErrorMessage(null);
  };

  const handlePlusClick = async (event, node) => {
    event.stopPropagation();

    if (!node.children) {
      const childNodes = await fetchDatasources(node.fullPath || node.name);
      setDataSources((prev) => updateNodeChildren(prev, node.name, childNodes));
    }

    setExpandedNodeIds((prev) => [...new Set([...prev, node.uniqueId])]);

    setActiveInputNode(node.uniqueId);
    setInputValue('');
  };

  const handleMinusClick = async (event, node) => {
    event.stopPropagation();

    const query = `DROP NAMESPACE ${node.fullPath}`;

    try {
      const response = await fetchApi(query);
      if (response.error) {
        console.error(`Error dropping namespace: ${response.message}`);
      } else {
        setDataSources((prev) => removeNode(prev, node.name));
      }
    } catch (error) {
      console.error(`Error during API call: ${error.message}`);
    }
  };

  const removeNode = (nodes, nodeName) => {
    return nodes
      .map((node) => {
        if (node.name === nodeName) {
          return null;
        }
        if (node.children) {
          return { ...node, children: removeNode(node.children, nodeName) };
        }
        return node;
      })
      .filter(Boolean);
  };

  const handleInputKeyDown = async (event, node) => {
    if (event.key === 'Escape') {
      setActiveInputNode(null);
    } else if (event.key === 'Enter') {
      const namespaceName = inputValue.trim();
      if (!namespaceName) {
        console.error('Namespace name cannot be empty.');
        return;
      }

      const query = `CREATE NAMESPACE ${node.fullPath}.${namespaceName}`;

      try {
        const response = await fetchApi(query);
        if (response.error) {
          console.error(`Error creating namespace: ${response.message}`);
        } else {
          handleTreeItemClick(node);
        }
      } catch (error) {
        console.error(`Error during API call: ${error.message}`);
      }

      setActiveInputNode(null);
    }
  };

  const checkActivate = async (node) => {
    if (!node.children || node.children.length === 0) return;
  
    const fullPathParts = node.fullPath.split('.');
    const namespace = fullPathParts.slice(0, -1).join('.');
    const uslName = fullPathParts[fullPathParts.length - 1];
    const query = `LOAD USL ${uslName} NAMESPACE ${namespace}`;
  
    try {
      const result = await fetchApi(query);
  
      if (result && Array.isArray(result)) {
        const parsedResult = result
          .map((item) => {
            try {
              if (item?.json) {
                const topLevelJson = JSON.parse(item.json);
  
                const tables = topLevelJson.tables.map((table) => ({
                  name: table.name,
                  activateQuery: table.activateQuery || null,
                }));
  
                return { tables };
              }
              return null;
            } catch (error) {
              console.error(`Error parsing item.json:`, error, item);
              return null;
            }
          })
          .filter(Boolean);
  
        node.children = node.children.map((child) => {
          const matchingTable = parsedResult
            .flatMap((res) => res.tables)
            .find((table) => table.name === child.name);
  
          return {
            ...child,
            activate: !!matchingTable?.activateQuery,
          };
        });
        console.log(node)
      }
    } catch (error) {
      console.error(`Error checking activation for ${node.name}:`, error);
    }
  };  

  const renderTree = (nodes, parentPath = '', isSemanticLayer = false) => {
    return nodes.map((node, index) => {
      const currentPath = node.fullPath || (parentPath ? `${parentPath}.${node.name}` : node.name);
      const uniqueId = `${currentPath}-${index + 1}`;
      const Icon = node.type === 'table' ? TableIcon : FolderIcon;
      let activateChildren;
      node.uniqueId = uniqueId;

      const hasTableChild = Array.isArray(node.children) ? node.children.some((child) => child.type === 'table') || false : null;

      if (hasTableChild && node.children) {
        checkActivate(node);
      }

      return (
        <TreeItem
          key={uniqueId}
          itemId={uniqueId}
          label={
            <span style={{ display: 'flex', alignItems: 'center' }}>
              {node.type !== 'column' && (
                <Icon style={{ width: '18px', height: '18px', marginRight: '8px', verticalAlign: 'middle', flexShrink: 0 }} />
              )}
              {node.name}
              {node.dataTypeElement && node.dataTypeElement}

              {node.type === 'table' && (
                <button
                  onClick={(event) => {
                    event.stopPropagation();
                    handlePreview(node.fullPath);
                  }}
                  style={{
                    background: 'none',
                    border: 'none',
                    padding: 0,
                    cursor: 'pointer',
                    marginLeft: '8px',
                    verticalAlign: 'middle',
                  }}
                >
                  <PreviewIcon style={{ width: '18px', height: '18px' }} />
                </button>
              )}

              {hasTableChild && isSemanticLayer && (
                <button
                  className="btn-table-add"
                  onClick={(event) => {
                    event.stopPropagation();
                    drawUSL(node);
                  }}
                >
                  ERD
                </button>
              )}

              {node.type === 'namespace' && !isSemanticLayer && (
                hasTableChild === false && (
                  <div style={{ marginLeft: '10px', display: 'flex', alignItems: 'center' }}>
                    <PlusIcon
                      onClick={(event) => handlePlusClick(event, node)}
                      style={{
                        width: '25px',
                        height: '25px',
                        cursor: 'pointer',
                        verticalAlign: 'middle',
                      }}
                    />
                    <MinusIcon
                      onClick={(event) => handleMinusClick(event, node)}
                      style={{
                        width: '25px',
                        height: '25px',
                        cursor: 'pointer',
                        verticalAlign: 'middle',
                      }}
                    />
                  </div>
                )
              )}
            </span>
          }
          onClick={() => handleTreeItemClick(node)}
        >
          {activeInputNode === node.uniqueId && (
            <div style={{ marginLeft: '30px' }}>
              <input
                autoFocus
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onKeyDown={(e) => {
                  e.stopPropagation();
                  handleInputKeyDown(e, node);
                }}
                style={{
                  padding: '6px',
                  border: '2px solid #27A7D2',
                  borderRadius: '6px',
                  width: '180px',
                  outline: 'none',
                }}
                placeholder="Enter Namespace"
              />

            </div>
          )}

          {Array.isArray(node.children) ? renderTree(node.children, currentPath, isSemanticLayer) : null}
        </TreeItem>
      );
    });
  };

  return (
    <>
      <Resizable
        axis="y"
        initial={700}
        min={200}
        max={1000}
        onResizeStart={() => {
          resizingRef.current = true;
        }}
        onResizeStop={(e, direction, ref, d) => {
          resizingRef.current = false;
        }}
      >
        {({ position, separatorProps }) => (
          <div className="guideline" style={{ display: 'flex', flexDirection: 'column', overflowY: 'auto' }}>
            <div style={{ height: `${position - reSizingOffset}px`, overflowY: 'auto', padding: '0 30px', paddingTop: '20px' }}>
              {/* Data Sources Tree */}
              <div className="nav-tab bold-text">Data Sources</div>
              <SimpleTreeView className="tree-view"
                expandedItems={expandedNodeIds}
                onExpandedItemsChange={(event, newExpanded) => {
                  setExpandedNodeIds(newExpanded);
                }}
              >
                {renderTree(dataSources, '', false)}
              </SimpleTreeView>
            </div>

            <div
              {...separatorProps}
              className="separator"
              style={{
                height: '1px',
                backgroundColor: '#ccc',
                cursor: 'row-resize',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                position: 'relative',
              }}
            >
              <div
                style={{
                  width: '50px',
                  height: '8px',
                  backgroundColor: '#888',
                  borderRadius: '4px',
                  position: 'absolute',
                }}
              />
            </div>

            <div style={{ flexGrow: 1, overflowY: 'auto', paddingBottom: '100px', paddingRight: '30px', paddingLeft: '30px', paddingBottom: '50px' }}>
              {/* Semantic Layer Tree */}
              <div className="nav-tab bold-text" style={{ display: 'flex', alignItems: 'center' }}>
                Semantic Layer
                <CirclePlus
                  onClick={() => setShowPopup(true)}
                  style={{ height: '20px', width: '20px', fill: '#27A7D2', cursor: 'pointer', marginLeft: '10px' }}
                />
              </div>
              <SimpleTreeView className="tree-view"
                expandedItems={expandedNodeIds}
                onExpandedItemsChange={(event, newExpanded) => {
                  setExpandedNodeIds(newExpanded);
                }}
              >
                {renderTree(semanticLayerFiles, '', true)}
              </SimpleTreeView>
              <SetSemanticLayerModal
                showPopup={showPopup}
                setShowPopup={setShowPopup}
                ddlName={ddlName}
                setDdlName={setDdlName}
                ddlCode={ddlCode}
                setDdlCode={setDdlCode}
                handleGenerateClick={handleGenerateClick}
              />
            </div>
          </div>
        )}
      </Resizable>

      <PreviewPopup
        open={previewOpen}
        onClose={handleClosePreview}
        columns={columns}
        data={previewData}
        errorMessage={errorMessage}
      />
    </>
  );
};

export default Navigation;