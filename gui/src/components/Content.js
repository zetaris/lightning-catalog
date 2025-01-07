import React, { useEffect } from 'react';
import SqlEditor from './contents/SqlEditor'; 
import SemanticLayer from './contents/SemanticLayer'; 

// Import editor configuration
import { defineCustomTheme, setupAceEditor } from './configs/editorConfig';

import 'ace-builds/src-noconflict/mode-sql';
import 'ace-builds/src-noconflict/theme-monokai';
import 'ace-builds/src-noconflict/ext-language_tools';

function Content({ view, toggleRefreshNav, selectedTable, semanticLayerInfo, setSemanticLayerInfo, uslNamebyClick, setIsLoading, previewTableName, setPreviewTableName, isMouseLoading, navErrorMsg, setNavErrorMsg, setPreviewableTables }) {
  
  // Load editor configurations once the component is mounted
  useEffect(() => {
    defineCustomTheme();
    setupAceEditor();
  }, []);

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', backgroundColor: 'gray' }}>
      {view === 'sqlEditor' && (
        <SqlEditor toggleRefreshNav={toggleRefreshNav} previewTableName={previewTableName} setPreviewTableName={setPreviewTableName} isMouseLoading={isMouseLoading} navErrorMsg={navErrorMsg} setNavErrorMsg={setNavErrorMsg} />
      )}
      {view === 'semanticLayer' && (
        <SemanticLayer selectedTable={selectedTable} semanticLayerInfo={semanticLayerInfo} setSemanticLayerInfo={setSemanticLayerInfo} uslNamebyClick={uslNamebyClick} setIsLoading={setIsLoading} previewTableName={previewTableName} setPreviewTableName={setPreviewTableName} isMouseLoading={isMouseLoading} navErrorMsg={navErrorMsg} setNavErrorMsg={setNavErrorMsg} setPreviewableTables={setPreviewableTables} />
      )}
      {view === 'documentation' && window.open('https://github.com/zetaris/lightning-catalog/tree/master/doc', '_blank')}
    </div>
  );
}

export default Content;