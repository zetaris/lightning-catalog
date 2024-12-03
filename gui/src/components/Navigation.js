import React, { useState, useEffect, useRef, useMemo } from 'react';
import { SimpleTreeView, TreeItem } from '@mui/x-tree-view';
import { fetchApi } from '../utils/common';
import './Navigation.css';
import '../styleguides/styleguides.css';
import { setPathKeywords } from '../components/configs/editorConfig';
import { ReactComponent as CirclePlus } from '../assets/images/circle-plus-solid.svg';
import { ReactComponent as TableIcon } from '../assets/images/table-solid.svg';
import { ReactComponent as FolderIcon } from '../assets/images/folder-regular.svg';
import { ReactComponent as PreviewIcon } from '../assets/images/circle-play-regular.svg';
import { ReactComponent as MinusIcon } from '../assets/images/square-minus-regular-black.svg';
import { ReactComponent as PlusIcon } from '../assets/images/square-plus-regular-black.svg';
import { ReactComponent as PkIcon } from '../assets/images/key-outline.svg';
import { ReactComponent as UniqueIcon } from '../assets/images/fingerprint-solid.svg';
import { ReactComponent as IndexIcon } from '../assets/images/book-solid.svg';
import { ReactComponent as NNIcon } from '../assets/images/notnull-icon.svg';
import { ReactComponent as LinkIcon } from '../assets/images/link-solid.svg';
import SetSemanticLayerModal from './SetSemanticLayerModal';
import Resizable from 'react-resizable-layout';

