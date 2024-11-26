import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Content from './components/Content';
import Navigation from './components/Navigation';
import Resizable from 'react-resizable-layout';
import './App.css';

const LoadingIndicator = () => (
  <div
    style={{
      position: 'fixed',
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
      zIndex: 1000,
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      color: '#fff',
      padding: '20px',
      borderRadius: '8px',
      textAlign: 'center',
    }}
  >
    Loading...
  </div>
);

function App() {
  const [view, setView] = useState('');
  const [refreshNav, setRefreshNav] = useState(false);
  const [selectedTable, setSelectedTable] = useState(null);
  const [semanticLayerInfo, setSemanticLayerInfo] = useState([]);
  const [uslNamebyClick, setUslNamebyClick] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [previewTableName, setPreviewTableName] = useState(null);

  const handleSetView = (newView) => {
    setView(newView);
    setRefreshNav((prev) => !prev);
  };

  const toggleRefreshNav = () => {
    setRefreshNav((prev) => !prev);
  };

  const handleTableSelect = (name, desc) => {
    setSelectedTable({ name, desc });
  };

  const onGenerateDDL = (name, ddlCode) => {
    const newLayer = { name, ddl: ddlCode };
    setSemanticLayerInfo([newLayer]);
    toggleRefreshNav();
  };

  useEffect(() => {
    setIsLoading(true);

    const simulateLoading = setTimeout(() => {
      setIsLoading(false);
    }, 0);

    return () => clearTimeout(simulateLoading);
  }, [view, refreshNav]);

  return (
    <div>
      {isLoading && <LoadingIndicator />}

      <div className="header">
        <Header setView={handleSetView} view={view} setIsLoading={setIsLoading}/>
      </div>

      <div style={{ display: 'flex' }}>
        <Resizable axis="x" initial={300} min={200} max={400}>
          {({ position, separatorProps }) => (
            <>
              <div className="navigation" style={{ minWidth: '200px', width: position }}>
                <Navigation
                  refreshNav={refreshNav}
                  onGenerateDDL={onGenerateDDL}
                  setView={handleSetView}
                  setUslNamebyClick={setUslNamebyClick}
                  setPreviewTableName={setPreviewTableName}
                />
              </div>

              <div
                {...separatorProps}
                style={{
                  width: '1px',
                  backgroundColor: '#ccc',
                  cursor: 'col-resize',
                  flexShrink: 0,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  position: 'relative',
                }}
              >
                <div
                  style={{
                    width: '8px',
                    height: '30px',
                    backgroundColor: '#888',
                    borderRadius: '4px',
                    position: 'absolute',
                    zIndex: '10',
                  }}
                />
              </div>

              <div className="content" style={{ flexGrow: 1 }}>
                <Content
                  view={view}
                  toggleRefreshNav={toggleRefreshNav}
                  selectedTable={selectedTable}
                  semanticLayerInfo={semanticLayerInfo}
                  uslNamebyClick={uslNamebyClick}
                  setIsLoading={setIsLoading}
                  previewTableName={previewTableName}
                />
              </div>
            </>
          )}
        </Resizable>
      </div>
    </div>
  );
}

export default App;
