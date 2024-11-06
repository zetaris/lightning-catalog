import React, { useState, useEffect } from 'react';
import './Components.css';
import logo from '../assets/images/lightning-logo.png';
import '../styleguides/styleguides.css';

function Header({ setView }) {
  const [selectedTab, setSelectedTab] = useState('');

  useEffect(() => {
    const savedTab = sessionStorage.getItem('selectedTab') || 'sqlEditor';
    handleClick(savedTab);
  }, []);

  const handleClick = (viewName) => {
    setSelectedTab(viewName);
    setView(viewName);
    sessionStorage.setItem('selectedTab', viewName);
  };

  return (
    <div className="header-container">
      <a href='https://www.zetaris.com/lightning-opensource' target='_blank'>
        <div className="logo">
          <img src={logo} alt="Logo" />
          <span className="logo_text">Lightning Catalog</span>
        </div>
      </a>
      <div className="menu-tabs">
        <button
          className={`btn-header bold-text ${selectedTab === 'sqlEditor' ? 'selected' : ''}`}
          onClick={() => handleClick('sqlEditor')}
        >
          SQL Editor
        </button>
        <button
          className={`btn-header bold-text ${selectedTab === 'semanticLayer' ? 'selected' : ''}`}
          onClick={() => handleClick('semanticLayer')}
        >
          Semantic Layer
        </button>
        <a href='https://github.com/zetaris/lightning-catalog/tree/master/doc' target='_blank'>
          <button className={`btn-header bold-text`}>
            Documentation
          </button>
        </a>
      </div>
    </div>
  );
}

export default Header;