const Navigation = ({ refreshNav, onGenerateDDL, setView, setUslNamebyClick, setPreviewTableName, setIsLoading }) => {

  const reSizingOffset = 115;
  const resizingRef = useRef(false);
  const [showPopup, setShowPopup] = useState(false);
  const [ddlName, setDdlName] = useState('');
  const [ddlCode, setDdlCode] = useState('');
  const [expandedNodeIds, setExpandedNodeIds] = useState([]);
  const [activeInputNode, setActiveInputNode] = useState(null);
  const [inputValue, setInputValue] = useState('');
  const [previewableTables, setPreviewableTables] = useState(new Set());
  const [hasTableChild, setHasTableChild] = useState(false);
  const [selectedUSL, setSelectedUSL] = useState('');
  const [currentFullPaths, setCurrentFullPaths] = useState([]);
  const [popupMessage, setPopupMessage] = useState(null);

  const closePopup = () => setPopupMessage(null);

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

  const handleGenerateClick = () => {
    if (!ddlName.trim()) {
      setPopupMessage('Semantic Layer Name cannot be empty. Please enter a valid name.');
      return;
    }

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

  useEffect(() => {
    if (currentFullPaths.length > 0) {
      // setPathKeywords(currentFullPaths);
    }
  }, [currentFullPaths]);

  useEffect(() => {
    const loadUSLFromStorage = () => {
      const storedUSL = localStorage.getItem(selectedUSL);
      if (storedUSL) {
        const uslData = JSON.parse(storedUSL);
        const semanticLayerTree = renderTreeFromUSL(uslData);
        setSemanticLayerFiles((prevData) => updateNodeChildren(prevData, uslData.name, semanticLayerTree));
      }
    };

    loadUSLFromStorage();
  }, []);

  // Function to get the depth (level) of the current path
  const getCurrentLevel = (fullPath) => {
    const parts = fullPath.split('.');
    return parts.length;
  };

  const fetchDatasources = async (fullPath, isMetastore = false) => {
    const excludedNamespaces = [];
    let query;
    const currentLevel = getCurrentLevel(fullPath);

    // Determine query based on level
    if (fullPath.toLowerCase().includes('datasource') || fullPath.toLowerCase().includes('metastore')) {
      query = `SHOW NAMESPACES OR TABLES IN ${fullPath};`;
    }

    const result = await fetchApi(query);
    if (!Array.isArray(result) || result.length === 0) return [];

    const fetchedData = result
      .map((item) => JSON.parse(item))
      .filter((parsedItem) => !excludedNamespaces.includes(parsedItem.name))
      .map((parsedItem) => ({
        name: parsedItem.name,
        fullPath: `${fullPath}.${parsedItem.name}`,
        type: parsedItem.type,
        children: null,
      }));

    // **Update currentFullPaths**
    // setCurrentFullPaths((prevPaths) => [
    //   ...prevPaths,
    //   ...fetchedData.map((item) => item.fullPath),
    // ]);

    return fetchedData;
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

  const renderTreeFromUSL = (uslData) => {
    const { namespace, name, tables } = uslData;

    const treeItems = tables.map((table) => ({
      name: table.name,
      fullPath: `${namespace.join('.')}.${name}.${table.name}`,
      type: 'table',
      activateQuery: table.activateQuery,
      children: table.columnSpecs.map((column) => ({
        name: column.name,
        fullPath: `${namespace.join('.')}.${name}.${table.name}.${column.name}`,
        type: 'column',
        dataTypeElement: (
          <span style={{ fontSize: '0.8em', color: '#888', marginLeft: '10px' }}>
            ({column.dataType})
          </span>
        ),
      })),
    }));

    return treeItems;
  };

  const handleTreeItemClick = async (node) => {
    if (node.type === 'column') {
      // console.log(`Skipping action for USL column: ${node.name}`);
      return;
    }

    if (node.type === 'table') {
      if (node.fullPath.toLowerCase().includes('datasource')) {
        setPreviewableTables((prev) => new Set(prev).add(node.fullPath));
      }

      let uslName;
      let storedData;

      if (node.fullPath.toLowerCase().includes('metastore')) {
        uslName = node.fullPath.match(/usldb\.([^.]+)/)[1];
      }

      if (uslName) {
        storedData = JSON.parse(localStorage.getItem(uslName));
      }

      if (storedData?.name === uslName && uslName) {
        const table = storedData.tables.find((tbl) => tbl.name === node.name);
        if (table) {
          const tableChildren = table.columnSpecs.map((column) => {
            const icons = [];
            if (column.primaryKey) {
              icons.push(
                <PkIcon key={`pk-${column.name}`} style={{ width: '16px', height: '16px', marginLeft: '4px' }} />
              );
            }
            if (column.foreignKey) {
              icons.push(
                <LinkIcon key={`fk-${column.name}`} style={{ width: '16px', height: '16px', marginLeft: '4px' }} />
              );
            }
            if (column.notNull) {
              icons.push(
                <NNIcon key={`nn-${column.name}`} style={{ width: '16px', height: '16px', marginLeft: '4px' }} />
              );
            }

            return {
              // name: column.name,
              name: (
                <span style={{ display: 'flex', alignItems: 'center' }}>
                  <span style={{ marginRight: '8px', fill: '#888', display: 'flex', alignItems: 'center' }}>{icons}</span>
                  <span>{column.name}</span>
                </span>
              ),
              fullPath: `${node.fullPath}.${column.name}`,
              type: 'column',
              dataTypeElement: (
                <span style={{ display: 'flex', alignItems: 'center', marginLeft: '10px' }}>
                  <span style={{ fontSize: '0.8em', color: '#888' }}>
                    ({column.dataType.replace(/"/g, '')})
                  </span>
                </span>
              ),
            };
          });

          node.children = tableChildren;

          setSemanticLayerFiles((prevData) =>
            updateNodeChildren(prevData, node.name, tableChildren)
          );

          return;
        }
      }

      if (node.fullPath.toLowerCase().includes('metastore') && hasTableChild) {
        // console.log('Skipping getTableDesc');
        return;
      }

      const tableDetails = await getTableDesc(node.fullPath);

      // const columnFullPath = tableDetails.map((column) => `${node.fullPath}.${column.col_name}`);
      // setCurrentFullPaths((prevPaths) => [...prevPaths, ...columnFullPath]);

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

      if (node.fullPath.toLowerCase().includes('metastore')) {
        setSemanticLayerFiles((prevData) => updateNodeChildren(prevData, node.name, tableChildren));
      } else {
        setDataSources((prevData) => updateNodeChildren(prevData, node.name, tableChildren));
      }
    } else {
      const hasTableChildResult = Array.isArray(node.children)
        ? node.children.some((child) => child.type === 'table')
        : false;
      setHasTableChild(hasTableChildResult);

      if (hasTableChildResult && node.fullPath.toLowerCase().includes('metastore')) {
        const storedData = localStorage.getItem(node.name);

        if (storedData) {
          // console.log(`Loading ${node.name} data from localStorage.`);
          const uslData = JSON.parse(storedData);

          const semanticLayerChildren = uslData.tables.map((table) => ({
            name: table.name,
            fullPath: `${uslData.namespace.join('.')}.${uslData.name}.${table.name}`,
            type: 'table',
            activateQuery: table.activateQuery,
            children: table.columnSpecs.map((column) => ({
              name: column.name,
              fullPath: `${uslData.namespace.join('.')}.${uslData.name}.${table.name}.${column.name}`,
              type: 'column',
              dataTypeElement: (
                <span style={{ fontSize: '0.8em', color: '#888', marginLeft: '10px' }}>
                  ({column.dataType.replace(/"/g, '')})
                </span>
              ),
            })),
          }));

          setSemanticLayerFiles((prevData) => updateNodeChildren(prevData, node.name, semanticLayerChildren));

          uslData.tables.forEach((table) => {
            if (table.activateQuery) {
              setPreviewableTables((prev) => {
                const newSet = new Set(prev);
                newSet.add(`${uslData.namespace.join('.')}.${uslData.name}.${table.name}`);
                return newSet;
              });
            }
          });
        } else {
          const dbname = node.fullPath.split('.').pop();
          const path = node.fullPath.split('.').slice(0, -1).join('.');

          try {
            const query = `LOAD USL ${dbname} NAMESPACE ${path}`;
            const result = await fetchApi(query);
            const uslData = JSON.parse(JSON.parse(result).json);

            localStorage.setItem(node.name, JSON.stringify(uslData));

            const semanticLayerChildren = uslData.tables.map((table) => ({
              name: table.name,
              fullPath: `${uslData.namespace.join('.')}.${uslData.name}.${table.name}`,
              type: 'table',
              children: table.columnSpecs.map((column) => ({
                name: column.name,
                fullPath: `${uslData.namespace.join('.')}.${uslData.name}.${table.name}.${column.name}`,
                type: 'column',
                dataTypeElement: (
                  <span style={{ fontSize: '0.8em', color: '#888', marginLeft: '10px' }}>
                    ({column.dataType.replace(/"/g, '')})
                  </span>
                ),
              })),
            }));

            setSemanticLayerFiles((prevData) => updateNodeChildren(prevData, node.name, semanticLayerChildren));

            uslData.tables.forEach((table) => {
              if (table.activateQuery) {
                setPreviewableTables((prev) => {
                  const newSet = new Set(prev);
                  newSet.add(`${uslData.namespace.join('.')}.${uslData.name}.${table.name}`);
                  return newSet;
                });
              }
            });
          } catch (error) {
            console.error(`Error loading USL for ${node.name}:`, error);
          }
        }
      } else {
        const childNodes = await fetchChildNodes(node, node.fullPath?.toLowerCase().includes('metastore') || false);

        if (Array.isArray(childNodes) && childNodes.some((child) => child.type === 'table')) {
          setHasTableChild(true);
        }
      }
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
        return { ...node, children, hasTableChild: children.some((child) => child.type === 'table') };
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
    setIsLoading(true);
    const fullPath = node.fullPath;
    const dbname = fullPath.split('.').pop();
    const path = fullPath.split('.').slice(0, -1).join('.');

    let query = `LOAD USL ${dbname} NAMESPACE ${path}`;
    try {
      setIsLoading(true);
      const result = await fetchApi(query);
      const uslData = JSON.parse(JSON.parse(result).json);

      setSelectedUSL(uslData.name)
      localStorage.setItem(dbname, JSON.stringify(uslData));
      updateTreeViewAndActivateButtons(uslData);

      setUslNamebyClick(result);
      setView('semanticLayer');
      sessionStorage.setItem('selectedTab', 'semanticLayer');

      setHasTableChild(true);
    } catch (error) {
      console.error('Error fetching USL file content:', error);
    }
  };

  const updateTreeViewAndActivateButtons = (uslData) => {
    const { tables } = uslData;

    const semanticLayerChildren = tables.map((table) => ({
      name: table.name,
      fullPath: `${uslData.namespace.join('.')}.${uslData.name}.${table.name}`,
      type: 'table',
      children: table.columnSpecs.map((column) => ({
        name: column.name,
        fullPath: `${uslData.namespace.join('.')}.${uslData.name}.${table.name}.${column.name}`,
        type: 'column',
        isActivate: !!table.activateQuery,
        dataTypeElement: (
          <span style={{ fontSize: '0.8em', color: '#888', marginLeft: '10px' }}>
            ({column.dataType.replace(/\"/g, '')})
          </span>
        ),
      })),
    }));

    setSemanticLayerFiles((prevData) =>
      updateNodeChildren(prevData, uslData.name, semanticLayerChildren)
    );

    tables.forEach((table) => {
      if (table.activateQuery) {
        setPreviewableTables((prev) => {
          const newSet = new Set(prev);
          newSet.add(`${uslData.namespace.join('.')}.${uslData.name}.${table.name}`);
          return newSet;
        });
      }
    });
  };

  const handlePreview = async (fullPath) => {
    if (!fullPath) return;
    if (fullPath.toLowerCase().includes('datasource')) {
      setView('sqlEditor');
      sessionStorage.setItem('selectedTab', 'sqlEditor');
      setPreviewTableName(fullPath);
    } else if (fullPath.toLowerCase().includes('metastore')) {
      setView('semanticLayer');
      sessionStorage.setItem('selectedTab', 'semanticLayer');
      setPreviewTableName("lightning." + fullPath);
    }
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

  const renderTree = (nodes, parentPath = '', isSemanticLayer = false) => {
    const uslDataKey = nodes[0]?.fullPath?.split('.')[1];
    const uslData = uslDataKey ? JSON.parse(localStorage.getItem(uslDataKey)) : null;

    if (uslData) {
      return renderTreeFromUSL(uslData, isSemanticLayer);
    }

    return nodes.map((node, index) => {
      const currentPath = node.fullPath || (parentPath ? `${parentPath}.${node.name}` : node.name);
      const uniqueId = `${currentPath}-${index + 1}`;
      const Icon = node.type === 'table' ? TableIcon : FolderIcon;
      const hasTableChildResult = Array.isArray(node.children)
        ? node.children.some((child) => child.type === 'table')
        : false;
      setHasTableChild(hasTableChildResult);

      node.uniqueId = uniqueId;

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
                  className={`preview-button ${node.fullPath.toLowerCase().includes('metastore') && !previewableTables.has(node.fullPath) ? 'hidden' : ''
                    }`}
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

              {hasTableChildResult && isSemanticLayer && (
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

              {node.type === 'namespace' && !node.hasTableChild && (
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

  // const memoizedTreeData = useMemo(() => renderTree(dataSources, '', false), [dataSources]);
  const memoizedTreeData = useMemo(
    () => renderTree(dataSources, '', false),
    [dataSources, activeInputNode, inputValue]
  );

  const memoizedSemanticLayerTree = useMemo(() => renderTree(semanticLayerFiles, '', true), [semanticLayerFiles, activeInputNode, inputValue]);

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
                {memoizedTreeData}
                {/* {renderTree(dataSources, '', false)} */}
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
                {memoizedSemanticLayerTree}
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
            {popupMessage && (
              <div className="popup-overlay" onClick={closePopup}>
                <div className="popup-message" onClick={(e) => e.stopPropagation()}>
                  <p>{popupMessage}</p>
                  <div className="popup-buttons">
                    <button className="btn-primary" onClick={closePopup}>Close</button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </Resizable>

    </>
  );
};

export default Navigation;