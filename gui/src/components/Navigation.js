import React, { useState, useEffect, useRef } from 'react';
import { SimpleTreeView } from '@mui/x-tree-view/SimpleTreeView';
import { TreeItem } from '@mui/x-tree-view/TreeItem';
import { fetchApi } from '../utils/common';
import { setPathKeywords } from '../components/configs/editorConfig';
import './Navigation.css';
import '../styleguides/styleguides.css';
import { ReactComponent as CirclePlus } from '../assets/images/circle-plus-solid.svg';
import SetSemanticLayerModal from './SetSemanticLayerModal';
import Resizable from 'react-resizable-layout';

const Navigation = ({ view, onTableSelect, refreshNav, onGenerateDDL, setView, setUslNamebyClick }) => {

  const reSizingOffset = 115;
  const resizingRef = useRef(false);
  const [currentFullPaths, setCurrentFullPaths] = useState([]);

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

  const [showPopup, setShowPopup] = useState(false);
  const [ddlName, setDdlName] = useState('');
  const [ddlCode, setDdlCode] = useState('');
  const [tableNames, setTableNames] = useState([]);

  useEffect(() => {
    if (currentFullPaths.length > 0) {
      setPathKeywords(currentFullPaths);
    }
  }, [currentFullPaths]);

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
  
        for (const childNode of dataSourceChildren) {
          const secondLevelChildren = await fetchDatasources(childNode.fullPath);
          childNode.children = secondLevelChildren || [];
        }
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
  
        for (const childNode of semanticLayerChildren) {
          const secondLevelChildren = await fetchDatasources(childNode.fullPath, true);
          childNode.children = secondLevelChildren || [];
        }
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
    let query;
    const currentLevel = getCurrentLevel(fullPath);

    // Determine query logic based on namespace prefix and current level
    if (fullPath.startsWith("lightning.datasource")) {
      if (currentLevel === 2 || currentLevel === 3 || currentLevel === 4) {
        query = `SHOW NAMESPACES IN ${fullPath};`;
      } else if (currentLevel === 5) {
        query = `SHOW TABLES IN ${fullPath};`;
      } else if (currentLevel === 6) {
        query = `DESC ${fullPath};`;
      }
    } else if (fullPath.startsWith("lightning.metastore")) {
      if (currentLevel === 2 || currentLevel === 3) {
        query = `SHOW NAMESPACES IN ${fullPath};`;
      } else if (currentLevel === 4) {
        query = `SHOW TABLES IN ${fullPath};`;
      } else if (currentLevel === 5) {
        query = `DESC ${fullPath};`;
      }
    }

    let result;
    if(currentLevel > 1){
      result = await fetchApi(query);
    }
    if (!Array.isArray(result) || result.length === 0) return [];

    // Process result based on the query type
    if (query.startsWith('SHOW TABLES')) {
      const parsedResult = result.map((item) => JSON.parse(item).tableName);
      const excludedNamespaces = ["information_schema", "pg_catalog", "public"];
      const filteredResult = parsedResult.filter(table => !excludedNamespaces.includes(table))
        .map(table => ({ name: table, fullPath: `${fullPath}.${table}`, isTable: true, children: null }));

      setCurrentFullPaths((prevPaths) => [...prevPaths, ...filteredResult.map((item) => item.fullPath)]);
      return filteredResult;
    } else if (query.startsWith('DESC')) {
      const parsedResult = result.map((item) => JSON.parse(item));
      return parsedResult.map((column) => ({
        name: column.col_name,
        dataTypeElement: <span style={{ fontSize: '0.8em', color: '#888', marginLeft: '10px' }}>({column.data_type})</span>,
        children: null
      }));
    } else {
      const parsedResult = result.map((item) => JSON.parse(item).namespace);
      const excludedNamespaces = ["information_schema", "pg_catalog", "public"];
      return parsedResult.filter(namespace => !excludedNamespaces.includes(namespace))
        .map(namespace => ({
          name: namespace,
          fullPath: `${fullPath}.${namespace}`,
          children: null,
          ...(isMetastore && currentLevel === 3 ? { isMetastoreLevel: true } : {})
        }));
    }
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
    const isMetastore = (node.name && node.name.includes('metastore')) || (node.fullPath && node.fullPath.includes('metastore'));
    await fetchChildNodes(node, isMetastore);
  };

  const fetchChildNodes = async (node, isMetastore = false) => {
    if (node.children && node.children.length > 0) return;
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

  const handleTableClick = async (node) => {
    if (node.isTable === true && view === 'semanticLayer') {
      const tableDesc = await getTableDesc(node.fullPath);
      onTableSelect(node.fullPath, tableDesc);
    }
    fetchChildNodes(node, node.fullPath.includes('metastore'));
  };

  const drawUSL = async (node) => {
    // console.log(node.name)
    setUslNamebyClick(node.fullPath);
    setView('semanticLayer');
    sessionStorage.setItem('selectedTab', 'semanticLayer');
  }

  const renderTree = (nodes, parentPath = '', isSemanticLayer = false) => {
    return nodes.map((node, index) => {
      const currentPath = node.fullPath || (parentPath ? `${parentPath}.${node.name}` : node.name);
      const uniqueId = `${currentPath}-${index + 1}`;

      // Check if the table is activated
      const isActivated = tableNames.includes(node.name);

      return (
        <TreeItem
          key={uniqueId}
          itemId={uniqueId}
          label={
            <span style={{ color: isActivated ? '#27A7D2' : 'inherit' }} className="MuiTreeItem-label">
              {node.name}
              {node.dataTypeElement && node.dataTypeElement}

              {/* Activate/DeActivate button for isTable nodes in semanticLayer view */}
              {/* {node.isTable === true && isSemanticLayer && view === 'semanticLayer' && (
                <button
                  className='btn-table-add'
                  onClick={(event) => {
                    event.stopPropagation();
                    if (isActivated) {
                      deActivateTable(node.fullPath);
                    } else {
                      activateTable(node.fullPath);
                    }
                  }}
                >
                  {isActivated ? 'DeActivate' : 'Activate'}
                </button>
              )} */}

              {/* Draw button for isMetastore nodes in semanticLayer view */}
              {node.isMetastoreLevel === true && isSemanticLayer && (
                <button
                  className='btn-table-add'
                  onClick={(event) => {
                    event.stopPropagation();
                    drawUSL(node);
                  }}
                >
                  ERD
                </button>
              )}

              {/* Add button for isTable nodes in semanticLayer view */}
              {/* {node.isTable === true && view === 'semanticLayer' && !isSemanticLayer && (
                <button
                  className='btn-table-add'
                  onClick={(event) => {
                    event.stopPropagation();
                    handleTableClick(node);
                  }}
                >
                  Add
                </button>
              )} */}
            </span>
          }
          onClick={() => {
            // const isMetastore = node.fullPath ? node.fullPath.includes('metastore') : false;
            // fetchChildNodes(node, isMetastore);
            handleTreeItemClick(node);
          }}
        >
          {Array.isArray(node.children) ? renderTree(node.children, currentPath, isSemanticLayer) : null}
        </TreeItem>
      );
    });
  };

  return (
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
            <SimpleTreeView className="tree-view">
              {renderTree(dataSources, '', false)}
            </SimpleTreeView>
          </div>

          {/* Separator */}
          {/* <div
            {...separatorProps}
            className="separator"
            style={{ height: '5px', cursor: 'row-resize', zIndex: '100' }}
          /> */}
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
            <SimpleTreeView className="tree-view">
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
  );
};

export default Navigation;