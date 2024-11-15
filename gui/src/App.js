import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Content from './components/Content';
import Navigation from './components/Navigation';
import Resizable from 'react-resizable-layout';
import './App.css'; // Import the CSS file
import { fetchApiForListSemanticLayers } from './utils/common'

function App() {
  // State to manage view and navigation updates
  const [view, setView] = useState('');
  const [refreshNav, setRefreshNav] = useState(false); // This will trigger navigation refresh
  const [selectedTable, setSelectedTable] = useState(null);
  const [semanticLayerInfo, setSemanticLayerInfo] = useState([]); // State to manage semantic layers
  const [uslNamebyClick, setUslNamebyClick] = useState(''); // State to manage semantic layers

  // Function to handle updates or changes that should refresh navigation
  const toggleRefreshNav = () => {
    setRefreshNav((prev) => !prev); // Toggle the state to refresh Navigation
  };

  const handleTableSelect = (name, desc) => {
    setSelectedTable({ name, desc });
  };

  // onGenerateDDL function to handle the new semantic layer from Navigation
  const onGenerateDDL = (name, ddlCode) => {
    // Create a new semantic layer object
    const newLayer = {
      name: name,
      ddl: ddlCode
    };

    // Add the new semantic layer to the list of semantic layers
    setSemanticLayerInfo([newLayer]);
    toggleRefreshNav(); // Trigger navigation refresh after adding the new semantic layer
  };

  return (
    <div>
      {/* Header section */}
      <div className="header">
        <Header setView={setView} />
      </div>

      {/* Resizable layout for Navigation and Content */}
      <div style={{ display: 'flex', height: '90vh' }}>
        <Resizable axis={'x'} initial={300} min={200} max={400}>
          {({ position, separatorProps }) => (
            <>
              {/* Navigation Section with refreshNav prop to trigger refresh */}
              <div className="navigation" style={{ minWidth: '200px', width: position }}>
                <Navigation refreshNav={refreshNav} view={view} onTableSelect={handleTableSelect} onGenerateDDL={onGenerateDDL} setView={setView} setUslNamebyClick={setUslNamebyClick} /> {/* Pass the state to Navigation */}
              </div>

              {/* Resizable separator */}
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
                    zIndex: '10'
                  }}
                />
              </div>


              {/* Content Section */}
              <div className="content" style={{ flexGrow: 1 }}>
                <Content view={view} toggleRefreshNav={toggleRefreshNav} selectedTable={selectedTable} semanticLayerInfo={semanticLayerInfo} uslNamebyClick={uslNamebyClick} />
                {/* Pass the handler to Content for triggering navigation refresh */}
              </div>
            </>
          )}
        </Resizable>
      </div>
    </div>
  );
}

export default App;
