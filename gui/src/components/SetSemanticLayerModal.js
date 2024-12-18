import React, { useState, useEffect } from 'react';
import Editor from './contents/components/Editor';
import './SetSemanticLayerModal.css';

const SetSemanticLayerModal = ({ showPopup, setShowPopup, ddlName, setDdlName, ddlCode, setDdlCode, handleGenerateClick }) => {
  const [dragOver, setDragOver] = useState(false);

  useEffect(() => {
    if (showPopup) {
      setDdlName('');
      setDdlCode('');
    }
  }, [showPopup, setDdlName, setDdlCode]);

  const handleFileRead = (file) => {
    const reader = new FileReader();
    reader.onload = (event) => {
      setDdlCode(event.target.result);
    };
    reader.readAsText(file);
  };

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      handleFileRead(file);
    }
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setDragOver(false);
    const file = event.dataTransfer.files[0];
    if (file) {
      handleFileRead(file);
    }
  };

  const handleDragOver = (event) => {
    event.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = () => {
    setDragOver(false);
  };

  const handleDropAreaClick = (event) => {
    // Prevent triggering file input click when AceEditor is clicked
    if (!event.target.closest(".ace_editor")) {
      document.getElementById('fileInput').click();
    }
  };

  if (!showPopup) return null;

  return (
    <div className="popup-overlay" style={{paddingTop:'0px'}}>
      <div className="popup-modal">
        <h3 className="popup-title">Create Semantic Layer</h3>

        {/* Form Group 1 */}
        <div className="popup-field">
          <label>Name</label>
          <input
            type="text"
            value={ddlName}
            onChange={(e) => setDdlName(e.target.value)}
            placeholder="Enter Layer name"
            className="popup-input"
          />
        </div>

        {/* Form Group 2 with File Upload and Drag & Drop */}
        <div
          className={`popup-field file-drop-area ${dragOver ? 'drag-over' : ''}`}
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onClick={handleDropAreaClick}
        >
          <label>DDL Code</label>
          <div className='label-text'>Enter DDL code, drag & drop or click to select file (.sql/.txt).</div>
          <div style={{ height: '500px', width: '100%' }}>
            <Editor
              id="ddl-editor"
              content={ddlCode}
              fontSize={14}
              value={ddlCode}
              onChange={(newCode) => setDdlCode(newCode)}
              editorProps={{ $blockScrolling: true }}
              setOptions={{
                showLineNumbers: true,
                tabSize: 2,
                wrap: true,
              }}
            />
          </div>
          <input
            type="file"
            id="fileInput"
            accept=".sql, .txt"
            onChange={handleFileUpload}
            className="file-input"
          />
        </div>

        {/* Buttons */}
        <div className="popup-buttons">
          <button className="btn-secondary" onClick={() => setShowPopup(false)}>Cancel</button>
          <button className="btn-primary" onClick={handleGenerateClick}>Generate</button>
        </div>
      </div>
    </div>
  );
};

export default SetSemanticLayerModal;
